package com.gasodoapp.gasodo.core.settings

import com.gasodoapp.gasodo.core.enums.units.Currency
import com.gasodoapp.gasodo.core.enums.units.Distance
import com.gasodoapp.gasodo.core.enums.units.FuelConsumption
import com.gasodoapp.gasodo.core.enums.units.Liquid
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for the derived display labels on [Units].
 *
 * Only the shorthand and symbol based labels are covered here. The labels that spell a unit out in
 * words (`Liter`, `Kilometer`) are string resources and are resolved in the UI layer through
 * `Units.liquidSingularLabel()`, so they cannot be asserted from a plain JVM test.
 */
class UnitsTest {

    @Test
    fun `defaults render metric labels`() {
        val units = Units()

        assertThat(units.distanceLabel).isEqualTo("km")
        assertThat(units.liquidLabel).isEqualTo("L")
        assertThat(units.currencyLabel).isEqualTo("€")
        assertThat(units.pricePerLiquidLabel).isEqualTo("€/L")
        assertThat(units.consumptionLabel).isEqualTo("L/100 km")
        assertThat(units.costPerDistanceLabel).isEqualTo("€/100 km")
    }

    @Test
    fun `imperial units render imperial labels`() {
        val units = Units(
            distance = Distance.MILE,
            liquid = Liquid.GALLON,
            currency = Currency.USD,
        )

        assertThat(units.distanceLabel).isEqualTo("mi")
        assertThat(units.liquidLabel).isEqualTo("gal")
        assertThat(units.currencyLabel).isEqualTo("$")
        assertThat(units.pricePerLiquidLabel).isEqualTo("$/gal")
        assertThat(units.consumptionLabel).isEqualTo("gal/100 mi")
        assertThat(units.costPerDistanceLabel).isEqualTo("$/100 mi")
    }

    @Test
    fun `labels compose each unit independently`() {
        val units = Units(
            distance = Distance.MILE,
            liquid = Liquid.LITER,
            currency = Currency.GBP,
        )

        assertThat(units.distanceLabel).isEqualTo("mi")
        assertThat(units.liquidLabel).isEqualTo("L")
        assertThat(units.currencyLabel).isEqualTo("£")
        assertThat(units.pricePerLiquidLabel).isEqualTo("£/L")
        assertThat(units.consumptionLabel).isEqualTo("L/100 mi")
        assertThat(units.costPerDistanceLabel).isEqualTo("£/100 mi")
    }

    @Test
    fun `fuel consumption defaults to liquid per hundred distance`() {
        assertThat(Units().fuelConsumption).isEqualTo(FuelConsumption.LIQUID_PER_100_DISTANCE)
    }

    /**
     * Switching the ratio has to change the consumption label, otherwise the value rendered next to
     * it would be read in the wrong unit.
     */
    @Test
    fun `distance per liquid inverts the consumption label`() {
        val units = Units(
            distance = Distance.MILE,
            liquid = Liquid.GALLON,
            fuelConsumption = FuelConsumption.DISTANCE_PER_LIQUID,
        )

        assertThat(units.consumptionLabel).isEqualTo("mi per gal")
    }

    /**
     * The cost figure is always accrued over a hundred distance units, so it must not follow the
     * consumption ratio above.
     */
    @Test
    fun `cost label ignores the fuel consumption ratio`() {
        val units = Units(
            distance = Distance.MILE,
            currency = Currency.USD,
            fuelConsumption = FuelConsumption.DISTANCE_PER_LIQUID,
        )

        assertThat(units.costPerDistanceLabel).isEqualTo("$/100 mi")
    }
}
