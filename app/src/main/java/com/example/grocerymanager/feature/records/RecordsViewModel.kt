package com.example.grocerymanager.feature.records

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerymanager.core.common.DateFormat
import com.example.grocerymanager.core.common.DateUtils
import com.example.grocerymanager.core.common.DateRange as CommonDateRange
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.model.PurchaseWithItems
import com.example.grocerymanager.domain.repository.CategoryRepository
import com.example.grocerymanager.domain.repository.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
enum class RecordsFilter { Today, Week, Month, LastMonth, All }

@Immutable
data class RecordsUiState(
    val currencyCode: String = "USD",
    val filter: RecordsFilter = RecordsFilter.Month,
    val query: String = "",
    val groups: List<DayGroup> = emptyList(),
    val totalInRange: Long = 0L,
    /**
     * Full category catalog so the Records cards can render the dynamic
     * per-category icons and the inline item-preview color dots. Loaded
     * from [CategoryRepository.observeCategories] and combined into the
     * single state emission — no extra screen-level wiring required.
     */
    val categories: List<Category> = emptyList(),
)

@Immutable
data class DayGroup(
    val label: String,
    /**
     * Each purchase carries its line items so the Records cards can show
     * the same inline item preview the Home screen does. Costs nothing
     * extra — the underlying DAO uses a `@Transaction`-backed join.
     */
    val purchases: List<PurchaseWithItems>,
    val total: Long,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val purchases: PurchaseRepository,
    private val categoriesRepo: CategoryRepository,
) : ViewModel() {

    private val filter = MutableStateFlow(RecordsFilter.Month)
    private val query = MutableStateFlow("")

    /**
     * Single combine: prefs + filter + query + purchases + categories.
     * The list is flattened through `flatMapLatest` so flipping the
     * filter cancels the previous DB read. Categories are folded in at
     * the very end so the recent-purchase / records cards always have
     * the latest category palette without a separate state hoist.
     */
    val state: StateFlow<RecordsUiState> = combine(
        preferences.preferences,
        filter,
        query,
    ) { prefs, f, q -> SnapshotKey(prefs.currencyCode, f, q) }
        .flatMapLatest { key ->
            purchases.observeBetweenWithItems(rangeFor(key.filter).startInclusive, rangeFor(key.filter).endInclusive)
                .combine(filter) { list, _ -> list to key }
        }
        .combine(preferences.preferences) { (list, key), prefs ->
            val q = key.query
            val filtered = if (q.isBlank()) list else list.filter { p ->
                (p.purchase.shopName ?: "").contains(q, ignoreCase = true) ||
                    p.purchase.notes?.contains(q, ignoreCase = true) == true
            }
            // Build a skeleton state here so the categories emission can
            // be merged in the final combine below without re-running
            // the search/grouping logic.
            val skeleton = RecordsUiState(
                currencyCode = prefs.currencyCode,
                filter = key.filter,
                query = q,
                groups = groupByDay(filtered),
                totalInRange = filtered.sumOf { it.purchase.totalAmount },
            )
            skeleton
        }
        .combine(categoriesRepo.observeCategories()) { ui, categories ->
            ui.copy(categories = categories)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            RecordsUiState(),
        )

    fun setFilter(value: RecordsFilter) { filter.value = value }
    fun setQuery(value: String) { query.value = value }

    /** Cycle to the next filter — used by the localized chip row where string keys are not stable. */
    fun toggleFilter() {
        val all = RecordsFilter.entries
        val idx = all.indexOf(filter.value)
        filter.value = all[(idx + 1) % all.size]
    }

    private fun rangeFor(filter: RecordsFilter): CommonDateRange {
        val now = DateUtils.now()
        return when (filter) {
            RecordsFilter.Today -> DateUtils.today(now)
            RecordsFilter.Week -> DateUtils.thisWeek(now)
            RecordsFilter.Month -> DateUtils.thisMonth(now)
            RecordsFilter.LastMonth -> DateUtils.previousMonth(now)
            RecordsFilter.All -> CommonDateRange(Long.MIN_VALUE / 2, Long.MAX_VALUE / 2)
        }
    }

    private fun groupByDay(purchases: List<PurchaseWithItems>): List<DayGroup> {
        return purchases
            .sortedByDescending { it.purchase.purchaseDate }
            .groupBy { DateUtils.startOfDay(it.purchase.purchaseDate) }
            .map { (dayStart, list) ->
                val label = DateFormat.dayHeader(dayStart)
                val total = list.sumOf { it.purchase.totalAmount }
                DayGroup(label, list, total)
            }
    }

    @Immutable
    private data class SnapshotKey(
        val currencyCode: String,
        val filter: RecordsFilter,
        val query: String,
    )
}
