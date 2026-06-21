package com.example.grocerymanager.core.common

/**
 * Canonical unit list for grocery items. The only place in the codebase where
 * this list lives — every screen and use case must import from here.
 *
 * Order matters: it's the display order on the unit chip row.
 */
object Units {
    val Common: List<String> = listOf("kg", "g", "L", "ml", "pcs", "pack", "dozen")

    /** Sentinel string the unit picker uses to mean "show the custom-unit text field". */
    const val CUSTOM_SENTINEL: String = "__custom__"

    /** True when [unit] is non-blank and not in the common list (case-insensitive). */
    fun isCustom(unit: String): Boolean =
        unit.isNotBlank() && Common.none { it.equals(unit, ignoreCase = true) }
}
