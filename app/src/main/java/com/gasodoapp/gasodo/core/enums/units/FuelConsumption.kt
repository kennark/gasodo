package com.gasodoapp.gasodo.core.enums.units

import androidx.compose.runtime.Composable

/**
 * Unit fuel consumption is shown in.
 *
 * The two entries describe the same figure from opposite sides: [LIQUID_PER_100_DISTANCE] is a
 * volume burnt over a hundred distance units (for example `L/100 km`), while
 * [DISTANCE_PER_LIQUID] is the distance covered on one volume unit (for example `mi per gal`).
 *
 * An entry only decides which of the two ratios is presented. The distance and volume units that
 * take part in it are chosen separately in [com.gasodoapp.gasodo.core.settings.Units], which is why
 * both are passed in to every label helper below.
 */
enum class FuelConsumption {

    /**
     * Volume of fuel burnt over a hundred distance units, for example `L/100 km`.
     */
    LIQUID_PER_100_DISTANCE,

    /**
     * Distance covered on one volume unit, for example `mi per gal`.
     */
    DISTANCE_PER_LIQUID;

    /**
     * Localised label for the unit selector, spelled out in words, for example
     * `Liters per 100 Kilometers` or `Miles per Gallon`.
     */
    @Composable
    fun getName(distance: Distance, liquid: Liquid): String {
        if (this == LIQUID_PER_100_DISTANCE) {
            return "${liquid.name()} per 100 ${distance.name()}"
        }
        return "${distance.name()} per ${liquid.singularName()}"
    }

    /**
     * Compact suffix rendered next to a consumption value, for example `L/100 km` or `mi per gal`.
     */
    fun getShorthand(distance: Distance, liquid: Liquid): String {
        if (this == LIQUID_PER_100_DISTANCE) {
            return "${liquid.shorthand}/100 ${distance.shorthand}"
        }
        return "${distance.shorthand} per ${liquid.shorthand}"
    }

    companion object {

        /**
         * Entry used when nothing has been stored yet.
         */
        val DEFAULT: FuelConsumption = LIQUID_PER_100_DISTANCE

        /**
         * Resolves a persisted [name] (an enum constant name), falling back to [DEFAULT] when the
         * value is absent or unknown.
         *
         * An unknown name can only happen after downgrading from a version that knew more entries,
         * or from a hand edited preferences file, so treating it as the default keeps the app
         * usable instead of crashing on `valueOf`.
         */
        fun fromName(name: String?): FuelConsumption =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
