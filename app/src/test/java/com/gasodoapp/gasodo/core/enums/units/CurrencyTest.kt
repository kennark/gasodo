package com.gasodoapp.gasodo.core.enums.units

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CurrencyTest {

    @Test
    fun `symbols are stable and names are resource backed`() {
        assertThat(Currency.USD.symbol).isEqualTo("$")
        assertThat(Currency.USD.nameRes).isNotEqualTo(0)
        assertThat(Currency.EUR.symbol).isEqualTo("€")
        assertThat(Currency.EUR.nameRes).isNotEqualTo(0)
        assertThat(Currency.GBP.symbol).isEqualTo("£")
        assertThat(Currency.GBP.nameRes).isNotEqualTo(0)
        assertThat(Currency.JPY.symbol).isEqualTo("¥")
        assertThat(Currency.JPY.nameRes).isNotEqualTo(0)
        assertThat(Currency.INR.symbol).isEqualTo("₹")
        assertThat(Currency.INR.nameRes).isNotEqualTo(0)
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
    fun `display name resources are unique`() {
        val nameRes = Currency.entries.map { it.nameRes }

        assertThat(nameRes).containsNoDuplicates()
    }

    @Test
    fun `every currency maps to a string resource`() {
        Currency.entries.forEach { currency ->
            assertThat(currency.nameRes).isNotEqualTo(0)
        }
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
