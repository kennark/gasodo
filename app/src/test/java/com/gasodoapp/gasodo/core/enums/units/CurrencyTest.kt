package com.gasodoapp.gasodo.core.enums.units

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CurrencyTest {

    @Test
    fun `all currencies have correct display names and symbols`() {
        assertThat(Currency.USD.displayName).isEqualTo("US Dollar")
        assertThat(Currency.USD.symbol).isEqualTo("$")
        assertThat(Currency.EUR.displayName).isEqualTo("Euro")
        assertThat(Currency.EUR.symbol).isEqualTo("€")
        assertThat(Currency.GBP.displayName).isEqualTo("British Pound")
        assertThat(Currency.GBP.symbol).isEqualTo("£")
        assertThat(Currency.JPY.displayName).isEqualTo("Japanese Yen")
        assertThat(Currency.JPY.symbol).isEqualTo("¥")
        assertThat(Currency.INR.displayName).isEqualTo("Indian Rupee")
        assertThat(Currency.INR.symbol).isEqualTo("₹")
    }

    @Test
    fun `toString returns displayName for each currency`() {
        Currency.entries.forEach { currency ->
            assertThat(currency.toString()).isEqualTo(currency.displayName)
        }
    }

    /**
     * Guards the invariant that lets a currency be recognised from the rendered symbol alone.
     */
    @Test
    fun `symbols are unique`() {
        val symbols = Currency.entries.map { it.symbol }

        assertThat(symbols).containsNoDuplicates()
    }

    @Test
    fun `display names are unique`() {
        val displayNames = Currency.entries.map { it.displayName }

        assertThat(displayNames).containsNoDuplicates()
    }

    @Test
    fun `there are exactly 16 currencies`() {
        assertThat(Currency.entries.size).isEqualTo(16)
    }

    @Test
    fun `DEFAULT is EUR`() {
        assertThat(Currency.DEFAULT).isEqualTo(Currency.EUR)
    }

    @Test
    fun `stored enum names resolve to their entry`() {
        Currency.entries.forEach { currency ->
            assertThat(Currency.fromName(currency.name)).isEqualTo(currency)
        }
    }

    @Test
    fun `a missing stored value falls back to the default`() {
        assertThat(Currency.fromName(null)).isEqualTo(Currency.DEFAULT)
    }

    @Test
    fun `an unknown stored value falls back to the default`() {
        assertThat(Currency.fromName("XYZ")).isEqualTo(Currency.DEFAULT)
    }

    @Test
    fun `a stored display name is not accepted as an enum name`() {
        assertThat(Currency.fromName("US Dollar")).isEqualTo(Currency.DEFAULT)
    }
}
