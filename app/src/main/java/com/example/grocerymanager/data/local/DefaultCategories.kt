package com.example.grocerymanager.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.grocerymanager.data.local.entity.CategoryEntity

object DefaultCategories {
    val seeds: List<CategoryEntity> = listOf(
        CategorySeed("Dairy & Eggs", "Egg", "#2E7D32", 1),
        CategorySeed("Fruits", "Apple", "#F59E0B", 2),
        CategorySeed("Vegetables", "Carrot", "#43D17A", 3),
        CategorySeed("Meat & Fish", "Fish", "#EF4444", 4),
        CategorySeed("Bakery", "Bread", "#A16207", 5),
        CategorySeed("Pantry & Dry Goods", "Rice", "#7C3AED", 6),
        CategorySeed("Oils & Spices", "Oil", "#FF8A1F", 7),
        CategorySeed("Beverages", "Coffee", "#0EA5E9", 8),
        CategorySeed("Snacks", "Candy", "#EC4899", 9),
        CategorySeed("Cleaning", "Spray", "#14B8A6", 10),
        CategorySeed("Personal Care", "Toothbrush", "#6366F1", 11),
        CategorySeed("Other", "Box", "#64748B", 99),
    ).map { (name, icon, color, order) ->
        val now = System.currentTimeMillis()
        CategoryEntity(
            name = name,
            iconName = icon,
            colorHex = color,
            isDefault = true,
            sortOrder = order,
            createdAt = now,
            updatedAt = now,
        )
    }
}

private data class CategorySeed(val name: String, val icon: String, val color: String, val order: Int)

internal fun seedDefaultCategories(db: SupportSQLiteDatabase, now: Long) {
    DefaultCategories.seeds.forEach { c ->
        db.execSQL(
            "INSERT OR IGNORE INTO categories (name, iconName, colorHex, isDefault, sortOrder, createdAt, updatedAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)",
            arrayOf(c.name, c.iconName, c.colorHex, 1, c.sortOrder, now, now),
        )
    }
}
