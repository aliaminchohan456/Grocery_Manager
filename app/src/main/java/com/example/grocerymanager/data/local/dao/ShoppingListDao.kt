package com.example.grocerymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.grocerymanager.data.local.entity.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_list ORDER BY isPurchased ASC, createdAt DESC")
    fun observeAll(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_list WHERE isPurchased = 0 ORDER BY createdAt DESC")
    fun observePending(): Flow<List<ShoppingListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingListEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingListEntity>): List<Long>

    @Update
    suspend fun update(item: ShoppingListEntity)

    @Update
    suspend fun updateAll(items: List<ShoppingListEntity>)

    @Delete
    suspend fun delete(item: ShoppingListEntity)

    @Query("DELETE FROM shopping_list WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("DELETE FROM shopping_list WHERE isPurchased = 1")
    suspend fun clearPurchased(): Int
}
