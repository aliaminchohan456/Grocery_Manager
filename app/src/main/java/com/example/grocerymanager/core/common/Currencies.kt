package com.example.grocerymanager.core.common

import java.util.Currency
import java.util.Locale

data class CurrencyOption(
    val code: String,
    val displayName: String,
    val symbol: String,
    val popular: Boolean = false,
    /**
     * When `true`, the option does not represent a real ISO 4217 code but
     * a UI affordance (e.g. "Custom…") that opens an input dialog. The
     * picker must filter or special-case this option before rendering
     * the list (see [CUSTOM_SENTINEL]).
     */
    val isCustomSentinel: Boolean = false,
)

object Currencies {
    /**
     * Sentinel code used by the picker to represent the "enter a custom
     * currency code" entry. Mirrors the `Units.CUSTOM_SENTINEL` pattern.
     * Callers must check `option.isCustomSentinel` (or filter on this
     * constant) before treating the option like a real currency.
     */
    const val CUSTOM_SENTINEL: String = "__custom__"

    /**
     * Popular codes are surfaced first in the picker. They cover the most common
     * regions where Grocery Manager is expected to ship first (South Asia, MENA,
     * Western markets).
     */
    private val popularCodes = setOf(
        "PKR", "INR", "BDT", "LKR", // South Asia
        "USD", "EUR", "GBP",        // Western
        "AED", "SAR",                // MENA
    )

    val supported: List<CurrencyOption> = listOf(
        "PKR", "INR", "BDT", "LKR",
        "USD", "EUR", "GBP", "AUD", "CAD",
        "AED", "SAR",
        "JPY", "CNY", "MYR", "SGD",
    ).map { code ->
        val display = runCatching { Currency.getInstance(code).displayName }.getOrDefault(code)
        val symbol = runCatching { Currency.getInstance(code).getSymbol(Locale.getDefault()) }.getOrNull()
            ?: runCatching { Currency.getInstance(code).symbol }.getOrDefault(code)
        CurrencyOption(code, display, symbol, popular = code in popularCodes)
    } + CurrencyOption(
        // Custom-sentinel entry; the picker special-cases this row to open
        // a code-entry dialog instead of calling `selectCurrency`.
        code = CUSTOM_SENTINEL,
        displayName = "Custom",
        symbol = "",
        popular = false,
        isCustomSentinel = true,
    )

    /**
     * Currency to default to for a fresh install, based on the device locale.
     * Returns "PKR" for ur-PK, en-PK, pa-PK, sd-PK; null when the locale is
     * not recognised (caller falls back to the persisted user choice or "USD").
     */
    fun defaultForLocale(locale: Locale = Locale.getDefault()): String? {
        val country = locale.country.uppercase(Locale.ROOT)
        return when (country) {
            "PK" -> "PKR"
            "IN" -> "INR"
            "BD" -> "BDT"
            "LK" -> "LKR"
            "AE" -> "AED"
            "SA" -> "SAR"
            "GB" -> "GBP"
            else -> null
        }
    }
}
