package com.example.grocerymanager.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuantityFormatTest {
    @Test fun `integer quantities drop the trailing zero`() {
        assertThat(QuantityFormat.format(1.0, "")).isEqualTo("1")
        assertThat(QuantityFormat.format(3.0, "kg")).isEqualTo("3 kg")
    }

    @Test fun `fractional quantities keep the decimal`() {
        assertThat(QuantityFormat.format(1.5, "kg")).isEqualTo("1.5 kg")
    }

    @Test fun `blank unit means quantity only`() {
        assertThat(QuantityFormat.format(2.0, "")).isEqualTo("2")
        assertThat(QuantityFormat.format(2.0, " ")).isEqualTo("2")
    }
}
