package com.example.grocerymanager.feature.categorydetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.grocerymanager.core.common.MinorUnits
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.model.GroceryItem
import com.example.grocerymanager.domain.repository.CategoryRepository
import com.example.grocerymanager.domain.repository.GroceryItemRepository
import com.example.grocerymanager.domain.usecase.DeleteGroceryItemUseCase
import com.example.grocerymanager.domain.usecase.GetGroceryItemsForCategoryUseCase
import com.example.grocerymanager.domain.usecase.UpsertGroceryItemUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val categoryId = 7L
    private val otherCategory = Category(99, "Other", "Box", "#888", isDefault = false, sortOrder = 0)
    private val targetCategory = Category(categoryId, "Vegetables", "Carrot", "#43D17A", isDefault = true, sortOrder = 3)

    private lateinit var categoriesRepo: FakeCategoryRepository
    private lateinit var itemsRepo: FakeGroceryItemRepository
    private lateinit var vm: CategoryDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        categoriesRepo = FakeCategoryRepository(mutableListOf(targetCategory, otherCategory))
        itemsRepo = FakeGroceryItemRepository(
            categoryId = categoryId,
            initialItems = listOf(
                GroceryItem(1, "Tomato", categoryId, "pcs", null, null),
                GroceryItem(2, "Potato", categoryId, "kg", null, null),
            ),
        )
        vm = TestableCategoryDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("categoryId" to categoryId)),
            categoryIdOverride = categoryId,
            categories = categoriesRepo,
            getItems = GetGroceryItemsForCategoryUseCase(itemsRepo),
            upsertItem = UpsertGroceryItemUseCase(itemsRepo),
            deleteItem = DeleteGroceryItemUseCase(itemsRepo),
        )
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `state reflects items for the target category`() = runTest(dispatcher) {
        advanceUntilIdle()
        val s = vm.state.value
        assertThat(s.isLoading).isFalse()
        assertThat(s.category).isEqualTo(targetCategory)
        assertThat(s.items.map { it.name }).containsExactly("Tomato", "Potato").inOrder()
    }

    @Test
    fun `openEditor seeds new draft with id=0 and empty fields`() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.openEditor()
        advanceUntilIdle()
        val editor = vm.state.value.editor
        assertThat(editor).isNotNull()
        assertThat(editor!!.id).isEqualTo(0L)
        assertThat(editor.name).isEmpty()
        assertThat(editor.defaultUnit).isEmpty()
    }

    @Test
    fun `openEditor with existing item seeds the editor with its values`() = runTest(dispatcher) {
        advanceUntilIdle()
        val existing = GroceryItem(3, "Chili", categoryId, "kg", null, null)
        vm.openEditor(existing)
        advanceUntilIdle()
        val editor = vm.state.value.editor!!
        assertThat(editor.id).isEqualTo(3L)
        assertThat(editor.name).isEqualTo("Chili")
        assertThat(editor.defaultUnit).isEqualTo("kg")
    }

    @Test
    fun `saveEditor calls upsert with id, name, categoryId, defaultUnit`() = runTest(dispatcher) {
        itemsRepo.nextUpsertResult = 42L
        advanceUntilIdle()
        vm.openEditor()
        advanceUntilIdle()
        vm.setEditorName("Spinach")
        vm.setEditorDefaultUnit("kg")
        vm.saveEditor()
        advanceUntilIdle()
        val saved = itemsRepo.lastUpserted
        assertThat(saved).isNotNull()
        assertThat(saved!!.name).isEqualTo("Spinach")
        assertThat(saved.defaultCategoryId).isEqualTo(categoryId)
        assertThat(saved.defaultUnit).isEqualTo("kg")
        assertThat(vm.state.value.editor).isNull()
    }

    @Test
    fun `saveEditor flags empty name without invoking upsert`() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.openEditor()
        vm.saveEditor()
        advanceUntilIdle()
        assertThat(itemsRepo.upsertCount).isEqualTo(0)
        assertThat(vm.state.value.editor?.nameErrorKey).isEqualTo(EditorErrorKey.Empty)
    }

    @Test
    fun `saveEditor flags duplicate name when upsert throws unique-constraint error`() = runTest(dispatcher) {
        // android.database.sqlite.SQLiteConstraintException is a stub in unit tests
        // and does not preserve the constructor message. Use a real Throwable
        // subclass so the message (and therefore the duplicate detection) works.
        itemsRepo.upsertError = RuntimeException("UNIQUE constraint failed: grocery_items.name")
        advanceUntilIdle()
        vm.openEditor()
        vm.setEditorName("Tomato")
        vm.saveEditor()
        advanceUntilIdle()
        assertThat(itemsRepo.upsertCount).isEqualTo(1)
        assertThat(vm.state.value.editor?.nameErrorKey).isEqualTo(EditorErrorKey.Duplicate)
    }

    @Test
    fun `askDelete then confirmDelete invokes the delete use case`() = runTest(dispatcher) {
        itemsRepo.nextDeleteResult = true
        advanceUntilIdle()
        val item = GroceryItem(2, "Potato", categoryId, "kg", null, null)
        vm.askDelete(item)
        advanceUntilIdle()
        assertThat(vm.state.value.showDeleteConfirmFor).isEqualTo(item)
        vm.confirmDelete()
        advanceUntilIdle()
        assertThat(itemsRepo.lastDeletedId).isEqualTo(2L)
        assertThat(vm.state.value.showDeleteConfirmFor).isNull()
    }

    @Test
    fun `cancelDelete clears the pending item without invoking delete`() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.askDelete(GroceryItem(1, "Tomato", categoryId, "pcs", null, null))
        advanceUntilIdle()
        vm.cancelDelete()
        advanceUntilIdle()
        assertThat(itemsRepo.deleteCount).isEqualTo(0)
        assertThat(vm.state.value.showDeleteConfirmFor).isNull()
    }

    @Test
    fun `saveEditor then Saved event is emitted`() = runTest(dispatcher) {
        itemsRepo.nextUpsertResult = 1L
        advanceUntilIdle()
        vm.openEditor()
        vm.setEditorName("Cucumber")
        vm.saveEditor()
        advanceUntilIdle()
        vm.events.test {
            val event = awaitItem()
            assertThat(event).isInstanceOf(CategoryDetailEvent.Saved::class.java)
            assertThat((event as CategoryDetailEvent.Saved).itemName).isEqualTo("Cucumber")
        }
    }

    @Test
    fun `items are hidden when their category is deleted (B15)`() = runTest(dispatcher) {
        advanceUntilIdle()
        assertThat(vm.state.value.items).hasSize(2)

        // Simulate the user deleting the target category.
        categoriesRepo.replace(listOf(otherCategory))
        advanceUntilIdle()

        val s = vm.state.value
        assertThat(s.category).isNull()
        assertThat(s.items).isEmpty()
    }
}

private class FakeCategoryRepository(
    initialCategories: List<Category>,
) : CategoryRepository {
    private val flow = MutableStateFlow(initialCategories)
    var added: Category? = null
    var updated: Category? = null
    var deletedId: Long? = null

    override fun observeCategories(): Flow<List<Category>> = flow
    override suspend fun getById(id: Long): Category? = flow.value.firstOrNull { it.id == id }
    override suspend fun add(category: Category): Long {
        added = category
        val newId = (flow.value.maxOfOrNull { it.id } ?: 0L) + 1L
        flow.value = flow.value + category.copy(id = newId)
        return newId
    }
    override suspend fun update(category: Category) { updated = category }
    override suspend fun reorder(orderedIds: List<Long>) {}
    override suspend fun delete(id: Long): Boolean { deletedId = id; return true }

    /** Test helper: replace the entire category list to simulate a delete. */
    fun replace(newList: List<Category>) { flow.value = newList }
}

private class FakeGroceryItemRepository(
    private val categoryId: Long,
    initialItems: List<GroceryItem>,
) : GroceryItemRepository {
    // perCategory is a hot StateFlow that the VM's combine subscribes to. We expose
    // a derived Flow for observeAll/search/observeForCategory so the combine
    // stays subscribed even after the first emission.
    private val perCategory: MutableStateFlow<Map<Long, List<GroceryItem>>> = MutableStateFlow(
        mapOf(categoryId to initialItems),
    )
    private fun itemsFor(id: Long): Flow<List<GroceryItem>> = perCategory.map { it[id].orEmpty() }

    var lastUpserted: GroceryItem? = null
    var upsertCount: Int = 0
    var nextUpsertResult: Long = 1L
    var upsertError: Throwable? = null
    var lastDeletedId: Long? = null
    var deleteCount: Int = 0
    var nextDeleteResult: Boolean = true

        override fun observeAll(): Flow<List<GroceryItem>> =
        perCategory.map { it.values.flatten() as List<GroceryItem> }
    override fun search(query: String, limit: Int): Flow<List<GroceryItem>> = observeAll()
    override fun observeForCategory(id: Long): Flow<List<GroceryItem>> = itemsFor(id)
    override suspend fun getById(id: Long): GroceryItem? =
        perCategory.value.values.flatten().firstOrNull { it.id == id }
    override suspend fun upsert(item: GroceryItem): Long {
        upsertCount++
        lastUpserted = item
        upsertError?.let { throw it }
        val current = perCategory.value.toMutableMap()
        val categoryKey = item.defaultCategoryId ?: error("upsert requires defaultCategoryId")
        val list = current[categoryKey].orEmpty().toMutableList()
        val idx = list.indexOfFirst { it.id == item.id }
        if (idx >= 0) list[idx] = item else list.add(item)
        current[categoryKey] = list
        perCategory.value = current
        return nextUpsertResult
    }
    override suspend fun update(item: GroceryItem) {}
    override suspend fun delete(id: Long): Boolean {
        deleteCount++
        lastDeletedId = id
        return nextDeleteResult
    }
    override suspend fun updateLastPrice(id: Long, price: MinorUnits) {}
}

/**
 * Test double that overrides [CategoryDetailViewModel.categoryId] so we don't have
 * to mock the inline reified [SavedStateHandle.toRoute] extension. All other VM
 * logic is identical.
 */
private class TestableCategoryDetailViewModel(
    savedStateHandle: SavedStateHandle,
    categoryIdOverride: Long,
    categories: CategoryRepository,
    getItems: GetGroceryItemsForCategoryUseCase,
    upsertItem: UpsertGroceryItemUseCase,
    deleteItem: DeleteGroceryItemUseCase,
) : CategoryDetailViewModel(
    savedStateHandle = savedStateHandle,
    categories = categories,
    getItems = getItems,
    upsertItem = upsertItem,
    deleteItem = deleteItem,
) {
    override val categoryId: Long = categoryIdOverride
}
