package com.example.grocerymanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stores",
    indices = [Index(value = ["name"], unique = true)],
)
data class StoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val visitCount: Int = 0,
    val lastVisitAt: Long? = null,
    val createdAt: Long,
)
