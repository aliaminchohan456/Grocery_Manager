package com.example.grocerymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.grocerymanager.data.local.entity.PricePoint
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<com.example.grocerymanager.data.local.entity.PurchaseItemEntity>): List<Long>

    @Query(
        """
        SELECT pi.pricePerUnit AS pricePerUnit, p.purchaseDate AS purchaseDate
        FROM purchase_items pi
        INNER JOIN purchases p ON pi.purchaseId = p.id
        WHERE LOWER(pi.itemName) = LOWER(:name) AND pi.pricePerUnit IS NOT NULL
        ORDER BY p.purchaseDate ASC
        """,
    )
    fun observePriceHistory(name: String): Flow<List<PricePoint>>

    @Query(
        """
        SELECT DISTINCT itemName FROM purchase_items
        WHERE itemName LIKE '%' || :query || '%'
        ORDER BY itemName ASC
        LIMIT :limit
        """,
    )
    fun searchItemNames(query: String, limit: Int = 10): Flow<List<String>>
}
