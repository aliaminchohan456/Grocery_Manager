package com.example.grocerymanager.domain.repository

import com.example.grocerymanager.core.common.MinorUnits
import com.example.grocerymanager.domain.model.GroceryItem
import kotlinx.coroutines.flow.Flow

interface GroceryItemRepository {
    fun observeAll(): Flow<List<GroceryItem>>
    fun search(query: String, limit: Int = 20): Flow<List<GroceryItem>>
    fun observeForCategory(categoryId: Long): Flow<List<GroceryItem>>
    suspend fun getById(id: Long): GroceryItem?
    suspend fun upsert(item: GroceryItem): Long
    suspend fun update(item: GroceryItem)
    suspend fun delete(id: Long): Boolean
    suspend fun updateLastPrice(id: Long, price: MinorUnits)
}
