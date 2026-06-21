package com.example.grocerymanager.data.repository

import com.example.grocerymanager.data.local.dao.ShoppingListDao
import com.example.grocerymanager.data.mapper.toDomain
import com.example.grocerymanager.data.mapper.toEntity
import com.example.grocerymanager.domain.model.ShoppingListItem
import com.example.grocerymanager.domain.repository.ShoppingListRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ShoppingListRepositoryImpl @Inject constructor(
    private val dao: ShoppingListDao,
) : ShoppingListRepository {
    override fun observeAll(): Flow<List<ShoppingListItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observePending(): Flow<List<ShoppingListItem>> =
        dao.observePending().map { list -> list.map { it.toDomain() } }

    override suspend fun add(item: ShoppingListItem): Long {
        val now = System.currentTimeMillis()
        val entity = item.copy(id = 0).toEntity(now)
        return dao.insert(entity)
    }

    override suspend fun update(item: ShoppingListItem) {
        val now = System.currentTimeMillis()
        dao.update(item.toEntity(now))
    }

    override suspend fun delete(id: Long) { dao.deleteByIds(listOf(id)) }

    override suspend fun togglePurchased(item: ShoppingListItem) {
        val now = System.currentTimeMillis()
        val toggled = if (item.isPurchased) {
            item.copy(isPurchased = false, purchasedAt = null)
        } else {
            item.copy(isPurchased = true, purchasedAt = now)
        }
        dao.update(toggled.toEntity(now))
    }
}
