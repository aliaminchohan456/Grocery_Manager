package com.example.grocerymanager.feature.purchasedetail

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.model.Purchase
import com.example.grocerymanager.domain.model.PurchaseItem
import com.example.grocerymanager.domain.model.PurchaseWithItems
import com.example.grocerymanager.domain.repository.CategoryRepository
import com.example.grocerymanager.domain.usecase.DeletePurchaseUseCase
import com.example.grocerymanager.domain.usecase.GetPurchaseWithItemsUseCase
import com.example.grocerymanager.domain.usecase.SavePurchaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.navigation.toRoute
import com.example.grocerymanager.navigation.PurchaseDetailRoute

@Immutable
data class PurchaseDetailUiState(
    val isLoading: Boolean = true,
    val currencyCode: String = "USD",
    val purchase: Purchase? = null,
    val items: List<PurchaseItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val showConfirm: Boolean = false,
)

sealed interface PurchaseDetailEvent {
    data object Deleted : PurchaseDetailEvent
    sealed interface Error : PurchaseDetailEvent {
        data class Generic(val message: String?) : Error
    }
}

@HiltViewModel
class PurchaseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    preferences: UserPreferencesRepository,
    private val categoriesRepo: CategoryRepository,
    private val getWithItems: GetPurchaseWithItemsUseCase,
    private val deletePurchase: DeletePurchaseUseCase,
    private val savePurchase: SavePurchaseUseCase,
) : ViewModel() {

    private val purchaseId: Long = savedStateHandle.toRoute<PurchaseDetailRoute>().purchaseId

    private val local = MutableStateFlow(LocalState())

    private data class LocalState(
        val isLoading: Boolean = true,
        val purchase: Purchase? = null,
        val items: List<PurchaseItem> = emptyList(),
        val showConfirm: Boolean = false,
    )

    // Kept in memory so the user can undo a delete from the Snackbar action.
    private var lastDeleted: PurchaseWithItems? = null

    val state: StateFlow<PurchaseDetailUiState> = combine(
        preferences.preferences,
        categoriesRepo.observeCategories(),
        getWithItems.observe(purchaseId),
        // B-fix: `local` MUST be a source flow here, otherwise askDelete /
        // cancelDelete / confirmDelete mutations to `local.showConfirm` never
        // cause this combine to re-emit, and the screen's `if (state.showConfirm)`
        // branch never opens the confirm dialog. Reading `local.value` inside
        // the transform body without subscribing to the flow was a subtle
        // re-entrancy hazard that hid the delete button.
        local,
    ) { prefs, categories, data, localValue ->
        PurchaseDetailUiState(
            isLoading = localValue.isLoading && data == null,
            currencyCode = prefs.currencyCode,
            purchase = data?.purchase ?: localValue.purchase,
            items = data?.items ?: localValue.items,
            categories = categories,
            showConfirm = localValue.showConfirm,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PurchaseDetailUiState())

    private val _events = kotlinx.coroutines.channels.Channel<PurchaseDetailEvent>(
        kotlinx.coroutines.channels.Channel.BUFFERED,
    )
    val events = _events.receiveAsFlow()

    fun askDelete() { local.update { it.copy(showConfirm = true) } }
    fun cancelDelete() { local.update { it.copy(showConfirm = false) } }

    fun confirmDelete() {
        // Flip isLoading off BEFORE the DB write so that when the flow
        // re-emits with `data == null` (because the purchase is gone),
        // the combine yields `isLoading = false && data == null = false`
        // instead of `true && true = true` — the latter would re-trigger
        // `LoadingState` and hide the screen content under a spinner for
        // the entire snackbar dwell time.
        local.update { it.copy(showConfirm = false, isLoading = false) }
        viewModelScope.launch {
            try {
                // Snapshot before deleting so we can restore on undo.
                val snapshot = getWithItems(purchaseId)
                lastDeleted = snapshot
                // Stash the just-deleted purchase into local so the
                // screen keeps rendering its content (hero total + items
                // list) while the "Purchase deleted · Undo" snackbar is
                // visible. Without this, the screen falls through to
                // `EmptyState` as soon as the DB flow re-emits null.
                if (snapshot != null) {
                    local.update {
                        it.copy(purchase = snapshot.purchase, items = snapshot.items)
                    }
                }
                deletePurchase(purchaseId)
                _events.trySend(PurchaseDetailEvent.Deleted)
            } catch (e: Exception) {
                Log.w(TAG, "confirmDelete failed for purchaseId=$purchaseId", e)
                _events.trySend(PurchaseDetailEvent.Error.Generic(e.message ?: "Delete failed"))
            }
        }
    }

    fun undoLastDelete() {
        val snapshot = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch {
            // Save as a brand new purchase: clear the id so the underlying
            // savePurchaseWithItems treats it as an insert.
            val now = System.currentTimeMillis()
            val resaved = snapshot.purchase.copy(
                id = 0L,
                createdAt = now,
                updatedAt = now,
            )
            val res = savePurchase(resaved, snapshot.items)
            res.onFailure { e ->
                Log.w(TAG, "undoLastDelete failed for purchaseId=${snapshot.purchase.id}", e)
                _events.trySend(PurchaseDetailEvent.Error.Generic(e.message ?: "Undo failed"))
            }
        }
    }

    private companion object {
        const val TAG = "PurchaseDetailViewModel"
    }
}
