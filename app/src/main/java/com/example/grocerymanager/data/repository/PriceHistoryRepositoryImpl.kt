package com.example.grocerymanager.data.repository

import com.example.grocerymanager.data.local.dao.PurchaseItemDao
import com.example.grocerymanager.data.mapper.toDomain
import com.example.grocerymanager.domain.model.PricePoint
import com.example.grocerymanager.domain.repository.PriceHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PriceHistoryRepositoryImpl @Inject constructor(
    private val dao: PurchaseItemDao,
) : PriceHistoryRepository {
    override fun observePriceHistory(name: String): Flow<List<PricePoint>> =
        dao.observePriceHistory(name).map { list -> list.map { it.toDomain() } }

    override fun searchItemNames(query: String, limit: Int): Flow<List<String>> =
        dao.searchItemNames(query, limit)
}
