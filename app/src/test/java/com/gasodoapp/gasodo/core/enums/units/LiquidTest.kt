package com.gasodoapp.gasodo.core.enums.units

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LiquidTest {

    @Test
    fun `all liquids have shorthands and resource backed names`() {
        assertThat(Liquid.GALLON.shorthand).isEqualTo("gal")
        assertThat(Liquid.GALLON.nameRes).isNotEqualTo(0)
        assertThat(Liquid.GALLON.singularNameRes).isNotEqualTo(0)

        assertThat(Liquid.LITER.shorthand).isEqualTo("L")
        assertThat(Liquid.LITER.nameRes).isNotEqualTo(0)
        assertThat(Liquid.LITER.singularNameRes).isNotEqualTo(0)
    }

    /**
     * Guards the invariant that makes the unit selectors readable, where two entries differing
     * only by their plural form would be indistinguishable.
     */
    @Test
    fun `display name resources are unique`() {
        val nameRes = Liquid.entries.map { it.nameRes }

        assertThat(nameRes).containsNoDuplicates()
    }

    /**
     * The singular form is what a field label such as `Price per Liter` reads, so it must stay
     * distinguishable for the same reason as the plural form.
     */
    @Test
    fun `singular name resources are unique and differ from the plural form`() {
        val singularNameRes = Liquid.entries.map { it.singularNameRes }

        assertThat(singularNameRes).containsNoDuplicates()

        Liquid.entries.forEach { liquid ->
            assertThat(liquid.singularNameRes).isNotEqualTo(liquid.nameRes)
        }
    }

    @Test
    fun `shorthands are unique`() {
        val shorthands = Liquid.entries.map { it.shorthand }

        assertThat(shorthands).containsNoDuplicates()
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
