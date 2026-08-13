package com.example.carrefueltracker.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for BigDecimalUtils utility functions.
 */
class BigDecimalUtilsTest {

    @Test
    fun `toDisplayString converts to plain string with specified decimal points`() {
        val amount = BigDecimal("10.5")
        val result = amount.toDisplayString(2)

        assertThat(result).isEqualTo("10.50")
    }

    @Test
    fun `toDisplayString rounds HALF_EVEN for odd decimals`() {
        val amount1 = BigDecimal("10.555")
        val result1 = amount1.toDisplayString(2)

        assertThat(result1).isEqualTo("10.56")

        val amount2 = BigDecimal("10.454")
        val result2 = amount2.toDisplayString(2)

        assertThat(result2).isEqualTo("10.45")

        val amount3 = BigDecimal("10.655")
        val result3 = amount3.toDisplayString(2)

        assertThat(result3).isEqualTo("10.66")
    }

    @Test
    fun `toDisplayString handles zero decimal points`() {
        val amount = BigDecimal("99.99")
        val result = amount.toDisplayString(0)

        assertThat(result).isEqualTo("100")
    }

    @Test
    fun `toDisplayString handles negative numbers`() {
        val amount = BigDecimal("-25.678")
        val result = amount.toDisplayString(2)

        assertThat(result).isEqualTo("-25.68")
    }

    @Test
    fun `toDisplayString handles very small decimal values`() {
        val amount = BigDecimal("0.001")
        val result = amount.toDisplayString(3)

        assertThat(result).isEqualTo("0.001")
    }

    @Test
    fun `toDisplayStringNullable converts non-null values`() {
        var amount: BigDecimal? = null
        amount = BigDecimal("15.7")
        assertThat(amount.toDisplayString(2)).isEqualTo("15.70")
    }

    @Test
    fun `toDisplayStringNullable converts null values`() {
        val amount: BigDecimal? = null
        assertThat(amount.toDisplayString(2)).isNull()
    }

    @Test
    fun `toDisplayString handles zero value`() {
        val amount = BigDecimal("0.0")
        val result = amount.toDisplayString(3)

        assertThat(result).isEqualTo("0.000")
    }

    @Test
    fun `toDisplayString handles large numbers with decimals`() {
        val amount = BigDecimal("1234567.891")
        val result = amount.toDisplayString(2)

        assertThat(result).isEqualTo("1234567.89")
    }

    @Test
    fun `toDisplayString handles trailing zeros correctly`() {
        val amount = BigDecimal("100.0")
        val result = amount.toDisplayString(2)

        assertThat(result).isEqualTo("100.00")
    }

    @Test
    fun `toDisplayString preserves precision for exact values`() {
        val amount = BigDecimal("5.00")
        val result = amount.toDisplayString(2)

        assertThat(result).isEqualTo("5.00")
    }

    @Test
    fun `toDisplayString with SCALE constant`() {
        val amount = BigDecimal("19.999")
        val result = amount.toDisplayString(BigDecimalUtils.SCALE)

        val scale = result.split('.')[1].length
        assertThat(scale).isEqualTo(BigDecimalUtils.SCALE)
    }

    @Test
    fun `toDisplayString converts scientific notation to plain string`() {
        val amount = BigDecimal("1E-5")
        val result = amount.toDisplayString(10)

        assertThat(result).isEqualTo("0.0000100000")
    }

    @Test
    fun `toDisplayString handles multiple decimal point requests`() {
        val amount = BigDecimal("5")

        val result1 = amount.toDisplayString(0)
        assertThat(result1).isEqualTo("5")

        val result2 = amount.toDisplayString(1)
        assertThat(result2).isEqualTo("5.0")

        val result3 = amount.toDisplayString(2)
        assertThat(result3).isEqualTo("5.00")

        val result4 = amount.toDisplayString(5)
        assertThat(result4).isEqualTo("5.00000")
    }
}
