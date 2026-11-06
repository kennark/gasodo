package com.gasodoapp.gasodo.core.enums.units

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DistanceTest {

    @Test
    fun `all distances have correct display names, singular names and shorthands`() {
        assertThat(Distance.KILOMETER.displayName).isEqualTo("Kilometers")
        assertThat(Distance.KILOMETER.singularName).isEqualTo("Kilometer")
        assertThat(Distance.KILOMETER.shorthand).isEqualTo("km")
        assertThat(Distance.MILE.displayName).isEqualTo("Miles")
        assertThat(Distance.MILE.singularName).isEqualTo("Mile")
        assertThat(Distance.MILE.shorthand).isEqualTo("mi")
    }

    @Test
    fun `toString returns displayName for each distance`() {
        Distance.entries.forEach { distance ->
            assertThat(distance.toString()).isEqualTo(distance.displayName)
        }
    }

    /**
     * Guards the invariant that makes the unit selectors readable, where two entries differing
     * only by their plural form would be indistinguishable.
     */
    @Test
    fun `display names are unique`() {
        val displayNames = Distance.entries.map { it.displayName }

        assertThat(displayNames).containsNoDuplicates()
    }

    /**
     * The singular form is what a field label such as `Price per Kilometer` reads, so it must stay
     * distinguishable for the same reason as the plural form.
     */
    @Test
    fun `singular names are unique`() {
        val singularNames = Distance.entries.map { it.singularName }

        assertThat(singularNames).containsNoDuplicates()
    }

    @Test
    fun `shorthands are unique`() {
        val shorthands = Distance.entries.map { it.shorthand }

        assertThat(shorthands).containsNoDuplicates()
    }

    /**
     * A singular form that is not just the plural form without the trailing `s` would be a typo,
     * and the UI shows either one depending on the label, so both must be non blank.
     */
    @Test
    fun `singular and plural names are non blank and differ`() {
        Distance.entries.forEach { distance ->
            assertThat(distance.displayName).isNotEmpty()
            assertThat(distance.singularName).isNotEmpty()
            assertThat(distance.singularName).isNotEqualTo(distance.displayName)
            assertThat(distance.shorthand).isNotEmpty()
        }
    }

    @Test
    fun `there are exactly 2 distances`() {
        assertThat(Distance.entries.size).isEqualTo(2)
    }

    @Test
    fun `DEFAULT is KILOMETER`() {
        assertThat(Distance.DEFAULT).isEqualTo(Distance.KILOMETER)
    }

    @Test
    fun `stored enum names resolve to their entry`() {
        assertThat(Distance.fromName("KILOMETER")).isEqualTo(Distance.KILOMETER)
        assertThat(Distance.fromName("MILE")).isEqualTo(Distance.MILE)
    }

    @Test
    fun `a missing stored value falls back to the default`() {
        assertThat(Distance.fromName(null)).isEqualTo(Distance.DEFAULT)
    }

    @Test
    fun `an unknown stored value falls back to the default`() {
        assertThat(Distance.fromName("FURLONG")).isEqualTo(Distance.DEFAULT)
    }
}
