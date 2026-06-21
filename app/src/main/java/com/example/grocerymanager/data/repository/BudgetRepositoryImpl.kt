package com.example.grocerymanager.data.repository

import androidx.room.withTransaction
import com.example.grocerymanager.data.local.GroceryDatabase
import com.example.grocerymanager.data.local.dao.BudgetDao
import com.example.grocerymanager.data.local.entity.BudgetEntity
import com.example.grocerymanager.data.mapper.toDomain
import com.example.grocerymanager.data.mapper.toEntity
import com.example.grocerymanager.domain.model.Budget
import com.example.grocerymanager.domain.repository.BudgetRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao,
    private val database: GroceryDatabase,
) : BudgetRepository {
    override fun observeForMonth(month: Int, year: Int): Flow<Budget?> =
        dao.observeForMonth(month, year).map { it?.toDomain() }

    override fun observeRecent(limit: Int): Flow<List<Budget>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun getForMonth(month: Int, year: Int): Budget? =
        dao.getForMonth(month, year)?.toDomain()

    override suspend fun upsert(budget: Budget): Long {
        // Serialize get-then-write so two concurrent upserts for the same
        // (month, year) can't both miss the existing row and race on the
        // unique index.
        return database.withTransaction {
            val now = System.currentTimeMillis()
            val existing = dao.getForMonth(budget.month, budget.year)
            val entity = budget.toEntity(now).let { newEntity ->
                if (existing != null) {
                    BudgetEntity(
                        id = existing.id,
                        month = newEntity.month,
                        year = newEntity.year,
                        budgetAmount = newEntity.budgetAmount,
                        alertThreshold = newEntity.alertThreshold,
                        createdAt = existing.createdAt,
                        updatedAt = now,
                    )
                } else newEntity
            }
            dao.upsert(entity)
        }
    }

    override suspend fun delete(id: Long) { dao.delete(id) }
}
