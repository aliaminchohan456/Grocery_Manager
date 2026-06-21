package com.example.grocerymanager.domain.repository

import com.example.grocerymanager.domain.model.PricePoint
import com.example.grocerymanager.domain.model.Store
import kotlinx.coroutines.flow.Flow

interface PriceHistoryRepository {
    fun observePriceHistory(name: String): Flow<List<PricePoint>>
    fun searchItemNames(query: String, limit: Int = 10): Flow<List<String>>
}

interface StoreRepository {
    fun observeAll(): Flow<List<Store>>
    fun search(query: String, limit: Int = 10): Flow<List<Store>>
    suspend fun getByName(name: String): Store?
    suspend fun recordVisit(name: String)
}
