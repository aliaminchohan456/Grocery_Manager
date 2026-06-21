package com.example.grocerymanager.core.common

import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import java.util.Locale
import org.junit.Test

class MoneyUtilsTest {

    @Test fun `parse simple decimal returns minor units`() {
        assertThat(MoneyUtils.parse("12.30")).isEqualTo(1230L)
        assertThat(MoneyUtils.parse("100")).isEqualTo(10000L)
        assertThat(MoneyUtils.parse("0.99")).isEqualTo(99L)
    }

    @Test fun `parse handles thousand separators and spaces`() {
        assertThat(MoneyUtils.parse("1,234.50")).isEqualTo(123450L)
        assertThat(MoneyUtils.parse("1 234,50")?.let { BigDecimal.valueOf(it).toString() }).isNotNull()
    }

    @Test fun `parse returns null for blank or invalid`() {
        assertThat(MoneyUtils.parse(null)).isNull()
        assertThat(MoneyUtils.parse("")).isNull()
        assertThat(MoneyUtils.parse("abc")).isNull()
    }

    @Test fun `parse rounds half up at the minor unit`() {
        assertThat(MoneyUtils.parse("1.005")).isEqualTo(101L)
        assertThat(MoneyUtils.parse("1.004")).isEqualTo(100L)
    }

    @Test fun `multiply does not suffer from double drift`() {
        // 0.1 + 0.2 in Double is 0.30000000000000004, but we work in Long minor units.
        val a = MoneyUtils.multiply(3.0, 33L)  // 3 × 0.33 = 0.99
        val b = MoneyUtils.multiply(0.1 + 0.2, 1000L) // (0.3) × 10.00 = 3.00
        assertThat(a).isEqualTo(99L)
        assertThat(b).isEqualTo(300L)
    }

    @Test fun `sum is exact`() {
        val values = listOf(33L, 99L, 100L, 1L)
        assertThat(MoneyUtils.sum(values)).isEqualTo(233L)
    }

    @Test fun `format renders currency code with grouping for USD`() {
        val formatted = MoneyUtils.format(123450L, "USD", Locale.US)
        assertThat(formatted).contains("1,234.50")
        assertThat(formatted).startsWith("USD ")
    }

    @Test fun `format respects JPY zero fraction digits`() {
        val formatted = MoneyUtils.format(1234L, "JPY", Locale.JAPAN)
        // 1234 minor units = 12.34 JPY for currencies with 2 fraction digits,
        // but JPY uses 0 fraction digits so it prints "1,234" instead.
        // (Internal representation is still 2 fraction digits across the app —
        //  the formatter is the only thing that respects the per-currency default.)
        assertThat(formatted).startsWith("JPY ")
        // Should not contain a decimal point since JPY has 0 fraction digits.
        // (The internal value 1234L = 12.34 with 2 dp, so we re-round to integer
        // 12 when formatting JPY. The point of this test is to confirm that the
        // formatter is respecting the currency's default fraction digits.)
        assertThat(formatted).doesNotContain(".")
    }

    @Test fun `format for EUR uses two fraction digits`() {
        val formatted = MoneyUtils.format(123450L, "EUR", Locale.GERMANY)
        assertThat(formatted).startsWith("EUR ")
        assertThat(formatted).contains("1.234,50") // German grouping
    }

    @Test fun `format for PKR uses two fraction digits`() {
        val formatted = MoneyUtils.format(123450L, "PKR", Locale.US)
        assertThat(formatted).startsWith("PKR ")
        assertThat(formatted).contains("1,234.50")
    }

    @Test fun `format for zero shows zero major units`() {
        val formatted = MoneyUtils.format(0L, "USD", Locale.US)
        assertThat(formatted).contains("0.00")
    }

    @Test fun `multiply with very small fractional quantities stays exact`() {
        // 0.001 kg × 1234 cents = 1.234 cents → 1 minor unit (HALF_UP rounding).
        assertThat(MoneyUtils.multiply(0.001, 1234L)).isEqualTo(1L)
        // 1000 × 1.234 = 1234.
        assertThat(MoneyUtils.multiply(1000.0, 1234L)).isEqualTo(1_234_000L)
    }
}
