package com.example.grocerymanager.feature.shoppinglist

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.model.ShoppingListItem
import com.example.grocerymanager.domain.repository.CategoryRepository
import com.example.grocerymanager.domain.repository.ShoppingListRepository
import com.example.grocerymanager.domain.usecase.ConvertShoppingListToPurchaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ShoppingListUiState(
    val currencyCode: String = "USD",
    val items: List<ShoppingListItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selected: Set<Long> = emptySet(),
)

sealed interface ShoppingListEvent {
    /** Carries the created purchase id so the AddPurchase screen can pre-load it for editing. */
    data class ConvertedToPurchase(val purchaseId: Long) : ShoppingListEvent
    sealed interface Error : ShoppingListEvent {
        data object SelectOne : Error
        data object NoCategory : Error
        data object NoItems : Error
        data object ConvertFailed : Error
    }
}

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val repo: ShoppingListRepository,
    private val categoriesRepo: CategoryRepository,
    private val convertToPurchase: ConvertShoppingListToPurchaseUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ShoppingListUiState())
    val state: StateFlow<ShoppingListUiState> = _state.asStateFlow()

    private val _events = Channel<ShoppingListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                preferences.preferences,
                repo.observeAll(),
                categoriesRepo.observeCategories(),
            ) { prefs, items, cats ->
                Triple(prefs, items, cats)
            }.collect { (prefs, items, cats) ->
                // Use _state.update so the user-toggled `selected` set is preserved
                // and we don't lose it on every pref/items/categories emission.
                _state.update { current ->
                    current.copy(
                        currencyCode = prefs.currencyCode,
                        items = items,
                        categories = cats,
                    )
                }
            }
        }
    }

    fun add(name: String, quantity: Double, unit: String, categoryId: Long?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repo.add(
                ShoppingListItem(
                    id = 0,
                    name = name.trim(),
                    quantity = quantity,
                    unit = unit,
                    categoryId = categoryId,
                    isPurchased = false,
                    purchasedAt = null,
                ),
            )
        }
    }

    fun togglePurchased(item: ShoppingListItem) {
        viewModelScope.launch { repo.togglePurchased(item) }
    }

    fun remove(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }

    fun toggleSelected(id: Long) {
        _state.update {
            val current = it.selected
            it.copy(selected = if (current.contains(id)) current - id else current + id)
        }
    }

    fun clearSelection() { _state.update { it.copy(selected = emptySet()) } }

    fun convertSelectedToPurchase() {
        val s = _state.value
        val selectedItems = s.items.filter { it.id in s.selected }
        if (selectedItems.isEmpty()) {
            _events.trySend(ShoppingListEvent.Error.SelectOne)
            return
        }
        val defaultCategoryId = s.categories.firstOrNull()?.id
        if (defaultCategoryId == null) {
            _events.trySend(ShoppingListEvent.Error.NoCategory)
            return
        }
        viewModelScope.launch {
            runCatching { convertToPurchase(selectedItems, defaultCategoryId) }
                .onSuccess { purchaseId ->
                    _state.update { it.copy(selected = emptySet()) }
                    if (purchaseId > 0L) {
                        _events.trySend(ShoppingListEvent.ConvertedToPurchase(purchaseId))
                    } else {
                        _events.trySend(ShoppingListEvent.Error.NoItems)
                    }
                }
                .onFailure {
                    Log.w(TAG, "convertSelectedToPurchase failed (n=${selectedItems.size})", it)
                    _events.trySend(ShoppingListEvent.Error.ConvertFailed)
                }
        }
    }

    private companion object {
        const val TAG = "ShoppingListViewModel"
    }
}
