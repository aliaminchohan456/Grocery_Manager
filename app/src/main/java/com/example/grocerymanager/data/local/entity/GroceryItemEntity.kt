package com.example.grocerymanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "grocery_items",
    indices = [
        Index(value = ["name"], unique = true),
        Index("defaultCategoryId"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["defaultCategoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class GroceryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val defaultCategoryId: Long?,
    val defaultUnit: String,
    val iconName: String?,
    val lastPricePerUnit: Long?,
    val createdAt: Long,
    val updatedAt: Long,
)
