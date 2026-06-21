package com.example.grocerymanager.domain.repository

import com.example.grocerymanager.domain.model.ShoppingListItem
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    fun observeAll(): Flow<List<ShoppingListItem>>
    fun observePending(): Flow<List<ShoppingListItem>>
    suspend fun add(item: ShoppingListItem): Long
    suspend fun update(item: ShoppingListItem)
    suspend fun delete(id: Long)
    suspend fun togglePurchased(item: ShoppingListItem)
}
