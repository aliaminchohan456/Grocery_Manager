package com.example.grocerymanager.data.repository

import androidx.room.withTransaction
import com.example.grocerymanager.data.local.GroceryDatabase
import com.example.grocerymanager.data.local.dao.StoreDao
import com.example.grocerymanager.data.local.entity.StoreEntity
import com.example.grocerymanager.data.mapper.toDomain
import com.example.grocerymanager.domain.model.Store
import com.example.grocerymanager.domain.repository.StoreRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class StoreRepositoryImpl @Inject constructor(
    private val dao: StoreDao,
    private val database: GroceryDatabase,
) : StoreRepository {
    override fun observeAll(): Flow<List<Store>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun search(query: String, limit: Int): Flow<List<Store>> =
        dao.search(query, limit).map { list -> list.map { it.toDomain() } }

    override suspend fun getByName(name: String): Store? = dao.getByName(name)?.toDomain()

    override suspend fun recordVisit(name: String) {
        // Serialize get-then-write so a concurrent visit to a brand-new
        // store can't both miss `getByName`, both insert, and lose one
        // visitCount.
        database.withTransaction {
            val now = System.currentTimeMillis()
            val existing = dao.getByName(name)
            if (existing == null) {
                dao.insert(StoreEntity(name = name, visitCount = 1, lastVisitAt = now, createdAt = now))
            } else {
                dao.bumpVisit(existing.id, now)
            }
        }
    }
}
