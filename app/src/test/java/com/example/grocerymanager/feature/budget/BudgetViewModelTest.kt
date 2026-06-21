package com.example.grocerymanager.feature.budget

import com.example.grocerymanager.core.common.MoneyUtils
import com.example.grocerymanager.core.preferences.ThemeMode
import com.example.grocerymanager.core.preferences.UnitSystem
import com.example.grocerymanager.core.preferences.UserPreferences
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.example.grocerymanager.domain.model.Budget
import com.example.grocerymanager.domain.model.CategoryTotal
import com.example.grocerymanager.domain.model.MonthlyTotal
import com.example.grocerymanager.domain.model.Purchase
import com.example.grocerymanager.domain.model.PurchaseItem
import com.example.grocerymanager.domain.model.PurchaseWithItems
import com.example.grocerymanager.domain.model.ShopTotal
import com.example.grocerymanager.domain.model.TopItem
import com.example.grocerymanager.domain.repository.BudgetRepository
import com.example.grocerymanager.domain.repository.PurchaseRepository
import com.example.grocerymanager.domain.usecase.GetBudgetStatusUseCase
import com.example.grocerymanager.domain.usecase.SetMonthlyBudgetUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
class BudgetViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `openEditor seeds amount and threshold from current budget`() = runTest(dispatcher) {
        // Compute month/year *inside* the test so a slow class load can't
        // cause the VM to see a different month.
        val now = com.example.grocerymanager.core.common.DateUtils.now()
        val (month, year) = com.example.grocerymanager.core.common.DateUtils.monthAndYear(now)
        val prefs = FakePrefs()
        val repo = FakeBudgetRepo(current = Budget(1, month, year, 10_000L, 0.7f))
        val vm = BudgetViewModel(
            preferences = prefs,
            budgetRepo = repo,
            setBudget = SetMonthlyBudgetUseCase(repo),
            getStatus = GetBudgetStatusUseCase(repo, FakePurchaseRepo(0L)),
        )
        advanceUntilIdle()
        val initial = vm.state.value
        assertThat(initial.status?.budget).isNotNull()

        vm.openEditor()
        advanceUntilIdle()

        val s = vm.state.value
        assertThat(s.showEditor).isTrue()
        assertThat(s.editorAmount).isEqualTo(MoneyUtils.toMajorUnits(10_000L).toPlainString())
        assertThat(s.editorThreshold).isEqualTo(0.7f)
    }

    @Test
    fun `clearBudget resets editor amount and threshold to defaults (B19)`() = runTest(dispatcher) {
        val now = com.example.grocerymanager.core.common.DateUtils.now()
        val (month, year) = com.example.grocerymanager.core.common.DateUtils.monthAndYear(now)
        val prefs = FakePrefs()
        val repo = FakeBudgetRepo(current = Budget(1, month, year, 10_000L, 0.7f))
        val vm = BudgetViewModel(
            preferences = prefs,
            budgetRepo = repo,
            setBudget = SetMonthlyBudgetUseCase(repo),
            getStatus = GetBudgetStatusUseCase(repo, FakePurchaseRepo(0L)),
        )
        advanceUntilIdle()

        vm.openEditor()
        advanceUntilIdle()
        assertThat(vm.state.value.editorAmount).isNotEmpty()

        vm.clearBudget()
        advanceUntilIdle()

        val s = vm.state.value
        assertThat(s.showEditor).isFalse()
        assertThat(s.editorAmount).isEmpty()
        assertThat(s.editorThreshold).isEqualTo(0.8f)
    }
}

private class FakePrefs : UserPreferencesRepository(context = mockk(relaxed = true)) {
    private val flow = MutableStateFlow(
        UserPreferences(
            setupComplete = true,
            currencyCode = "USD",
            unitSystem = UnitSystem.Metric,
            themeMode = ThemeMode.System,
        ),
    )
    override val preferences: Flow<UserPreferences> = flow
}

private class FakeBudgetRepo(
    private val current: Budget? = null,
) : BudgetRepository {
    private val state = MutableStateFlow<Budget?>(current)
    override fun observeForMonth(month: Int, year: Int): Flow<Budget?> = state
    override fun observeRecent(limit: Int): Flow<List<Budget>> = flowOf(state.value?.let { listOf(it) } ?: emptyList())
    override suspend fun getForMonth(month: Int, year: Int): Budget? = state.value
    override suspend fun upsert(budget: Budget): Long {
        state.value = budget
        return budget.id
    }
    override suspend fun delete(id: Long) { state.value = null }
}

private class FakePurchaseRepo(private val spend: Long) : PurchaseRepository {
    override fun observeAll(): Flow<List<Purchase>> = flowOf(emptyList())
    override fun observeAllWithItems(): Flow<List<PurchaseWithItems>> = flowOf(emptyList())
    override fun observeRecent(start: Long, end: Long, limit: Int): Flow<List<Purchase>> = flowOf(emptyList())
    override fun observeBetween(start: Long, end: Long): Flow<List<Purchase>> = flowOf(emptyList())
    override fun observeBetweenWithItems(start: Long, end: Long): Flow<List<PurchaseWithItems>> = flowOf(emptyList())
    override fun observeWithItems(id: Long): Flow<PurchaseWithItems?> = flowOf(null)
    override suspend fun getWithItems(id: Long): PurchaseWithItems? = null
    override fun observeSpendBetween(start: Long, end: Long): Flow<Long> = flowOf(spend)
    override fun observeCategoryBreakdown(start: Long, end: Long): Flow<List<CategoryTotal>> = flowOf(emptyList())
    override fun observeShopBreakdown(start: Long, end: Long) = flowOf(emptyList<ShopTotal>())
    override fun observeTopItems(start: Long, end: Long, limit: Int) = flowOf(emptyList<TopItem>())
    override fun observeMonthlyTotals(since: Long): Flow<List<MonthlyTotal>> = flowOf(emptyList())
    override fun observeShopNames(): Flow<List<String>> = flowOf(emptyList())
    override suspend fun save(purchase: Purchase, items: List<PurchaseItem>): Long = purchase.id
    override suspend fun delete(id: Long) {}
    override suspend fun totalBetween(start: Long, end: Long): Long = spend
}
