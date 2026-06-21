package com.example.grocerymanager.data.repository

import com.example.grocerymanager.core.common.MinorUnits
import com.example.grocerymanager.data.local.dao.PurchaseDao
import com.example.grocerymanager.data.mapper.toDomain
import com.example.grocerymanager.data.mapper.toEntity
import com.example.grocerymanager.domain.model.Purchase
import com.example.grocerymanager.domain.model.PurchaseItem
import com.example.grocerymanager.domain.model.PurchaseWithItems
import com.example.grocerymanager.domain.repository.PurchaseRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PurchaseRepositoryImpl @Inject constructor(
    private val dao: PurchaseDao,
) : PurchaseRepository {

    override fun observeAll(): Flow<List<Purchase>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeAllWithItems(): Flow<List<PurchaseWithItems>> =
        dao.observeAllWithItems().map { list -> list.map { it.toDomain() } }

    override fun observeRecent(start: Long, end: Long, limit: Int): Flow<List<Purchase>> =
        dao.observeRecent(start, end, limit).map { list -> list.map { it.toDomain() } }

    override fun observeBetween(start: Long, end: Long): Flow<List<Purchase>> =
        dao.observeBetween(start, end).map { list -> list.map { it.toDomain() } }

    override fun observeBetweenWithItems(start: Long, end: Long): Flow<List<PurchaseWithItems>> =
        dao.observeBetweenWithItems(start, end).map { list -> list.map { it.toDomain() } }

    override fun observeWithItems(id: Long): Flow<PurchaseWithItems?> =
        dao.observeWithItems(id).map { it?.toDomain() }

    override suspend fun getWithItems(id: Long): PurchaseWithItems? =
        dao.getWithItems(id)?.toDomain()

    override fun observeSpendBetween(start: Long, end: Long): Flow<MinorUnits> =
        dao.observeSpendBetween(start, end)

    override fun observeCategoryBreakdown(start: Long, end: Long): Flow<List<com.example.grocerymanager.domain.model.CategoryTotal>> =
        dao.observeCategoryBreakdown(start, end).map { list -> list.map { it.toDomain() } }

    override fun observeShopBreakdown(start: Long, end: Long): Flow<List<com.example.grocerymanager.domain.model.ShopTotal>> =
        dao.observeShopBreakdown(start, end).map { list -> list.map { it.toDomain() } }

    override fun observeTopItems(start: Long, end: Long, limit: Int): Flow<List<com.example.grocerymanager.domain.model.TopItem>> =
        dao.observeTopItems(start, end, limit).map { list -> list.map { it.toDomain() } }

    override fun observeMonthlyTotals(since: Long): Flow<List<com.example.grocerymanager.domain.model.MonthlyTotal>> =
        dao.observeMonthlyTotals(since).map { list -> list.map { it.toDomain() } }

    override fun observeShopNames(): Flow<List<String>> = dao.observeShopNames()

    override suspend fun save(purchase: Purchase, items: List<PurchaseItem>): Long {
        val now = System.currentTimeMillis()
        val entity = (if (purchase.id == 0L) purchase.copy(createdAt = now, updatedAt = now) else purchase.copy(updatedAt = now)).toEntity()
        val itemEntities = items.map { it.toEntity() }
        return dao.savePurchaseWithItems(entity, itemEntities)
    }

    override suspend fun delete(id: Long) {
        dao.deletePurchaseById(id)
    }

    override suspend fun totalBetween(start: Long, end: Long): MinorUnits =
        dao.totalBetween(start, end)
}
