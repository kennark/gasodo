package com.gasodoapp.gasodo.core.enums.units

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [FuelConsumption].
 *
 * Only [FuelConsumption.getShorthand] is asserted here, because it is built from the untranslated
 * unit symbols. [FuelConsumption.getName] reads string resources and therefore cannot run in a
 * plain JVM test.
 */
class FuelConsumptionTest {

    @Test
    fun `there are exactly 2 fuel consumption units`() {
        assertThat(FuelConsumption.entries.size).isEqualTo(2)
    }

    @Test
    fun `DEFAULT is LIQUID_PER_100_DISTANCE`() {
        assertThat(FuelConsumption.DEFAULT).isEqualTo(FuelConsumption.LIQUID_PER_100_DISTANCE)
    }

    @Test
    fun `stored enum names resolve to their entry`() {
        FuelConsumption.entries.forEach { unit ->
            assertThat(FuelConsumption.fromName(unit.name)).isEqualTo(unit)
        }
    }

    @Test
    fun `a missing stored value falls back to the default`() {
        assertThat(FuelConsumption.fromName(null)).isEqualTo(FuelConsumption.DEFAULT)
    }

    @Test
    fun `an unknown stored value falls back to the default`() {
        assertThat(FuelConsumption.fromName("KILOMETERS_PER_TANK")).isEqualTo(
            FuelConsumption.DEFAULT,
        )
    }

    /**
     * A stored display label (`L/100 km`) is not an enum constant name, so it must not be accepted.
     */
    @Test
    fun `a stored label is not accepted as an enum name`() {
        assertThat(FuelConsumption.fromName("L/100 km")).isEqualTo(FuelConsumption.DEFAULT)
    }

    @Test
    fun `liquid per distance renders a per hundred shorthand`() {
        val shorthand = FuelConsumption.LIQUID_PER_100_DISTANCE.getShorthand(
            Distance.KILOMETER,
            Liquid.LITER
        )

        assertThat(shorthand).isEqualTo("L/100 km")
    }

    @Test
    fun `distance per liquid renders a per unit shorthand`() {
        val shorthand = FuelConsumption.DISTANCE_PER_LIQUID.getShorthand(
            Distance.MILE,
            Liquid.GALLON
        )

        assertThat(shorthand).isEqualTo("mi per gal")
    }

    /**
     * The distance and liquid units are picked independently of the ratio, so every combination
     * has to produce a label rather than an empty or broken string.
     */
    @Test
    fun `every combination of distance and liquid renders a label`() {
        FuelConsumption.entries.forEach { unit ->
            Distance.entries.forEach { distance ->
                Liquid.entries.forEach { liquid ->
                    val shorthand = unit.getShorthand(distance, liquid)

                    assertThat(shorthand).contains(distance.shorthand)
                    assertThat(shorthand).contains(liquid.shorthand)
                }
            }
        }
    }

    /**
     * Two entries rendering the same label would make the unit selector impossible to tell apart.
     */
    @Test
    fun `labels are unique for every combination of distance and liquid`() {
        Distance.entries.forEach { distance ->
            Liquid.entries.forEach { liquid ->
                val labels = FuelConsumption.entries.map { it.getShorthand(distance, liquid) }

                assertThat(labels).containsNoDuplicates()
            }
        }
    }
}
