package com.gasodoapp.gasodo.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

/**
 * Unit tests for BigDecimalUtils utility functions.
 */
class BigDecimalUtilsTest {

    private val us = Locale.US

    @Test
    fun `toDisplayString converts to plain string with specified decimal points`() {
        val amount = BigDecimal("10.5")

        assertThat(amount.toDisplayString(2, us)).isEqualTo("10.50")
    }

    @Test
    fun `toDisplayString rounds HALF_EVEN for odd decimals`() {
        assertThat(BigDecimal("10.555").toDisplayString(2, us)).isEqualTo("10.56")
        assertThat(BigDecimal("10.454").toDisplayString(2, us)).isEqualTo("10.45")
        assertThat(BigDecimal("10.655").toDisplayString(2, us)).isEqualTo("10.66")
    }

    @Test
    fun `toDisplayString handles zero decimal points`() {
        assertThat(BigDecimal("99.99").toDisplayString(0, us)).isEqualTo("100")
    }

    @Test
    fun `toDisplayString handles negative numbers`() {
        assertThat(BigDecimal("-25.678").toDisplayString(2, us)).isEqualTo("-25.68")
    }

    @Test
    fun `toDisplayString handles very small decimal values`() {
        assertThat(BigDecimal("0.001").toDisplayString(3, us)).isEqualTo("0.001")
    }

    @Test
    fun `toDisplayStringNullable converts non-null values`() {
        val amount: BigDecimal = BigDecimal("15.7")

        assertThat(amount.toDisplayString(2, us)).isEqualTo("15.70")
    }

    @Test
    fun `toDisplayStringNullable converts null values`() {
        val amount: BigDecimal? = null

        assertThat(amount.toDisplayString(2, us)).isNull()
    }

    @Test
    fun `toDisplayString handles zero value`() {
        assertThat(BigDecimal("0.0").toDisplayString(3, us)).isEqualTo("0.000")
    }

    @Test
    fun `toDisplayString handles large numbers with decimals`() {
        assertThat(BigDecimal("1234567.891").toDisplayString(2, us)).isEqualTo("1,234,567.89")
    }

    @Test
    fun `toDisplayString skips digit grouping when asked to`() {
        assertThat(BigDecimal("1234567.891").toDisplayString(2, us, groupingUsed = false))
            .isEqualTo("1234567.89")
    }

    @Test
    fun `toDisplayString handles trailing zeros correctly`() {
        assertThat(BigDecimal("100.0").toDisplayString(2, us)).isEqualTo("100.00")
    }

    @Test
    fun `toDisplayString preserves precision for exact values`() {
        assertThat(BigDecimal("5.00").toDisplayString(2, us)).isEqualTo("5.00")
    }

    @Test
    fun `toDisplayString with SCALE constant`() {
        val result = BigDecimal("19.999").toDisplayString(BigDecimalUtils.SCALE, us)

        assertThat(result).isEqualTo("19.99900")
    }

    @Test
    fun `toDisplayString converts scientific notation to plain string`() {
        assertThat(BigDecimal("1E-5").toDisplayString(10, us)).isEqualTo("0.0000100000")
    }

    @Test
    fun `toDisplayString handles multiple decimal point requests`() {
        val amount = BigDecimal("5")

        assertThat(amount.toDisplayString(0, us)).isEqualTo("5")
        assertThat(amount.toDisplayString(1, us)).isEqualTo("5.0")
        assertThat(amount.toDisplayString(2, us)).isEqualTo("5.00")
        assertThat(amount.toDisplayString(5, us)).isEqualTo("5.00000")
    }

    // region Locale awareness

    @Test
    fun `toDisplayString uses the separators of the given locale`() {
        val amount = BigDecimal("1234.5")

        assertThat(amount.toDisplayString(2, Locale.US)).isEqualTo("1,234.50")
        assertThat(amount.toDisplayString(2, Locale.GERMANY)).isEqualTo("1.234,50")
    }

    @Test
    fun `toDisplayString does not group below a thousand in comma decimal locales`() {
        assertThat(BigDecimal("999.5").toDisplayString(2, Locale.ITALY)).isEqualTo("999,50")
    }

    @Test
    fun `toDisplayString falls back to the device locale`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.GERMANY)

            assertThat(BigDecimal("1234.5").toDisplayString(2)).isEqualTo("1.234,50")
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun `toDisplayString renders a negative zero without a sign`() {
        assertThat(BigDecimal("-0.001").toDisplayString(2, us)).isEqualTo("0.00")
    }

    // endregion

    // region Long overload

    @Test
    fun `toDisplayString on Long groups digits`() {
        assertThat(934085L.toDisplayString(us)).isEqualTo("934,085")
        assertThat(934085L.toDisplayString(Locale.GERMANY)).isEqualTo("934.085")
    }

    @Test
    fun `toDisplayString on Long skips grouping when asked to`() {
        assertThat(934085L.toDisplayString(us, groupingUsed = false)).isEqualTo("934085")
    }

    // endregion
}
