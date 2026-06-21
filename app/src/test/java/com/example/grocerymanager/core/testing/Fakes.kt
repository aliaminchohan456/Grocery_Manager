package com.example.grocerymanager.core.testing

import com.example.grocerymanager.core.common.MinorUnits
import com.example.grocerymanager.domain.model.Budget
import com.example.grocerymanager.domain.model.CategoryTotal
import com.example.grocerymanager.domain.model.MonthlyTotal
import com.example.grocerymanager.domain.model.Purchase
import com.example.grocerymanager.domain.repository.BudgetRepository
import com.example.grocerymanager.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeBudgetRepository : BudgetRepository {
    var stored: Budget? = null
    override fun observeForMonth(month: Int, year: Int): Flow<Budget?> = flowOf(stored)
    override fun observeRecent(limit: Int): Flow<List<Budget>> = flowOf(stored?.let { listOf(it) } ?: emptyList())
    override suspend fun getForMonth(month: Int, year: Int): Budget? = stored
    override suspend fun upsert(budget: Budget): Long {
        stored = budget
        return budget.id
    }
    override suspend fun delete(id: Long) { stored = null }
}

class FakePurchaseRepository(
    private val spend: MinorUnits = 0L,
) : PurchaseRepository {
    override fun observeAll(): Flow<List<Purchase>> = flowOf(emptyList())
    override fun observeAllWithItems(): Flow<List<com.example.grocerymanager.domain.model.PurchaseWithItems>> = flowOf(emptyList())
    override fun observeRecent(start: Long, end: Long, limit: Int): Flow<List<Purchase>> = flowOf(emptyList())
    override fun observeBetween(start: Long, end: Long): Flow<List<Purchase>> = flowOf(emptyList())
    override fun observeBetweenWithItems(start: Long, end: Long): Flow<List<com.example.grocerymanager.domain.model.PurchaseWithItems>> = flowOf(emptyList())
    override fun observeWithItems(id: Long): Flow<com.example.grocerymanager.domain.model.PurchaseWithItems?> = flowOf(null)
    override suspend fun getWithItems(id: Long): com.example.grocerymanager.domain.model.PurchaseWithItems? = null
    override fun observeSpendBetween(start: Long, end: Long): Flow<MinorUnits> = flowOf(spend)
    override fun observeCategoryBreakdown(start: Long, end: Long): Flow<List<CategoryTotal>> = flowOf(emptyList())
    override fun observeShopBreakdown(start: Long, end: Long) = flowOf(emptyList<com.example.grocerymanager.domain.model.ShopTotal>())
    override fun observeTopItems(start: Long, end: Long, limit: Int) = flowOf(emptyList<com.example.grocerymanager.domain.model.TopItem>())
    override fun observeMonthlyTotals(since: Long): Flow<List<MonthlyTotal>> = flowOf(emptyList())
    override fun observeShopNames(): Flow<List<String>> = flowOf(emptyList())
    override suspend fun save(purchase: Purchase, items: List<com.example.grocerymanager.domain.model.PurchaseItem>): Long = purchase.id
    override suspend fun delete(id: Long) {}
    override suspend fun totalBetween(start: Long, end: Long): MinorUnits = spend
}
