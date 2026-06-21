package com.example.grocerymanager.domain.repository

import com.example.grocerymanager.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    suspend fun getById(id: Long): Category?
    suspend fun add(category: Category): Long
    suspend fun update(category: Category)
    suspend fun reorder(orderedIds: List<Long>)
    suspend fun delete(id: Long): Boolean
}
