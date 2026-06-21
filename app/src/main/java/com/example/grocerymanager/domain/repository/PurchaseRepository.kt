package com.example.grocerymanager.domain.repository

import kotlinx.coroutines.flow.Flow
import com.example.grocerymanager.core.common.MinorUnits
import com.example.grocerymanager.domain.model.Purchase
import com.example.grocerymanager.domain.model.PurchaseItem
import com.example.grocerymanager.domain.model.PurchaseWithItems
import com.example.grocerymanager.domain.model.ShopTotal
import com.example.grocerymanager.domain.model.MonthlyTotal
import com.example.grocerymanager.domain.model.TopItem
import com.example.grocerymanager.domain.model.CategoryTotal

/**
 * Read/write access to the user's purchase history. Implementations live in
 * `data.repository` and are bound in `RepositoryModule`.
 */
interface PurchaseRepository {
    fun observeAll(): Flow<List<Purchase>>
    fun observeAllWithItems(): Flow<List<PurchaseWithItems>>
    fun observeRecent(start: Long, end: Long, limit: Int): Flow<List<Purchase>>
    fun observeBetween(start: Long, end: Long): Flow<List<Purchase>>
    /**
     * Range-filtered purchase stream with line items joined in a single
     * Room transaction. Use this when the UI needs the items (e.g. the
     * Records screen's inline item preview) so the card can render in one
     * pass instead of N+1 round-trips.
     */
    fun observeBetweenWithItems(start: Long, end: Long): Flow<List<PurchaseWithItems>>
    fun observeWithItems(id: Long): Flow<PurchaseWithItems?>
    suspend fun getWithItems(id: Long): PurchaseWithItems?
    fun observeSpendBetween(start: Long, end: Long): Flow<MinorUnits>
    fun observeCategoryBreakdown(start: Long, end: Long): Flow<List<CategoryTotal>>
    fun observeShopBreakdown(start: Long, end: Long): Flow<List<ShopTotal>>
    fun observeTopItems(start: Long, end: Long, limit: Int = 10): Flow<List<TopItem>>
    fun observeMonthlyTotals(since: Long): Flow<List<MonthlyTotal>>
    fun observeShopNames(): Flow<List<String>>
    suspend fun save(purchase: Purchase, items: List<PurchaseItem>): Long
    suspend fun delete(id: Long)
    suspend fun totalBetween(start: Long, end: Long): MinorUnits
}
