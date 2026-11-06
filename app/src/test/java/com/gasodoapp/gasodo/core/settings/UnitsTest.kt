package com.gasodoapp.gasodo.core.settings

import com.gasodoapp.gasodo.core.enums.units.Currency
import com.gasodoapp.gasodo.core.enums.units.Distance
import com.gasodoapp.gasodo.core.enums.units.Liquid
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for the derived display labels on [Units].
 */
class UnitsTest {

    @Test
    fun `defaults render metric labels`() {
        val units = Units()

        assertThat(units.distanceLabel).isEqualTo("km")
        assertThat(units.liquidLabel).isEqualTo("L")
        assertThat(units.liquidSingularLabel).isEqualTo("Liter")
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
        assertThat(units.liquidSingularLabel).isEqualTo("Gallon")
        assertThat(units.currencyLabel).isEqualTo("$")
        assertThat(units.pricePerLiquidLabel).isEqualTo("$/gal")
        assertThat(units.consumptionLabel).isEqualTo("gal/100 mi")
        assertThat(units.costPerDistanceLabel).isEqualTo("$/100 mi")
    }

    /**
     * The singular label is substituted into sentences such as `Price per Liter`, so it must
     * follow the volume unit while staying independent of the other units.
     */
    @Test
    fun `singular liquid label follows the volume unit`() {
        assertThat(Units(liquid = Liquid.LITER).liquidSingularLabel).isEqualTo("Liter")
        assertThat(Units(liquid = Liquid.GALLON).liquidSingularLabel).isEqualTo("Gallon")
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
}
