package com.example.grocerymanager.core.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

typealias MinorUnits = Long

/**
 * Single source of truth for displaying money values.
 *
 * Why not just `NumberFormat.getCurrencyInstance(locale)`?
 * The default ICU currency formatter for `en_US` returns `"PKR1,234.50"` (no space)
 * while `en_PK` returns `"Rs 1,234.50"`. We want a consistent, premium look everywhere,
 * so we always emit `"<CODE> <amount>"` with a non-breaking-thin-space separator
 * for amounts and `"<CODE> 0.00"` for zero.
 */
object MoneyUtils {
    private const val DEFAULT_FRACTION_DIGITS = 2

    /**
     * Convert a display string (e.g. "12.30" or "1,234.50") to minor units.
     * Returns null if the string is blank or unparseable.
     */
    fun parse(input: String?): MinorUnits? {
        if (input.isNullOrBlank()) return null
        val cleaned = input.trim()
            .replace(",", "")
            .replace(" ", "")
            .replace("\u00A0", "")
        val bd = cleaned.toBigDecimalOrNull() ?: return null
        return toMinorUnits(bd)
    }

    /** Convert BigDecimal to minor units using HALF_UP rounding. */
    fun toMinorUnits(value: BigDecimal): MinorUnits =
        value.setScale(DEFAULT_FRACTION_DIGITS, RoundingMode.HALF_UP)
            .movePointRight(DEFAULT_FRACTION_DIGITS)
            .toLong()

    /** Convert a Double to minor units. Avoid using this for user input — prefer BigDecimal. */
    fun toMinorUnits(value: Double): MinorUnits = toMinorUnits(BigDecimal.valueOf(value))

    /** Convert minor units to BigDecimal in major units (e.g. 1230 -> 12.30). */
    fun toMajorUnits(minor: MinorUnits): BigDecimal =
        BigDecimal.valueOf(minor).movePointLeft(DEFAULT_FRACTION_DIGITS)

    /**
     * Format minor units for display.
     *
     * Output shape:
     *  - With grouping: `"PKR 1,234.50"`
     *  - Zero:          `"PKR 0.00"`
     *  - Negative:      `"- PKR 12.30"` (or `"PKR -12.30"` when the symbol follows the amount)
     *
     * We always emit a SPACE between the currency code and the amount, regardless of the
     * platform locale formatter. This guarantees a clean, predictable look across all
     * locales.
     *
     * Currencies that natively use 0 or 3 fraction digits (e.g. JPY, KRW, BHD) respect
     * the platform's [java.util.Currency] defaults.
     */
    fun format(minor: MinorUnits, currencyCode: String, locale: Locale = Locale.getDefault()): String {
        val major = toMajorUnits(minor)
        val fractionDigits = fractionDigitsFor(currencyCode)
        val nf = numberFormat(currencyCode, locale, fractionDigits)
        val amount = nf.format(major)
        // Build a clean "<CODE> <amount>" — always space-separated for premium look.
        return "$currencyCode $amount"
    }

    /**
     * Premium compact formatter — drops the trailing `.00` for whole major
     * units so the home screen and recent-purchase cards read clean.
     *
     * Examples (default `en_US`):
     *  - `format(123450L,  "USD")` → `"USD 1,234.50"`
     *  - `formatCompact(123400L, "USD")` → `"USD 1,234"`     (`.00` dropped)
     *  - `formatCompact(123450L, "USD")` → `"USD 1,234.50"`  (still rendered normally)
     *  - `formatCompact(0L,      "USD")` → `"USD 0"`          (no leading `.00` clutter)
     *  - `formatCompact(123400L, "JPY")` → `"JPY 1,234"`     (unchanged — JPY has 0 fraction digits)
     *  - `formatCompact(123400L, "BHD")` → `"BHD 1,234.000"`  (3 fraction digits preserved)
     *
     * Only "true" zero-decimal collapses for 2-fraction-digit currencies — for
     * 3-fraction-digit currencies (BHD, KWD, OMR) we keep all three digits so
     * we never lose precision the user expects to see.
     */
    fun formatCompact(minor: MinorUnits, currencyCode: String, locale: Locale = Locale.getDefault()): String {
        val fractionDigits = fractionDigitsFor(currencyCode)
        // 2-fraction-digit currencies are the only ones where a clean
        // "1,234" / "1,234.50" toggle reads as natural. JPY (0 dp) already
        // drops the decimals, and BHD (3 dp) should never lose a digit.
        if (fractionDigits == DEFAULT_FRACTION_DIGITS && minor % 100L == 0L) {
            val wholeMajor = minor / 100L
            val nf = numberFormat(currencyCode, locale, 0)
            return "$currencyCode ${nf.format(wholeMajor)}"
        }
        return format(minor, currencyCode, locale)
    }

    /**
     * Multiply a fractional quantity by a per-unit price (stored in minor units)
     * and return the total in minor units. Uses BigDecimal to avoid Double drift.
     */
    fun multiply(quantity: Double, pricePerUnit: MinorUnits): MinorUnits {
        val qty = BigDecimal.valueOf(quantity)
        val perUnit = BigDecimal.valueOf(pricePerUnit).movePointLeft(DEFAULT_FRACTION_DIGITS)
        return toMinorUnits(qty.multiply(perUnit))
    }

    /** Sum a list of minor-unit values safely (Long math, no Double drift). */
    fun sum(values: Collection<MinorUnits>): MinorUnits = values.sum()

    /**
     * Single source of truth for the currency symbol of a given ISO code.
     * Returns the localized symbol, falling back to the generic symbol, then to the code itself.
     */
    fun symbolFor(currencyCode: String, locale: Locale = Locale.getDefault()): String {
        return runCatching { Currency.getInstance(currencyCode).getSymbol(locale) }.getOrNull()
            ?: runCatching { Currency.getInstance(currencyCode).symbol }.getOrNull()
            ?: currencyCode
    }

    /**
     * `NumberFormat` instances are heavy to allocate. Cache them by the
     * (currencyCode, locale, fractionDigits) triple and reuse across calls.
     * Compose calls `format` on every recomposition for every amount label,
     * so this materially reduces GC pressure on Home / Records / Insights.
     */
    private val numberFormatCache = java.util.concurrent.ConcurrentHashMap<NumberFormatKey, NumberFormat>()

    private data class NumberFormatKey(
        val currencyCode: String,
        val locale: Locale,
        val fractionDigits: Int,
    )

    private fun numberFormat(currencyCode: String, locale: Locale, fractionDigits: Int): NumberFormat {
        val key = NumberFormatKey(currencyCode, locale, fractionDigits)
        return numberFormatCache.getOrPut(key) {
            NumberFormat.getNumberInstance(locale).apply {
                minimumFractionDigits = fractionDigits
                maximumFractionDigits = fractionDigits
                isGroupingUsed = true
            }
        }
    }

    private fun fractionDigitsFor(currencyCode: String): Int {
        val defaultFractionDigits = runCatching {
            Currency.getInstance(currencyCode).defaultFractionDigits
        }.getOrDefault(DEFAULT_FRACTION_DIGITS)
        // Currency.defaultFractionDigits returns 0 for JPY/KRW, 3 for BHD/KWD, etc.
        // Clamp to a sane range so a misconfigured currency doesn't print 8 decimals.
        return defaultFractionDigits.coerceIn(0, 3)
    }
}

