package com.example.grocerymanager.core.common

/**
 * Single source of truth for quantity display strings.
 *
 * Replaces the three near-identical helpers that lived in `Purchase.kt`,
 * `ShoppingListScreen.kt` and `AddItemBottomSheet.kt` before the refactor.
 */
object QuantityFormat {

    /**
     * "1" instead of "1.0"; "1.5" stays as is. If [unit] is non-blank, append
     * it with a single space.
     */
    fun format(quantity: Double, unit: String): String {
        val q = if (quantity == quantity.toLong().toDouble()) {
            quantity.toLong().toString()
        } else {
            quantity.toString()
        }
        return if (unit.isBlank()) q else "$q $unit"
    }
}
