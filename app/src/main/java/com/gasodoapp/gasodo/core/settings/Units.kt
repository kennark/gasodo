package com.gasodoapp.gasodo.core.settings

import com.gasodoapp.gasodo.core.enums.units.Currency
import com.gasodoapp.gasodo.core.enums.units.Distance
import com.gasodoapp.gasodo.core.enums.units.FuelConsumption
import com.gasodoapp.gasodo.core.enums.units.Liquid

/**
 * Immutable snapshot of the user's unit preferences.
 *
 * Kept separate from [AppSettings] so code that only needs units does not have to observe every
 * other preference.
 */
data class Units(
    /**
     * Unit for distances and mileage.
     *
     * Defaults to [Distance.DEFAULT].
     */
    val distance: Distance = Distance.DEFAULT,

    /**
     * Unit for fuel volumes.
     *
     * Defaults to [Liquid.DEFAULT].
     */
    val liquid: Liquid = Liquid.DEFAULT,

    /**
     * Currency prices and totals are shown in.
     *
     * Defaults to [Currency.DEFAULT].
     */
    val currency: Currency = Currency.DEFAULT,

    /**
     * Ratio fuel consumption is presented as: either a volume per hundred distance units or a
     * distance per volume unit.
     *
     * It only selects which of the two ratios is shown; the units taking part in it are [distance]
     * and [liquid].
     *
     * Defaults to [FuelConsumption.DEFAULT].
     */
    val fuelConsumption: FuelConsumption = FuelConsumption.DEFAULT,
) {

    // region Display labels
    //
    // The properties below are the only strings the UI should render next to a value. They are
    // display-only: no value is converted, the stored number is always shown as-is.

    /**
     * Suffix for a distance or mileage value, for example `km`.
     */
    val distanceLabel: String
        get() = distance.shorthand

    /**
     * Suffix for a fuel volume, for example `L`.
     */
    val liquidLabel: String
        get() = liquid.shorthand

    /**
     * Prefix or suffix for a price or total, for example `€`.
     */
    val currencyLabel: String
        get() = currency.symbol

    /**
     * Label for a price charged per unit of fuel, for example `€/L`.
     */
    val pricePerLiquidLabel: String
        get() = "${currency.symbol}/${liquid.shorthand}"

    /**
     * Label for fuel consumption, for example `L/100 km` or `mi per gal`, depending on
     * [fuelConsumption].
     */
    val consumptionLabel: String
        get() = fuelConsumption.getShorthand(distance, liquid)

    /**
     * Label for a cost accrued over a hundred distance units, for example `€/100 km`.
     */
    val costPerDistanceLabel: String
        get() = "${currency.symbol}/100 ${distance.shorthand}"

    // endregion
}
