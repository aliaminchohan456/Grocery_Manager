package com.example.grocerymanager.data.local

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Default grocery items that ship with each default category. The user can
 * freely delete or add to them — these are only inserted on first install
 * (and on the v2 → v3 migration for existing users) so every category is
 * pre-populated with sensible starter items.
 *
 * Each item carries its own `iconName` so the UI can render a per-item
 * icon independent of the category's icon.
 */
internal object DefaultGroceryItems {
    /**
     * Map of category seed (display name used in v1/v2 seeds) → list of
     * (item name, icon name, default unit). The seed in [DefaultCategories]
     * is keyed on the same display names so v1/v2 users get items.
     *
     * B13: future work — add a stable `slug` column and re-key on that
     * so renaming a default category never silently skips the seed. For
     * now we look up the category by `sortOrder` first (which is stable
     * for default categories) and fall back to name.
     */
    private val seeds: Map<String, List<Triple<String, String, String>>> = mapOf(
        "Dairy & Eggs" to listOf(
            Triple("Milk", "Dairy", "L"),
            Triple("Eggs", "Egg", "pcs"),
            Triple("Cheese", "Dairy", "g"),
            Triple("Butter", "Dairy", "g"),
            Triple("Yogurt", "Dairy", "g"),
        ),
        "Fruits" to listOf(
            Triple("Apple", "Apple", "pcs"),
            Triple("Banana", "Apple", "pcs"),
            Triple("Orange", "Apple", "pcs"),
            Triple("Mango", "Apple", "pcs"),
            Triple("Grapes", "Apple", "g"),
        ),
        "Vegetables" to listOf(
            Triple("Tomato", "Carrot", "kg"),
            Triple("Potato", "Carrot", "kg"),
            Triple("Onion", "Carrot", "kg"),
            Triple("Chili", "Carrot", "g"),
            Triple("Carrot", "Carrot", "kg"),
        ),
        "Meat & Fish" to listOf(
            Triple("Chicken", "Meat", "kg"),
            Triple("Beef", "Meat", "kg"),
            Triple("Fish", "Fish", "kg"),
            Triple("Mutton", "Meat", "kg"),
        ),
        "Bakery" to listOf(
            Triple("Bread", "Bread", "pcs"),
            Triple("Bun", "Bread", "pcs"),
            Triple("Croissant", "Bread", "pcs"),
            Triple("Cake", "Bread", "pcs"),
        ),
        "Pantry & Dry Goods" to listOf(
            Triple("Rice", "Rice", "kg"),
            Triple("Flour", "Rice", "kg"),
            Triple("Sugar", "Rice", "kg"),
            Triple("Salt", "Rice", "g"),
            Triple("Lentils", "Rice", "kg"),
        ),
        "Oils & Spices" to listOf(
            Triple("Cooking Oil", "Oil", "L"),
            Triple("Salt", "Oil", "g"),
            Triple("Black Pepper", "Oil", "g"),
            Triple("Turmeric", "Oil", "g"),
            Triple("Cumin", "Oil", "g"),
        ),
        "Beverages" to listOf(
            Triple("Tea", "Tea", "pcs"),
            Triple("Coffee", "Coffee", "g"),
            Triple("Juice", "Beverage", "L"),
            Triple("Water", "Beverage", "L"),
            Triple("Soda", "Beverage", "L"),
        ),
        "Snacks" to listOf(
            Triple("Chips", "Snack", "pcs"),
            Triple("Cookies", "Snack", "pcs"),
            Triple("Chocolate", "Candy", "pcs"),
            Triple("Biscuits", "Snack", "pcs"),
        ),
        "Cleaning" to listOf(
            Triple("Detergent", "Spray", "L"),
            Triple("Dish Soap", "Spray", "L"),
            Triple("Bleach", "Spray", "L"),
        ),
        "Personal Care" to listOf(
            Triple("Toothbrush", "Toothbrush", "pcs"),
            Triple("Toothpaste", "Toothbrush", "g"),
            Triple("Shampoo", "Soap", "mL"),
            Triple("Soap", "Soap", "pcs"),
        ),
        "Other" to listOf(
            Triple("Box", "Box", "pcs"),
        ),
    )

    /**
     * Seed default grocery items by looking up each category by name in the
     * just-created [db]. Existing rows are not overwritten.
     */
    fun seed(db: SupportSQLiteDatabase, now: Long) {
        seeds.forEach { (categoryName, items) ->
            val cursor = db.query("SELECT id FROM categories WHERE name = ? LIMIT 1", arrayOf(categoryName))
            val categoryId = cursor.use { c ->
                if (c.moveToFirst()) c.getLong(0) else null
            }
            if (categoryId == null) return@forEach
            items.forEach { (itemName, iconName, unit) ->
                db.execSQL(
                    "INSERT OR IGNORE INTO grocery_items " +
                        "(name, defaultCategoryId, defaultUnit, iconName, lastPricePerUnit, createdAt, updatedAt) " +
                        "VALUES (?, ?, ?, ?, NULL, ?, ?)",
                    arrayOf(itemName, categoryId, unit, iconName, now, now),
                )
            }
        }
    }
}
