package com.gasodoapp.gasodo.core.enums.units

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DistanceTest {

    @Test
    fun `all distances have correct display names and shorthands`() {
        assertThat(Distance.KILOMETER.displayName).isEqualTo("Kilometers")
        assertThat(Distance.KILOMETER.shorthand).isEqualTo("km")
        assertThat(Distance.MILE.displayName).isEqualTo("Miles")
        assertThat(Distance.MILE.shorthand).isEqualTo("mi")
    }

    @Test
    fun `toString returns displayName for each distance`() {
        assertThat(Distance.KILOMETER.toString()).isEqualTo("Kilometers")
        assertThat(Distance.MILE.toString()).isEqualTo("Miles")
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
