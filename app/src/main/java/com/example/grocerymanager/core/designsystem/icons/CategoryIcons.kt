package com.example.grocerymanager.core.designsystem.icons

import androidx.compose.ui.graphics.vector.ImageVector
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.*
import com.adamglin.phosphoricons.regular.*

/**
 * Premium category icon registry. Each name is the same string persisted on
 * the [CategoryEntity.iconName] column. Default categories ship with a
 * mapped name; new categories get `CategoryDefaultIcon` ("Box").
 */
object CategoryIcons {
    private val map: Map<String, ImageVector> = mapOf(
        // Dairy (distinct icons per item)
        "Milk" to PhosphorIcons.Regular.Martini,
        "Butter" to PhosphorIcons.Regular.Cookie,
        "Cheese" to PhosphorIcons.Regular.Cookie,
        "Eggs" to PhosphorIcons.Regular.Egg,
        "Yogurt" to PhosphorIcons.Regular.Coffee,
        "Dairy" to PhosphorIcons.Regular.Drop,

        // Fruits
        "Apple" to PhosphorIcons.Regular.AppleLogo,
        "Banana" to PhosphorIcons.Regular.OrangeSlice,
        "Fruit" to PhosphorIcons.Regular.AppleLogo,

        // Vegetables
        "Carrot" to PhosphorIcons.Regular.Carrot,
        "Tomato" to PhosphorIcons.Regular.Circle,
        "Vegetable" to PhosphorIcons.Regular.Plant,

        // Meat
        "Chicken" to PhosphorIcons.Regular.Hamburger,
        "Meat" to PhosphorIcons.Regular.Hamburger,
        "Fish" to PhosphorIcons.Regular.Fish,

        // Bakery
        "Bread" to PhosphorIcons.Regular.BowlFood,
        "Bakery" to PhosphorIcons.Regular.BowlFood,

        // Pantry
        "Rice" to PhosphorIcons.Regular.Grains,
        "Pasta" to PhosphorIcons.Regular.Package,
        "Pantry" to PhosphorIcons.Regular.Package,
        "Oil" to PhosphorIcons.Regular.Drop,

        // Oils & Spices
        "Spice" to PhosphorIcons.Regular.Fire,
        "OilSpice" to PhosphorIcons.Regular.Drop,

        // Beverages
        "Coffee" to PhosphorIcons.Regular.Coffee,
        "Tea" to PhosphorIcons.Regular.Coffee,
        "Juice" to PhosphorIcons.Regular.Wine,
        "Water" to PhosphorIcons.Regular.Drop,
        "Beverage" to PhosphorIcons.Regular.Wine,

        // Snacks
        "Cookie" to PhosphorIcons.Regular.Cookie,
        "Candy" to PhosphorIcons.Regular.Cookie,
        "Snack" to PhosphorIcons.Regular.Cookie,
        "Pizza" to PhosphorIcons.Regular.Cookie,
        "Popcorn" to PhosphorIcons.Regular.Cookie,

        // Frozen
        "Frozen" to PhosphorIcons.Regular.Snowflake,
        "IceCream" to PhosphorIcons.Regular.Snowflake,

        // Cleaning
        "Cleaning" to PhosphorIcons.Regular.SprayBottle,
        "Spray" to PhosphorIcons.Regular.SprayBottle,

        // Personal care
        "PersonalCare" to PhosphorIcons.Regular.PaintBrush,
        "Toothbrush" to PhosphorIcons.Regular.PaintBrush,

        // Generic
        "Box" to PhosphorIcons.Regular.Package,
        "Grocery" to PhosphorIcons.Regular.ShoppingBag,
        "Default" to PhosphorIcons.Regular.Storefront,
    )

    fun byName(name: String): ImageVector = map[name] ?: map.getValue("Default")

    fun allNames(): List<String> = map.keys.toList()
}
