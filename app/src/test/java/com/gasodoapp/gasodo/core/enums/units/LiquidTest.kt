package com.gasodoapp.gasodo.core.enums.units

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LiquidTest {

    @Test
    fun `all liquids have correct display names, singular names and shorthands`() {
        assertThat(Liquid.GALLON.displayName).isEqualTo("Gallons")
        assertThat(Liquid.GALLON.singularName).isEqualTo("Gallon")
        assertThat(Liquid.GALLON.shorthand).isEqualTo("gal")
        assertThat(Liquid.LITER.displayName).isEqualTo("Liters")
        assertThat(Liquid.LITER.singularName).isEqualTo("Liter")
        assertThat(Liquid.LITER.shorthand).isEqualTo("L")
    }

    @Test
    fun `toString returns displayName for each liquid`() {
        Liquid.entries.forEach { liquid ->
            assertThat(liquid.toString()).isEqualTo(liquid.displayName)
        }
    }

    /**
     * Guards the invariant that makes the unit selectors readable, where two entries differing
     * only by their plural form would be indistinguishable.
     */
    @Test
    fun `display names are unique`() {
        val displayNames = Liquid.entries.map { it.displayName }

        assertThat(displayNames).containsNoDuplicates()
    }

    /**
     * The singular form is what a field label such as `Price per Liter` reads, so it must stay
     * distinguishable for the same reason as the plural form.
     */
    @Test
    fun `singular names are unique`() {
        val singularNames = Liquid.entries.map { it.singularName }

        assertThat(singularNames).containsNoDuplicates()
    }

    @Test
    fun `shorthands are unique`() {
        val shorthands = Liquid.entries.map { it.shorthand }

        assertThat(shorthands).containsNoDuplicates()
    }

    /**
     * A singular form that is not just the plural form without the trailing `s` would be a typo,
     * and the UI shows either one depending on the label, so both must be non blank.
     */
    @Test
    fun `singular and plural names are non blank and differ`() {
        Liquid.entries.forEach { liquid ->
            assertThat(liquid.displayName).isNotEmpty()
            assertThat(liquid.singularName).isNotEmpty()
            assertThat(liquid.singularName).isNotEqualTo(liquid.displayName)
            assertThat(liquid.shorthand).isNotEmpty()
        }
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
