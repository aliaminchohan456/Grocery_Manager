package com.example.grocerymanager.data.repository

import androidx.room.withTransaction
import com.example.grocerymanager.core.common.MinorUnits
import com.example.grocerymanager.data.local.GroceryDatabase
import com.example.grocerymanager.data.local.dao.GroceryItemDao
import com.example.grocerymanager.data.mapper.toDomain
import com.example.grocerymanager.data.mapper.toEntity
import com.example.grocerymanager.domain.model.GroceryItem
import com.example.grocerymanager.domain.repository.GroceryItemRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class GroceryItemRepositoryImpl @Inject constructor(
    private val dao: GroceryItemDao,
    private val database: GroceryDatabase,
) : GroceryItemRepository {
    override fun observeAll(): Flow<List<GroceryItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun search(query: String, limit: Int): Flow<List<GroceryItem>> =
        dao.search(query, limit).map { list -> list.map { it.toDomain() } }

    override fun observeForCategory(categoryId: Long): Flow<List<GroceryItem>> =
        dao.observeForCategory(categoryId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): GroceryItem? = dao.getById(id)?.toDomain()

    override suspend fun upsert(item: GroceryItem): Long {
        // Serialize the get-then-write so two concurrent upserts of a new
        // item can't both miss the existing row and then race on the unique
        // name index (which would cause REPLACE to delete-then-reinsert the
        // loser — silent data loss).
        return database.withTransaction {
            val now = System.currentTimeMillis()
            val existing = if (item.id != 0L) dao.getById(item.id) else null
            val entity = item.toEntity(existing?.createdAt ?: now)
            dao.upsert(entity)
        }
    }

    override suspend fun update(item: GroceryItem) {
        val existing = dao.getById(item.id) ?: return
        dao.update(item.toEntity(existing.createdAt))
    }

    override suspend fun delete(id: Long): Boolean = dao.delete(id) > 0

    override suspend fun updateLastPrice(id: Long, price: MinorUnits) {
        dao.updateLastPrice(id, price, System.currentTimeMillis())
    }
}
