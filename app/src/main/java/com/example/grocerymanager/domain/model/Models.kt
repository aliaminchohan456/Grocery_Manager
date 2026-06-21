package com.example.grocerymanager.domain.model

import com.example.grocerymanager.core.common.MinorUnits

data class Category(
    val id: Long,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val isDefault: Boolean,
    val sortOrder: Int,
)

data class GroceryItem(
    val id: Long,
    val name: String,
    val defaultCategoryId: Long?,
    val defaultUnit: String,
    val iconName: String? = null,
    val lastPricePerUnit: MinorUnits?,
)

data class Purchase(
    val id: Long,
    val purchaseDate: Long,
    val shopName: String?,
    val totalAmount: MinorUnits,
    val notes: String?,
    val receiptImageUri: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

data class PurchaseItem(
    val id: Long,
    val purchaseId: Long,
    val itemName: String,
    val groceryItemId: Long?,
    val categoryId: Long,
    val quantity: Double,
    val unit: String,
    val pricePerUnit: MinorUnits?,
    val totalPrice: MinorUnits,
)

data class PurchaseWithItems(
    val purchase: Purchase,
    val items: List<PurchaseItem>,
) {
    val total: MinorUnits get() = items.sumOf { it.totalPrice }
}

data class Budget(
    val id: Long,
    val month: Int,
    val year: Int,
    val budgetAmount: MinorUnits,
    val alertThreshold: Float,
)

data class ShoppingListItem(
    val id: Long,
    val name: String,
    val quantity: Double,
    val unit: String,
    val categoryId: Long?,
    val isPurchased: Boolean,
    val purchasedAt: Long?,
)

data class Store(
    val id: Long,
    val name: String,
    val visitCount: Int,
    val lastVisitAt: Long?,
)

data class CategoryTotal(
    val categoryId: Long,
    val total: MinorUnits,
)

data class ShopTotal(
    val shopName: String?,
    val total: MinorUnits,
)

data class MonthlyTotal(
    val year: Int,
    val month: Int,
    val total: MinorUnits,
)

data class TopItem(
    val itemName: String,
    val totalSpent: MinorUnits,
    val purchaseCount: Int,
)

data class PricePoint(
    val purchaseDate: Long,
    val pricePerUnit: MinorUnits,
)
