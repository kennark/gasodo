package com.gasodoapp.gasodo.core.enums.units

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DistanceTest {

    @Test
    fun `all distances have shorthands and resource backed names`() {
        assertThat(Distance.KILOMETER.shorthand).isEqualTo("km")
        assertThat(Distance.KILOMETER.nameRes).isNotEqualTo(0)
        assertThat(Distance.KILOMETER.singularNameRes).isNotEqualTo(0)

        assertThat(Distance.MILE.shorthand).isEqualTo("mi")
        assertThat(Distance.MILE.nameRes).isNotEqualTo(0)
        assertThat(Distance.MILE.singularNameRes).isNotEqualTo(0)
    }

    /**
     * Guards the invariant that makes the unit selectors readable, where two entries differing
     * only by their plural form would be indistinguishable.
     */
    @Test
    fun `display name resources are unique`() {
        val nameRes = Distance.entries.map { it.nameRes }

        assertThat(nameRes).containsNoDuplicates()
    }

    /**
     * The singular form is what a field label such as `Price per Kilometer` reads, so it must stay
     * distinguishable for the same reason as the plural form.
     */
    @Test
    fun `singular name resources are unique and differ from the plural form`() {
        val singularNameRes = Distance.entries.map { it.singularNameRes }

        assertThat(singularNameRes).containsNoDuplicates()

        Distance.entries.forEach { distance ->
            assertThat(distance.singularNameRes).isNotEqualTo(distance.nameRes)
        }
    }

    @Test
    fun `shorthands are unique`() {
        val shorthands = Distance.entries.map { it.shorthand }

        assertThat(shorthands).containsNoDuplicates()
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
