package com.example.grocerymanager.domain.usecase

import com.example.grocerymanager.core.common.DateUtils
import com.example.grocerymanager.core.common.MinorUnits
import com.example.grocerymanager.domain.model.Purchase
import com.example.grocerymanager.domain.model.PurchaseItem
import com.example.grocerymanager.domain.model.PurchaseWithItems
import com.example.grocerymanager.domain.repository.GroceryItemRepository
import com.example.grocerymanager.domain.repository.PurchaseRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavePurchaseUseCase @Inject constructor(
    private val purchases: PurchaseRepository,
    private val groceryItems: GroceryItemRepository,
) {
    /**
     * Saves a purchase and its items atomically. The same use case handles
     * both insert and update — the underlying DAO treats [Purchase.id] == 0
     * as a new purchase and replaces the items list otherwise.
     *
     * Returns [Result.success] with the purchase id, or [Result.failure] if
     * validation fails or the database write throws.
     *
     * Note on the `totalAmount > 0` guard: this use case is the **final save**
     * path used by [com.example.grocerymanager.feature.addpurchase.AddPurchaseViewModel].
     * The shopping-list-to-purchase converter
     * ([com.example.grocerymanager.domain.usecase.ConvertShoppingListToPurchaseUseCase])
     * intentionally persists a zero-total **draft** directly via
     * [com.example.grocerymanager.domain.repository.PurchaseRepository.save] —
     * it bypasses this use case so the user can edit prices before the final
     * save. The two paths are intentionally different.
     */
    suspend operator fun invoke(
        purchase: Purchase,
        items: List<PurchaseItem>,
    ): Result<Long> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("At least one item is required"))
        }
        if (purchase.totalAmount <= 0L) {
            return Result.failure(IllegalArgumentException("Total must be greater than zero"))
        }
        return runCatching {
            val id = purchases.save(purchase, items)
            // Only refresh the "last price" hint when this is a new bill so
            // editing an old receipt never silently overwrites the price the
            // user has most recently seen for the item.
            if (purchase.id == 0L) {
                items.forEach { item ->
                    item.groceryItemId?.let { groceryItems.updateLastPrice(it, item.pricePerUnit ?: item.totalPrice) }
                }
            }
            id
        }
    }
}

class DeletePurchaseUseCase @Inject constructor(
    private val purchases: PurchaseRepository,
) {
    suspend operator fun invoke(id: Long) = purchases.delete(id)
}

class GetPurchaseWithItemsUseCase @Inject constructor(
    private val purchases: PurchaseRepository,
) {
    suspend operator fun invoke(id: Long): PurchaseWithItems? = purchases.getWithItems(id)
    fun observe(id: Long): Flow<PurchaseWithItems?> = purchases.observeWithItems(id)
}

/**
 * Single source of truth for "spend in a date range". Replaces the three
 * thin wrappers `GetTodaySpendingUseCase`, `GetWeeklySpendingUseCase`,
 * `GetMonthlySummaryUseCase` (A2).
 */
class ObserveSpendInRangeUseCase @Inject constructor(
    private val purchases: PurchaseRepository,
) {
    operator fun invoke(start: Long, end: Long): Flow<MinorUnits> =
        purchases.observeSpendBetween(start, end)
}

class GetCategoryBreakdownUseCase @Inject constructor(
    private val purchases: PurchaseRepository,
) {
    fun observe(start: Long, end: Long) = purchases.observeCategoryBreakdown(start, end)
}

class GetTopItemsUseCase @Inject constructor(
    private val purchases: PurchaseRepository,
) {
    fun observe(start: Long, end: Long, limit: Int = 10) =
        purchases.observeTopItems(start, end, limit)
}

class GetRecentPurchasesUseCase @Inject constructor(
    private val purchases: PurchaseRepository,
) {
    /**
     * Streams the most recent [limit] purchases including their line items.
     *
     * Returning [PurchaseWithItems] (instead of bare [Purchase]) lets the
     * Home screen render an inline item preview on each card without an
     * extra round-trip per row — the Room `@Transaction`-backed
     * `observeAllWithItems()` query already hydrates the items in a single
     * statement.
     */
    fun observe(limit: Int = 5): Flow<List<PurchaseWithItems>> =
        purchases.observeAllWithItems().map { list -> list.take(limit) }
}

class GetPriceHistoryUseCase @Inject constructor(
    private val priceHistory: com.example.grocerymanager.domain.repository.PriceHistoryRepository,
) {
    fun observe(name: String) = priceHistory.observePriceHistory(name)
}
