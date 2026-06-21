package com.example.grocerymanager.feature.categories

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerymanager.core.designsystem.color.BrandColors
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

@Immutable
data class CategoriesUiState(
    val pendingDelete: Category? = null,
)

sealed interface CategoriesEvent {
    sealed interface Error : CategoriesEvent {
        /** The category is in use by purchase items; Room throws FK violation. */
        data object InUse : Error
        /** The DAO returned 0 — usually a non-default category id. */
        data object DeleteFailed : Error
        /** The reorder pass threw; the local mirror is now out of sync. */
        data object ReorderFailed : Error
        /** Catch-all for create / update errors with a user-facing message. */
        data class Generic(val message: String) : Error
    }
    data class Deleted(val name: String) : CategoriesEvent
}
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repo: CategoryRepository,
) : ViewModel() {

    val categories: StateFlow<List<Category>> = repo.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _state = MutableStateFlow(CategoriesUiState())
    val state: StateFlow<CategoriesUiState> = _state.asStateFlow()

    private val _events = Channel<CategoriesEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun add(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repo.add(
                Category(
                    id = 0,
                    name = name.trim(),
                    iconName = BrandColors.CategoryDefaultIcon,
                    colorHex = BrandColors.CategoryFallbackHex,
                    isDefault = false,
                    sortOrder = 100,
                ),
            )
        }
    }

    fun askDelete(category: Category) {
        _state.update { it.copy(pendingDelete = category) }
    }

    /**
     * Create a new category from the add-category sheet. Used by both
     * the Categories-page FAB and the SelectItem sheet's "Add category"
     * chip. `name` is trimmed and ignored if blank. The new category
     * gets a `sortOrder` larger than any existing one so it lands at
     * the bottom of the list (consistent with the previous inline-add
     * behavior).
     */
    fun createCategory(name: String, iconName: String?) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val nextOrder = (categories.value.maxOfOrNull { it.sortOrder } ?: 0) + 100
            runCatching {
                repo.add(
                    Category(
                        id = 0L,
                        name = trimmed,
                        iconName = iconName ?: "",
                        colorHex = "",
                        isDefault = false,
                        sortOrder = nextOrder,
                    ),
                )
            }.onFailure {
                Log.w(TAG, "createCategory failed for name=$trimmed", it)
                _events.trySend(CategoriesEvent.Error.Generic(it.message ?: "Create failed"))
            }
        }
    }

    /**
     * Persist a new ordering for the categories. The list contains the
     * category ids in the order they should appear in the UI. The repository
     * assigns fresh sortOrder values so the change is durable across restarts.
     */
    fun reorder(orderedIds: List<Long>) {
        if (orderedIds.isEmpty()) return
        viewModelScope.launch {
            runCatching { repo.reorder(orderedIds) }
                .onFailure {
                    Log.w(TAG, "reorder failed for ids=$orderedIds", it)
                    _events.trySend(CategoriesEvent.Error.ReorderFailed)
                }
        }
    }

    fun cancelDelete() {
        _state.update { it.copy(pendingDelete = null) }
    }

    fun confirmDelete() {
        val category = _state.value.pendingDelete ?: return
        _state.update { it.copy(pendingDelete = null) }
        viewModelScope.launch {
            runCatching { repo.delete(category.id) }
                .onSuccess { ok ->
                    if (ok) {
                        _events.trySend(CategoriesEvent.Deleted(category.name))
                    } else {
                        _events.trySend(CategoriesEvent.Error.DeleteFailed)
                    }
                }
                .onFailure {
                    // Most likely a foreign-key violation from
                    // purchase_items.categoryId being RESTRICT. Any other
                    // failure (SQLite corruption, disk full, …) is misreported
                    // as "in use" today; we log the actual cause so production
                    // failures are diagnosable.
                    Log.w(TAG, "confirmDelete failed for id=${category.id}", it)
                    _events.trySend(CategoriesEvent.Error.InUse)
                }
        }
    }

    private companion object {
        const val TAG = "CategoriesViewModel"
    }
}
