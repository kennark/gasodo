package com.gasodoapp.gasodo.core.enums.units

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LiquidTest {

    @Test
    fun `all liquids have correct display names and shorthands`() {
        assertThat(Liquid.GALLON.displayName).isEqualTo("Gallons")
        assertThat(Liquid.GALLON.shorthand).isEqualTo("gal")
        assertThat(Liquid.LITER.displayName).isEqualTo("Liters")
        assertThat(Liquid.LITER.shorthand).isEqualTo("L")
    }

    @Test
    fun `toString returns displayName for each liquid`() {
        assertThat(Liquid.GALLON.toString()).isEqualTo("Gallons")
        assertThat(Liquid.LITER.toString()).isEqualTo("Liters")
    }

    @Test
    fun `there are exactly 2 liquids`() {
        assertThat(Liquid.entries.size).isEqualTo(2)
    }

    @Test
    fun `DEFAULT is LITER`() {
        assertThat(Liquid.DEFAULT).isEqualTo(Liquid.LITER)
    }

    @Test
    fun `stored enum names resolve to their entry`() {
        assertThat(Liquid.fromName("GALLON")).isEqualTo(Liquid.GALLON)
        assertThat(Liquid.fromName("LITER")).isEqualTo(Liquid.LITER)
    }

    @Test
    fun `a missing stored value falls back to the default`() {
        assertThat(Liquid.fromName(null)).isEqualTo(Liquid.DEFAULT)
    }

    @Test
    fun `an unknown stored value falls back to the default`() {
        assertThat(Liquid.fromName("PINT")).isEqualTo(Liquid.DEFAULT)
    }
}
