package com.example.grocerymanager.feature.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerymanager.core.common.DateFormat
import com.example.grocerymanager.core.common.DateUtils
import com.example.grocerymanager.core.common.Greeting
import com.example.grocerymanager.core.common.GreetingResolver
import com.example.grocerymanager.core.common.MinorUnits
import com.example.grocerymanager.core.common.tickerFlow
import com.example.grocerymanager.core.preferences.UserPreferences
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.example.grocerymanager.domain.model.BudgetStatus
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.model.CategoryTotal
import com.example.grocerymanager.domain.model.PurchaseWithItems
import com.example.grocerymanager.domain.repository.CategoryRepository
import com.example.grocerymanager.domain.repository.PurchaseRepository
import com.example.grocerymanager.domain.usecase.GetBudgetStatusUseCase
import com.example.grocerymanager.domain.usecase.GetRecentPurchasesUseCase
import com.example.grocerymanager.domain.usecase.ObserveSpendInRangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class HomeUiState(
    val currencyCode: String = "USD",
    val today: MinorUnits = 0L,
    val week: MinorUnits = 0L,
    val month: MinorUnits = 0L,
    val budgetStatus: BudgetStatus? = null,
    /**
     * Recent purchases including their line items, so the Home cards can
     * render an inline item preview without an extra DB round-trip. The
     * number of items per purchase is intentionally unbounded here — the
     * card is responsible for smart-truncating to the first 2–3 entries.
     */
    val recent: List<PurchaseWithItems> = emptyList(),
    val topCategory: TopCategory? = null,
    val categories: List<Category> = emptyList(),
    val categoryBreakdown: List<CategoryTotal> = emptyList(),
    val greeting: Greeting = Greeting.Hello,
    val currentMonthShort: String = "",
)

// Re-export the shared Greeting enum so existing references in the screen
// (`Greeting.Morning`, `Greeting.Afternoon`, …) keep compiling without a
// wide import change. The single source of truth is `core.common.Greeting`.
typealias Greeting = com.example.grocerymanager.core.common.Greeting

data class TopCategory(val category: Category, val total: MinorUnits, val percent: Float)

/**
 * Intermediate data carriers for the typed `combine` chain. Kept private
 * to the ViewModel so the only public emission is [HomeUiState]. Each
 * type is `@Immutable` so the framework can do stable-equality checks
 * without defensive copying.
 */
@Immutable private data class HomeSpending(
    val today: MinorUnits,
    val week: MinorUnits,
    val month: MinorUnits,
    val recent: List<PurchaseWithItems>,
)

@Immutable private data class HomeBudgetAndCategories(
    val status: BudgetStatus,
    val categories: List<Category>,
    val breakdown: List<CategoryTotal>,
)

@Immutable private data class HomeDataSources(
    val prefs: UserPreferences,
    val spending: HomeSpending,
    val budgetCategories: HomeBudgetAndCategories,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val categoriesRepo: CategoryRepository,
    private val purchaseRepo: PurchaseRepository,
    private val observeSpend: ObserveSpendInRangeUseCase,
    private val getRecent: GetRecentPurchasesUseCase,
    private val getBudgetStatus: GetBudgetStatusUseCase,
) : ViewModel() {

    /**
     * Source flow that combines every upstream once-per-emit. Replaced the
     * previous `combine(values: Array<*>)` 8-way overload (which used
     * `@Suppress("UNCHECKED_CAST")` casts) with two typed combines and a
     * stable fold. Splitting the time-dependent `greeting` / `currentMonthShort`
     * into a separate ticker flow also means a 1×/min wall-clock tick no
     * longer forces every other upstream to re-emit `HomeUiState`.
     */
    private val dataSources: Flow<HomeDataSources> = combine(
        preferences.preferences,
        combine(
            observeSpend(DateUtils.today().startInclusive, DateUtils.today().endInclusive),
            observeSpend(DateUtils.thisWeek().startInclusive, DateUtils.thisWeek().endInclusive),
            observeSpend(DateUtils.thisMonth().startInclusive, DateUtils.thisMonth().endInclusive),
            getRecent.observe(5),
        ) { today, week, month, recent -> HomeSpending(today, week, month, recent) },
        combine(
            getBudgetStatus.observe(),
            categoriesRepo.observeCategories(),
            purchaseRepo.observeCategoryBreakdown(
                DateUtils.thisMonth().startInclusive,
                DateUtils.thisMonth().endInclusive,
            ),
        ) { status, categories, breakdown ->
            HomeBudgetAndCategories(status, categories, breakdown)
        },
    ) { prefs, spending, budgetCategories ->
        HomeDataSources(prefs, spending, budgetCategories)
    }

    private val ticker: Flow<Long> = tickerFlow(intervalMillis = 60_000L).map { System.currentTimeMillis() }

    val state: StateFlow<HomeUiState> = combine(dataSources, ticker) { data, _ ->
        val greeting = GreetingResolver.current()
        val monthShort = DateFormat.monthShort(System.currentTimeMillis())
        val top = topCategory(data.budgetCategories.breakdown, data.budgetCategories.categories, data.spending.month)
        HomeUiState(
            currencyCode = data.prefs.currencyCode,
            today = data.spending.today,
            week = data.spending.week,
            month = data.spending.month,
            budgetStatus = data.budgetCategories.status,
            recent = data.spending.recent,
            topCategory = top,
            categories = data.budgetCategories.categories,
            categoryBreakdown = data.budgetCategories.breakdown,
            greeting = greeting,
            currentMonthShort = monthShort,
        )
    }.distinctUntilChanged().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState(),
    )

    private fun topCategory(
        breakdown: List<CategoryTotal>,
        cats: List<Category>,
        monthTotal: MinorUnits,
    ): TopCategory? {
        val top = breakdown.firstOrNull() ?: return null
        val cat = cats.firstOrNull { it.id == top.categoryId } ?: return null
        val percent = if (monthTotal > 0) (top.total.toFloat() / monthTotal.toFloat()) else 0f
        return TopCategory(cat, top.total, percent)
    }
}
