package com.example.grocerymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.grocerymanager.data.local.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Query("SELECT * FROM stores ORDER BY name ASC")
    fun observeAll(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE name LIKE '%' || :query || '%' ORDER BY name ASC LIMIT :limit")
    fun search(query: String, limit: Int = 10): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): StoreEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(store: StoreEntity): Long

    @Update
    suspend fun update(store: StoreEntity)

    @Query("UPDATE stores SET visitCount = visitCount + 1, lastVisitAt = :now WHERE id = :id")
    suspend fun bumpVisit(id: Long, now: Long)
}
