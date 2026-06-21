package com.example.grocerymanager.data.local.entity

data class CategoryTotal(
    val categoryId: Long,
    val total: Long,
)

data class ShopTotal(
    val shopName: String?,
    val total: Long,
)

data class MonthlyTotal(
    val year: Int,
    val month: Int,
    val total: Long,
)

data class TopItem(
    val itemName: String,
    val totalSpent: Long,
    val purchaseCount: Int,
)

data class PricePoint(
    val purchaseDate: Long,
    val pricePerUnit: Long,
)
