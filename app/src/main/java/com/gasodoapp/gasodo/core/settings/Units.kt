package com.gasodoapp.gasodo.core.settings

import com.gasodoapp.gasodo.core.enums.units.Currency
import com.gasodoapp.gasodo.core.enums.units.Distance
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
)
