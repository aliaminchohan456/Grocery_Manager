package com.example.grocerymanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = GroceryItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["groceryItemId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("purchaseId"),
        Index("categoryId"),
        Index("groceryItemId"),
        Index("itemName"),
    ],
)
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val itemName: String,
    val groceryItemId: Long?,
    val categoryId: Long,
    val quantity: Double,
    val unit: String,
    val pricePerUnit: Long?,
    val totalPrice: Long,
    val createdAt: Long,
)
