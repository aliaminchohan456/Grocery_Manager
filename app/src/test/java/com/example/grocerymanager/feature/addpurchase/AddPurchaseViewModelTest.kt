package com.example.grocerymanager.feature.addpurchase

import app.cash.turbine.test
import com.example.grocerymanager.core.common.MinorUnits
import com.example.grocerymanager.core.preferences.ThemeMode
import com.example.grocerymanager.core.preferences.UnitSystem
import com.example.grocerymanager.core.preferences.UserPreferences
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.model.GroceryItem
import com.example.grocerymanager.domain.model.Purchase
import com.example.grocerymanager.domain.model.PurchaseItem
import com.example.grocerymanager.domain.model.PurchaseWithItems
import com.example.grocerymanager.domain.repository.CategoryRepository
import com.example.grocerymanager.domain.repository.PurchaseRepository
import com.example.grocerymanager.domain.usecase.GetPurchaseWithItemsUseCase
import com.example.grocerymanager.domain.usecase.SavePurchaseUseCase
import com.example.grocerymanager.domain.usecase.UpsertGroceryItemUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddPurchaseViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val preferences = FakePreferences()
    private val categoriesRepo = FakeCategoryRepository()
    private val groceryItemsRepo = FakeGroceryItemRepository()
    private val purchases = FakePurchaseRepository()
    private val savePurchase = SavePurchaseUseCase(purchases, groceryItemsRepo)
    private val getWithItems = GetPurchaseWithItemsUseCase(purchases)
    private val upsertGroceryItem = UpsertGroceryItemUseCase(groceryItemsRepo)

    private lateinit var vm: AddPurchaseViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        categoriesRepo.categories = listOf(Category(1, "Default", "Box", "#000000", isDefault = true, sortOrder = 0))
        vm = AddPurchaseViewModel(
            preferences = preferences,
            categories = categoriesRepo,
            groceryItems = groceryItemsRepo,
            purchases = purchases,
            savePurchase = savePurchase,
            getWithItems = getWithItems,
            upsertGroceryItem = upsertGroceryItem,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tempIds in a fresh bill are unique and monotonically decreasing`() = runTest(dispatcher) {
        vm.upsertItem(newDraft(tempId = 0L, name = "Apple"))
        vm.upsertItem(newDraft(tempId = 0L, name = "Bread"))
        vm.upsertItem(newDraft(tempId = 0L, name = "Milk"))

        val items = vm.state.value.items
        val tempIds = items.map { it.tempId }
        assertThat(tempIds.toSet()).hasSize(3)
        // Each subsequent id must be strictly less than the previous (monotonic).
        for (i in 1 until tempIds.size) {
            assertThat(tempIds[i]).isLessThan(tempIds[i - 1])
        }
    }

    @Test
    fun `loading an existing bill then adding a new item does not collide with existing tempIds`() = runTest(dispatcher) {
        // Seed an existing bill with two items, mimicking what loadForEdit does.
        purchases.persisted = PurchaseWithItems(
            purchase = samplePurchase(),
            items = listOf(
                sampleItem(id = 1, purchaseId = 1, name = "Apple"),
                sampleItem(id = 2, purchaseId = 1, name = "Bread"),
            ),
        )

        vm.loadForEdit(1)
        advanceUntilIdle()

        val existingIds = vm.state.value.items.map { it.tempId }
        // The two existing items occupy -1 and -2.
        assertThat(existingIds).containsExactly(-1L, -2L).inOrder()

        // Now add a brand new item. It must not collide with either of the existing
        // tempIds (-1, -2). This is the bug fix in action.
        vm.upsertItem(newDraft(tempId = 0L, name = "Cheese"))
        advanceUntilIdle()

        val allIds = vm.state.value.items.map { it.tempId }
        assertThat(allIds.toSet()).hasSize(3)
        assertThat(allIds[2]).isLessThan(existingIds.last())
    }

    @Test
    fun `createNewGroceryItem persists with the given category and unit`() = runTest(dispatcher) {
        val result = vm.createNewGroceryItem(
            name = "Olive Oil",
            categoryId = 1L,
            unit = "L",
            iconName = "Oil",
        )
        advanceUntilIdle()

        assertThat(result.isSuccess).isTrue()
        val newId = result.getOrThrow()
        // The fake repository starts ids at 1 and increments, so the first
        // new item should be 1.
        assertThat(newId).isEqualTo(1L)
    }

    @Test
    fun `createNewGroceryItem returns failure for empty name`() = runTest(dispatcher) {
        val result = vm.createNewGroceryItem(
            name = "   ",
            categoryId = 1L,
            unit = "L",
            iconName = "Oil",
        )
        advanceUntilIdle()

        assertThat(result.isFailure).isTrue()
    }

    private fun newDraft(tempId: Long, name: String) = DraftItem(
        tempId = tempId,
        itemName = name,
        categoryId = 1L,
        quantity = 1.0,
        unit = "pcs",
        pricePerUnit = 100L,
        totalPrice = 100L,
    )

    private fun samplePurchase() = Purchase(
        id = 1,
        purchaseDate = 1_700_000_000_000L,
        shopName = "Test Mart",
        totalAmount = 200L,
        notes = null,
        receiptImageUri = null,
        createdAt = 1_700_000_000_000L,
        updatedAt = 1_700_000_000_000L,
    )

    private fun sampleItem(id: Long, purchaseId: Long, name: String) = PurchaseItem(
        id = id,
        purchaseId = purchaseId,
        itemName = name,
        groceryItemId = null,
        categoryId = 1,
        quantity = 1.0,
        unit = "pcs",
        pricePerUnit = 100L,
        totalPrice = 100L,
    )
}

private class FakePreferences(
    context: android.content.Context = mockk(relaxed = true),
) : UserPreferencesRepository(context) {
    private val flow = MutableStateFlow(
        UserPreferences(
            onboardingComplete = true,
            setupComplete = true,
            currencyCode = "USD",
            unitSystem = UnitSystem.Metric,
            themeMode = ThemeMode.System,
        ),
    )
    override val preferences: kotlinx.coroutines.flow.Flow<UserPreferences> = flow
}

private class FakeCategoryRepository : CategoryRepository {
    var categories: List<Category> = emptyList()
    override fun observeCategories() = flowOf(categories)
    override suspend fun getById(id: Long) = categories.firstOrNull { it.id == id }
    override suspend fun add(category: Category) = 0L
    override suspend fun update(category: Category) {}
    override suspend fun reorder(orderedIds: List<Long>) {}
    override suspend fun delete(id: Long) = true
}

private class FakeGroceryItemRepository : com.example.grocerymanager.domain.repository.GroceryItemRepository {
    private var nextId: Long = 1L
    val stored: MutableList<com.example.grocerymanager.domain.model.GroceryItem> = mutableListOf()
    override fun observeAll() = flowOf(stored.toList())
    override fun search(query: String, limit: Int) = flowOf(emptyList<com.example.grocerymanager.domain.model.GroceryItem>())
    override fun observeForCategory(categoryId: Long) = flowOf(emptyList<com.example.grocerymanager.domain.model.GroceryItem>())
    override suspend fun getById(id: Long) = stored.find { it.id == id }
    override suspend fun upsert(item: com.example.grocerymanager.domain.model.GroceryItem): Long {
        val newId = if (item.id == 0L) nextId++ else item.id
        stored.add(item.copy(id = newId))
        return newId
    }
    override suspend fun update(item: com.example.grocerymanager.domain.model.GroceryItem) {}
    override suspend fun delete(id: Long) = true
    override suspend fun updateLastPrice(id: Long, price: MinorUnits) {}
}

private class FakePurchaseRepository : PurchaseRepository {
    var persisted: PurchaseWithItems? = null
    private val state = MutableStateFlow<Pair<Purchase, List<PurchaseItem>>?>(null)

    override fun observeAll() = flowOf(emptyList<Purchase>())
    override fun observeAllWithItems() = flowOf(emptyList<PurchaseWithItems>())
    override fun observeRecent(start: Long, end: Long, limit: Int) = flowOf(emptyList<Purchase>())
    override fun observeBetween(start: Long, end: Long) = flowOf(emptyList<Purchase>())
    override fun observeBetweenWithItems(start: Long, end: Long) = flowOf(emptyList<PurchaseWithItems>())
    override fun observeWithItems(id: Long) = flowOf(persisted)
    override suspend fun getWithItems(id: Long) = persisted
    override fun observeSpendBetween(start: Long, end: Long) = flowOf(0L)
    override fun observeCategoryBreakdown(start: Long, end: Long) = flowOf(emptyList<com.example.grocerymanager.domain.model.CategoryTotal>())
    override fun observeShopBreakdown(start: Long, end: Long) = flowOf(emptyList<com.example.grocerymanager.domain.model.ShopTotal>())
    override fun observeTopItems(start: Long, end: Long, limit: Int) = flowOf(emptyList<com.example.grocerymanager.domain.model.TopItem>())
    override fun observeMonthlyTotals(since: Long) = flowOf(emptyList<com.example.grocerymanager.domain.model.MonthlyTotal>())
    override fun observeShopNames() = flowOf(emptyList<String>())
    override suspend fun save(purchase: Purchase, items: List<PurchaseItem>): Long {
        state.value = purchase to items
        return if (purchase.id == 0L) 99L else purchase.id
    }
    override suspend fun delete(id: Long) { state.value = null; persisted = null }
    override suspend fun totalBetween(start: Long, end: Long) = 0L
}
