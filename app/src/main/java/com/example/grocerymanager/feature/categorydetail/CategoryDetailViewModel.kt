package com.example.grocerymanager.feature.categorydetail

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.model.GroceryItem
import com.example.grocerymanager.domain.repository.CategoryRepository
import com.example.grocerymanager.domain.usecase.DeleteGroceryItemUseCase
import com.example.grocerymanager.domain.usecase.GetGroceryItemsForCategoryUseCase
import com.example.grocerymanager.domain.usecase.UpsertGroceryItemUseCase
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
data class CategoryDetailUiState(
    val isLoading: Boolean = false,
    val category: Category? = null,
    val items: List<GroceryItem> = emptyList(),
    val editor: CategoryItemEditor? = null,
    val showDeleteConfirmFor: GroceryItem? = null,
    val showDeleteCategoryConfirm: Boolean = false,
    val categoryEditor: CategoryEditor? = null,
)

@Immutable
data class CategoryEditor(
    val name: String,
    val iconName: String,
    val colorHex: String,
    val nameErrorKey: CategoryEditorErrorKey? = null,
)

enum class CategoryEditorErrorKey { Empty, Duplicate }

@Immutable
data class CategoryItemEditor(
    val id: Long,
    val name: String,
    val defaultUnit: String,
    val iconName: String? = null,
    val nameErrorKey: EditorErrorKey? = null,
)

enum class EditorErrorKey { Empty, Duplicate }

sealed interface CategoryDetailEvent {
    data class Saved(val itemName: String) : CategoryDetailEvent
    data class Deleted(val itemName: String) : CategoryDetailEvent
    sealed interface Error : CategoryDetailEvent {
        data object DeleteFailed : Error
        data object InUse : Error
        data class Generic(val message: String) : Error
    }
}

@HiltViewModel
open class CategoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categories: CategoryRepository,
    private val getItems: GetGroceryItemsForCategoryUseCase,
    private val upsertItem: UpsertGroceryItemUseCase,
    private val deleteItem: DeleteGroceryItemUseCase,
) : ViewModel() {

    /**
     * The route argument key. Navigation Compose sets this on the SavedStateHandle
     * when navigating to a serializable data class route (the key matches the
     * data class property name).
     */
    open val categoryId: Long = savedStateHandle.get<Long>(KEY_CATEGORY_ID)
        ?: error("CategoryDetail requires '$KEY_CATEGORY_ID' in SavedStateHandle")

    private val _state = MutableStateFlow(CategoryDetailUiState())
    val state: StateFlow<CategoryDetailUiState> = _state.asStateFlow()

    private val _events = Channel<CategoryDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // Stream both the category list (so the header can react to renames
        // and deletion) and the items for this category. One combine, one
        // state update — the previous two independent collectors could race
        // on `_state.update` (B17).
        //
        // B15: when the category is deleted, drop the items so the screen
        // doesn't render orphan tiles under the gray fallback icon.
        viewModelScope.launch {
            combine(
                categories.observeCategories(),
                getItems.observe(categoryId),
            ) { allCategories: List<Category>, items: List<GroceryItem> ->
                val category: Category? = allCategories.firstOrNull { c -> c.id == categoryId }
                Pair(category, if (category == null) emptyList() else items)
            }.collect { pair: Pair<Category?, List<GroceryItem>> ->
                val (category, items) = pair
                _state.update { current ->
                    current.copy(
                        category = category,
                        items = items,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun openEditor(item: GroceryItem? = null) {
        _state.update {
            it.copy(
                editor = CategoryItemEditor(
                    id = item?.id ?: 0L,
                    name = item?.name.orEmpty(),
                    defaultUnit = item?.defaultUnit.orEmpty(),
                    iconName = item?.iconName ?: state.value.category?.iconName,
                ),
            )
        }
    }

    fun closeEditor() {
        _state.update { it.copy(editor = null) }
    }

    fun setEditorName(value: String) {
        _state.update { state ->
            state.copy(
                editor = state.editor?.copy(name = value, nameErrorKey = null),
            )
        }
    }

    fun setEditorDefaultUnit(value: String) {
        _state.update { state ->
            state.copy(editor = state.editor?.copy(defaultUnit = value, nameErrorKey = null))
        }
    }

    fun setEditorIconName(value: String?) {
        _state.update { state ->
            state.copy(editor = state.editor?.copy(iconName = value))
        }
    }

    fun saveEditor() {
        val editor = _state.value.editor ?: return
        val name = editor.name.trim()
        if (name.isEmpty()) {
            _state.update { it.copy(editor = editor.copy(nameErrorKey = EditorErrorKey.Empty)) }
            return
        }
        viewModelScope.launch {
            val result = upsertItem(
                id = editor.id,
                name = name,
                defaultCategoryId = categoryId,
                defaultUnit = editor.defaultUnit.trim(),
                iconName = editor.iconName,
            )
            result.fold(
                onSuccess = {
                    _state.update { it.copy(editor = null) }
                    _events.trySend(CategoryDetailEvent.Saved(name))
                },
                onFailure = { e ->
                    val msg = e.message ?: ""
                    val isDuplicate = msg.contains("UNIQUE", ignoreCase = true) ||
                        msg.contains("duplicate", ignoreCase = true) ||
                        msg.contains("already exists", ignoreCase = true)
                    if (isDuplicate) {
                        // Read the current editor from state (not the captured `editor`)
                        // so we don't lose any updates made between the save call and
                        // this failure handler running.
                        _state.update { current ->
                            current.copy(
                                editor = current.editor?.copy(nameErrorKey = EditorErrorKey.Duplicate),
                            )
                        }
                    } else {
                        _state.update { it.copy(editor = null) }
                        _events.trySend(CategoryDetailEvent.Error.Generic(msg))
                    }
                },
            )
        }
    }

    fun askDelete(item: GroceryItem) {
        _state.update { it.copy(showDeleteConfirmFor = item) }
    }

    /**
     * Delete handler for the edit-page trash icon. Finds the currently
     * edited item by its id and sets `showDeleteConfirmFor` to it so
     * the same single-item confirm dialog is shown. No-op when no
     * item is being edited or the item has been deleted elsewhere.
     */
    fun askDeleteFromEditor() {
        val editorId = _state.value.editor?.id ?: return
        val item = _state.value.items.firstOrNull { it.id == editorId } ?: return
        askDelete(item)
    }

    fun cancelDelete() {
        _state.update { it.copy(showDeleteConfirmFor = null) }
    }

    fun confirmDelete() {
        val item = _state.value.showDeleteConfirmFor ?: return
        _state.update {
            // Clear the editor if the deleted item matches the
            // currently-edited item so the sheet doesn't linger on a
            // stale id after the row is gone.
            it.copy(
                showDeleteConfirmFor = null,
                editor = if (it.editor?.id == item.id) null else it.editor,
            )
        }
        viewModelScope.launch {
            val ok = deleteItem(item.id)
            if (ok) {
                _events.trySend(CategoryDetailEvent.Deleted(item.name))
            } else {
                _events.trySend(CategoryDetailEvent.Error.DeleteFailed)
            }
        }
    }

    /**
     * Bulk delete for the multi-select mode. Deletes each id in a single
     * `viewModelScope.launch` block so the row removes appear in one
     * snackbar / DB transaction batch. Returns the number successfully
     * deleted so the screen can show a count in the snackbar.
     */
    fun deleteMany(ids: List<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            var success = 0
            for (id in ids) {
                if (deleteItem(id)) success += 1
            }
            _events.trySend(
                CategoryDetailEvent.Deleted(
                    if (success == 1) "1 item" else "$success items",
                ),
            )
        }
    }

    // --- Category-level edit / delete ----------------------------------------

    fun openCategoryEditor() {
        val cat = _state.value.category ?: return
        _state.update {
            it.copy(
                categoryEditor = CategoryEditor(
                    name = cat.name,
                    iconName = cat.iconName,
                    colorHex = cat.colorHex,
                ),
            )
        }
    }

    fun closeCategoryEditor() {
        _state.update { it.copy(categoryEditor = null) }
    }

    fun setCategoryEditorName(value: String) {
        _state.update { it.copy(categoryEditor = it.categoryEditor?.copy(name = value, nameErrorKey = null)) }
    }

    fun setCategoryEditorIconName(value: String) {
        _state.update { it.copy(categoryEditor = it.categoryEditor?.copy(iconName = value)) }
    }

    fun saveCategoryEditor() {
        val editor = _state.value.categoryEditor ?: return
        val cat = _state.value.category ?: return
        val name = editor.name.trim()
        if (name.isEmpty()) {
            _state.update { it.copy(categoryEditor = editor.copy(nameErrorKey = CategoryEditorErrorKey.Empty)) }
            return
        }
        viewModelScope.launch {
            val updated = cat.copy(
                name = name,
                iconName = editor.iconName,
                colorHex = editor.colorHex,
            )
            runCatching { categories.update(updated) }
                .onSuccess {
                    _state.update { it.copy(categoryEditor = null) }
                    _events.trySend(CategoryDetailEvent.Saved(name))
                }
                .onFailure { e ->
                    val msg = e.message ?: ""
                    val isDuplicate = msg.contains("UNIQUE", ignoreCase = true) ||
                        msg.contains("duplicate", ignoreCase = true) ||
                        msg.contains("already exists", ignoreCase = true)
                    if (isDuplicate) {
                        _state.update { it.copy(categoryEditor = editor.copy(nameErrorKey = CategoryEditorErrorKey.Duplicate)) }
                    } else {
                        _state.update { it.copy(categoryEditor = null) }
                        _events.trySend(CategoryDetailEvent.Error.Generic(msg))
                    }
                }
        }
    }

    fun askDeleteCategory() {
        val cat = _state.value.category ?: return
        _state.update { it.copy(showDeleteCategoryConfirm = true) }
    }

    fun cancelDeleteCategory() {
        _state.update { it.copy(showDeleteCategoryConfirm = false) }
    }

    fun confirmDeleteCategory() {
        val cat = _state.value.category ?: return
        _state.update { it.copy(showDeleteCategoryConfirm = false) }
        viewModelScope.launch {
            runCatching { categories.delete(cat.id) }
                .onSuccess { ok ->
                    if (ok) {
                        _events.trySend(CategoryDetailEvent.Deleted(cat.name))
                    } else {
                        _events.trySend(CategoryDetailEvent.Error.DeleteFailed)
                    }
                }
                .onFailure {
                    Log.w(TAG, "confirmDeleteCategory failed for id=${cat.id}", it)
                    _events.trySend(CategoryDetailEvent.Error.InUse)
                }
        }
    }

    private companion object {
        const val TAG = "CategoryDetailViewModel"
    }
}

private const val KEY_CATEGORY_ID = "categoryId"
