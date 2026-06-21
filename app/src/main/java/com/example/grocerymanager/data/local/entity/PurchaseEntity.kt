package com.example.grocerymanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    indices = [Index("purchaseDate"), Index("shopName")],
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseDate: Long,
    val shopName: String?,
    val totalAmount: Long,
    val notes: String?,
    val receiptImageUri: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
