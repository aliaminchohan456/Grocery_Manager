package com.example.grocerymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.grocerymanager.data.local.entity.GroceryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryItemDao {
    @Query("SELECT * FROM grocery_items ORDER BY name ASC")
    fun observeAll(): Flow<List<GroceryItemEntity>>

    @Query(
        """
        SELECT * FROM grocery_items
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name ASC
        LIMIT :limit
        """,
    )
    fun search(query: String, limit: Int = 20): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE id = :id")
    suspend fun getById(id: Long): GroceryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: GroceryItemEntity): Long

    @Update
    suspend fun update(item: GroceryItemEntity)

    @Query("UPDATE grocery_items SET lastPricePerUnit = :price, updatedAt = :now WHERE id = :id")
    suspend fun updateLastPrice(id: Long, price: Long, now: Long)

    @Query("SELECT * FROM grocery_items WHERE defaultCategoryId = :categoryId ORDER BY name ASC")
    fun observeForCategory(categoryId: Long): Flow<List<GroceryItemEntity>>

    @Query("DELETE FROM grocery_items WHERE id = :id")
    suspend fun delete(id: Long): Int
}
