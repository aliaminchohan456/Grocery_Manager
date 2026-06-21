package com.example.grocerymanager.domain.repository

import com.example.grocerymanager.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeForMonth(month: Int, year: Int): Flow<Budget?>
    fun observeRecent(limit: Int = 6): Flow<List<Budget>>
    suspend fun getForMonth(month: Int, year: Int): Budget?
    suspend fun upsert(budget: Budget): Long
    suspend fun delete(id: Long)
}
