package com.example.grocerymanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["month", "year"], unique = true)],
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val month: Int,
    val year: Int,
    val budgetAmount: Long,
    val alertThreshold: Float = 0.8f,
    val createdAt: Long,
    val updatedAt: Long,
)
