package com.example.grocerymanager.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MoneyUtilsFormatCacheTest {
    @Test fun `format is stable for the same minor unit value`() {
        val once = MoneyUtils.format(123_450L, "PKR")
        val twice = MoneyUtils.format(123_450L, "PKR")
        assertThat(once).isEqualTo(twice)
        assertThat(once).contains("1,234.50")
        assertThat(once).startsWith("PKR ")
    }

    @Test fun `format with negative value is also stable`() {
        val once = MoneyUtils.format(-12_300L, "USD")
        val twice = MoneyUtils.format(-12_300L, "USD")
        assertThat(once).isEqualTo(twice)
        assertThat(once).contains("123.00")
    }

    @Test fun `format with zero shows zero major units`() {
        val out = MoneyUtils.format(0L, "USD")
        assertThat(out).contains("0.00")
    }
}
