package com.example.grocerymanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_list",
    indices = [
        Index("isPurchased"),
        Index("categoryId"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "",
    val categoryId: Long? = null,
    val isPurchased: Boolean = false,
    val purchasedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
