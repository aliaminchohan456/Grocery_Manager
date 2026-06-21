package com.example.grocerymanager.feature.addpurchase

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.model.GroceryItem
import com.example.grocerymanager.domain.model.Purchase
import com.example.grocerymanager.domain.model.PurchaseItem
import com.example.grocerymanager.domain.repository.CategoryRepository
import com.example.grocerymanager.domain.repository.GroceryItemRepository
import com.example.grocerymanager.domain.repository.PurchaseRepository
import com.example.grocerymanager.domain.usecase.GetPurchaseWithItemsUseCase
import com.example.grocerymanager.domain.usecase.SavePurchaseUseCase
import com.example.grocerymanager.domain.usecase.UpsertGroceryItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DraftItem(
    val tempId: Long,
    val itemName: String,
    val categoryId: Long,
    val quantity: Double,
    val unit: String,
    val pricePerUnit: Long?,
    val totalPrice: Long,
    val groceryItemId: Long? = null,
)

@Immutable
data class AddPurchaseUiState(
    val isEdit: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val purchaseDate: Long = System.currentTimeMillis(),
    val shopName: String = "",
    val notes: String = "",
    val items: List<DraftItem> = emptyList(),
    val receiptImageUri: String? = null,
    val total: Long = 0L,
    val currencyCode: String = "USD",
    val canSave: Boolean = false,
    val showSelectItemSheet: Boolean = false,
    val showDatePicker: Boolean = false,
    val editingItem: DraftItem? = null,
    val errorMessage: String? = null,
)

@Immutable
sealed interface AddPurchaseEvent {
    data object Saved : AddPurchaseEvent
    data class Error(val key: AddPurchaseError) : AddPurchaseEvent
}

enum class AddPurchaseError { NotFound, SaveFailed }

@HiltViewModel
class AddPurchaseViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val categories: CategoryRepository,
    private val groceryItems: GroceryItemRepository,
    private val purchases: PurchaseRepository,
    private val savePurchase: SavePurchaseUseCase,
    private val getWithItems: GetPurchaseWithItemsUseCase,
    private val upsertGroceryItem: UpsertGroceryItemUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AddPurchaseUiState())
    val state: StateFlow<AddPurchaseUiState> = _state.asStateFlow()

    val categoryList: StateFlow<List<Category>> = categories.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Full catalog of grocery items, used by the "Select item" sheet to render
     * the items grid for the chosen category. The flow stays hot for 5s after
     * the last subscriber leaves so quick open/close of the sheet doesn't
     * re-query the database.
     */
    val groceryItemList: StateFlow<List<GroceryItem>> = groceryItems.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Distinct, non-blank shop names from previously saved purchases. Used
     * by the shop-name input to show a one-tap autocomplete list under the
     * field so the user doesn't have to retype the same shop on every bill.
     */
    val shopSuggestions: StateFlow<List<String>> = purchases.observeShopNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = Channel<AddPurchaseEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Atomic so concurrent upserts (e.g. from a coroutine on a background
    // dispatcher) can never mint the same tempId. Starts at -1L so the first
    // new draft gets -2L, then -3L, etc.
    private val nextTempId = AtomicLong(-1L)
    private var originalCreatedAt: Long? = null

    init {
        viewModelScope.launch {
            preferences.preferences.collect { prefs ->
                _state.update { it.copy(currencyCode = prefs.currencyCode) }
            }
        }
    }

    fun loadForEdit(purchaseId: Long) {
        if (purchaseId <= 0) return
        _state.update { it.copy(isLoading = true, isEdit = true) }
        viewModelScope.launch {
            val data = getWithItems(purchaseId) ?: run {
                _events.trySend(AddPurchaseEvent.Error(AddPurchaseError.NotFound))
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            originalCreatedAt = data.purchase.createdAt
            val items = data.items.mapIndexed { idx, item ->
                DraftItem(
                    tempId = -1L - idx,
                    itemName = item.itemName,
                    categoryId = item.categoryId,
                    quantity = item.quantity,
                    unit = item.unit,
                    pricePerUnit = item.pricePerUnit,
                    totalPrice = item.totalPrice,
                    groceryItemId = item.groceryItemId,
                )
            }
            // Initialise nextTempId strictly below every loaded item's tempId
            // so newly-added drafts never collide with existing ones.
            nextTempId.set(-1L - items.size.toLong() - 1L)
            _state.update {
                it.copy(
                    isLoading = false,
                    isEdit = true,
                    purchaseDate = data.purchase.purchaseDate,
                    shopName = data.purchase.shopName.orEmpty(),
                    notes = data.purchase.notes.orEmpty(),
                    receiptImageUri = data.purchase.receiptImageUri,
                    items = items,
                )
            }
            recomputeTotal()
        }
    }

    fun setDate(epochMillis: Long) {
        _state.update { it.copy(purchaseDate = epochMillis, showDatePicker = false) }
    }

    fun showDatePicker(show: Boolean) { _state.update { it.copy(showDatePicker = show) } }
    fun setShopName(name: String) { _state.update { it.copy(shopName = name) }; recomputeCanSave() }
    fun setNotes(notes: String) { _state.update { it.copy(notes = notes) } }

    fun openSelectItemSheet() {
        _state.update { it.copy(showSelectItemSheet = true) }
    }

    fun closeSelectItemSheet() {
        _state.update { it.copy(showSelectItemSheet = false) }
    }

    /**
     * Persist a new grocery item to the catalog. Used by the "+ Add item"
     * card inside the SelectItem sheet — the user creates a fresh item
     * without leaving the bill flow.
     *
     * @return [Result.success] with the new item's id, or [Result.failure]
     *   if validation failed (e.g. empty name).
     */
    suspend fun createNewGroceryItem(
        name: String,
        categoryId: Long,
        unit: String,
        iconName: String?,
    ): Result<Long> = upsertGroceryItem(
        id = 0L,
        name = name,
        defaultCategoryId = categoryId,
        defaultUnit = unit,
        iconName = iconName,
    )

    /**
     * Create a new category from the SelectItem sheet's "Add category"
     * chip. Returns the new id on success, or a failure result if the
     * name is blank or the DB write throws. The new category gets a
     * `sortOrder` larger than any existing one so it lands at the
     * bottom of the chip row.
     */
    suspend fun createNewCategory(name: String, iconName: String?): Result<Long> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("Name is blank"))
        return runCatching {
            val nextOrder = (categoryList.value.maxOfOrNull { it.sortOrder } ?: 0) + 100
            categories.add(
                Category(
                    id = 0L,
                    name = trimmed,
                    iconName = iconName ?: "",
                    colorHex = "",
                    isDefault = false,
                    sortOrder = nextOrder,
                ),
            )
        }
    }

    fun upsertItem(draft: DraftItem) {
        _state.update { s ->
            val existingIdx = s.items.indexOfFirst { it.tempId == draft.tempId }
            val newItems = if (existingIdx >= 0) {
                s.items.toMutableList().also { it[existingIdx] = draft }
            } else {
                // nextTempId was initialised below all existing tempIds by loadForEdit
                // (or is -1L for a fresh bill). Atomically decrement to mint a fresh
                // unique id.
                val id = nextTempId.decrementAndGet()
                s.items + draft.copy(tempId = id)
            }
            s.copy(items = newItems, showSelectItemSheet = false)
        }
        recomputeTotal()
    }

    fun removeItem(tempId: Long) {
        _state.update { it.copy(items = it.items.filterNot { item -> item.tempId == tempId }) }
        recomputeTotal()
    }

    /**
     * Open the inline item-editor sheet for an existing draft row. The user
     * taps a row in the items list (especially after items are pre-filled
     * from the shopping-list convert flow where every row lands with
     * `totalPrice = 0`) and edits the qty / unit / price / total. The
     * editor sheet reads back from `state.editingItem` so the user can
     * freely dismiss / re-open without losing their in-progress edits
     * until they explicitly Save or Cancel.
     */
    fun startEditingItem(tempId: Long) {
        _state.update { s ->
            s.copy(editingItem = s.items.firstOrNull { it.tempId == tempId })
        }
    }

    fun cancelEditingItem() {
        _state.update { it.copy(editingItem = null) }
    }

    /**
     * Commit edits back to the items list. Re-uses [upsertItem] so the
     * existing tempId lookup keeps the row in place (no flash, no reorder).
     * Recomputes the bill total afterward so the "Update purchase" CTA
     * enables as soon as any item has a non-zero price.
     */
    fun commitItemEdits(updated: DraftItem) {
        upsertItem(updated)
        _state.update { it.copy(editingItem = null) }
    }

    private fun recomputeTotal() {
        _state.update { s ->
            val total = s.items.sumOf { it.totalPrice }
            s.copy(total = total)
        }
        recomputeCanSave()
    }

    /**
     * Single source of truth for the "Save purchase" CTA enablement.
     *
     * All three conditions must hold:
     *  - at least one item is on the bill
     *  - the bill total is > 0
     *  - a non-blank shop name is entered
     *
     * Shop name is treated as required (the field label no longer says
     * "(optional)") — a blank name used to silently round-trip as `null`
     * in the DB and the card would then render as "Unknown shop" in the
     * Home / Records lists. Requiring the shop name here means every row
     * in the recent-purchases section is identifiable.
     */
    private fun recomputeCanSave() {
        val s = _state.value
        val canSave = s.items.isNotEmpty() && s.total > 0 && s.shopName.isNotBlank()
        _state.update { it.copy(canSave = canSave) }
    }

    fun save(existingId: Long = 0L) {
        val s = _state.value
        if (!s.canSave) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val purchase = Purchase(
                id = existingId,
                purchaseDate = s.purchaseDate,
                shopName = s.shopName.ifBlank { null },
                totalAmount = s.total,
                notes = s.notes.ifBlank { null },
                receiptImageUri = s.receiptImageUri,
                createdAt = originalCreatedAt ?: now,
                updatedAt = now,
            )
            val items = s.items.map { it.toDomain(existingId) }
            val result = savePurchase(purchase, items)
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isSaving = false) }
                    _events.trySend(AddPurchaseEvent.Saved)
                },
                onFailure = { e ->
                    _state.update { it.copy(isSaving = false) }
                    _events.trySend(AddPurchaseEvent.Error(AddPurchaseError.SaveFailed))
                },
            )
        }
    }
}

private fun DraftItem.toDomain(purchaseId: Long): PurchaseItem = PurchaseItem(
    id = 0L,
    purchaseId = purchaseId,
    itemName = itemName,
    groceryItemId = groceryItemId,
    categoryId = categoryId,
    quantity = quantity,
    unit = unit,
    pricePerUnit = pricePerUnit,
    totalPrice = totalPrice,
)
