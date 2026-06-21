package com.example.grocerymanager.feature.insights

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerymanager.core.common.DateUtils
import com.example.grocerymanager.core.common.MinorUnits
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.model.MonthlyTotal
import com.example.grocerymanager.domain.model.TopItem
import com.example.grocerymanager.domain.repository.CategoryRepository
import com.example.grocerymanager.domain.repository.PurchaseRepository
import com.example.grocerymanager.domain.usecase.GetCategoryBreakdownUseCase
import com.example.grocerymanager.domain.usecase.GetTopItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@Immutable
data class CategorySlice(
    val category: Category,
    val total: MinorUnits,
    val percent: Float,
)

/** A pre-computed share so the screen can render without any extra math. */
@Immutable
data class TopItemShare(
    val item: com.example.grocerymanager.domain.model.TopItem,
    val share: Float,
)

@Immutable
data class InsightsUiState(
    val currencyCode: String = "USD",
    val month: MinorUnits = 0L,
    val previousMonth: MinorUnits = 0L,
    val monthChangePercent: Float = 0f,
    val categorySlices: List<CategorySlice> = emptyList(),
    val topItems: List<TopItemShare> = emptyList(),
    val monthlyTotals: List<MonthlyTotal> = emptyList(),
    val hasData: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val categoriesRepo: CategoryRepository,
    private val purchases: PurchaseRepository,
    private val getCategoryBreakdown: GetCategoryBreakdownUseCase,
    private val getTopItems: GetTopItemsUseCase,
) : ViewModel() {

    val state: StateFlow<InsightsUiState> = preferences.preferences.flatMapLatest { prefs ->
        val monthRange = DateUtils.thisMonth()
        val prevRange = DateUtils.previousMonth()
        // Use the use cases (A1) for the two domain-level aggregations.
        val monthBreakdown = getCategoryBreakdown.observe(monthRange.startInclusive, monthRange.endInclusive)
        val prevTotal = purchases.observeSpendBetween(prevRange.startInclusive, prevRange.endInclusive)
        val monthTotal = purchases.observeSpendBetween(monthRange.startInclusive, monthRange.endInclusive)
        val topItems = getTopItems.observe(monthRange.startInclusive, monthRange.endInclusive, 8)
        // monthlyTotals is currently a one-shot raw SQL aggregate not yet
        // wrapped in a use case; keep the direct repo call for it (P8:
        // the use case layer can grow a `GetMonthlyTotalsUseCase` when
        // a second screen needs it).
        val monthly = purchases.observeMonthlyTotals(monthsAgoTimestamp(6))
        val cats = categoriesRepo.observeCategories()
        combine(
            combine(monthBreakdown, monthTotal, prevTotal, ::Triple),
            combine(topItems, monthly, cats, ::Triple),
        ) { totals, lists ->
            val (breakdown, month, prev) = totals
            val (top, monthlyList, categories) = lists
            val total = month
            val slices = breakdown.mapNotNull { ct ->
                val cat = categories.firstOrNull { it.id == ct.categoryId } ?: return@mapNotNull null
                CategorySlice(
                    category = cat,
                    total = ct.total,
                    percent = if (total > 0) ct.total.toFloat() / total.toFloat() else 0f,
                )
            }.sortedByDescending { it.total }
            val change = if (prev > 0) ((month - prev).toFloat() / prev.toFloat()) * 100f else 0f
            // B2: top-item share must be of monthly spend, not of the top-items
            // list. We pre-compute the share here so the screen never has to
            // re-divide (and so the unit tests can assert on the exact number).
            val safeMonth = month.coerceAtLeast(1L)
            val sharedTop = top.map { ti ->
                TopItemShare(item = ti, share = ti.totalSpent.toFloat() / safeMonth.toFloat())
            }
            InsightsUiState(
                currencyCode = prefs.currencyCode,
                month = month,
                previousMonth = prev,
                monthChangePercent = change,
                categorySlices = slices,
                topItems = sharedTop,
                monthlyTotals = monthlyList,
                hasData = slices.isNotEmpty() || top.isNotEmpty(),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

    private fun monthsAgoTimestamp(months: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -months)
        return cal.timeInMillis
    }
}
