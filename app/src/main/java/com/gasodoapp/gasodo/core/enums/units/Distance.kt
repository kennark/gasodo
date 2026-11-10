package com.gasodoapp.gasodo.core.enums.units

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gasodoapp.gasodo.R

/**
 * Unit used to display and enter distances.
 *
 * [nameRes] is the plural form the unit selectors list, [singularNameRes] is the form used by
 * labels that read as a quantity of one (for example `Price per Kilometer`) and [shorthand] is the
 * suffix rendered next to a value (for example `km`).
 *
 * A full name is always spelled out rather than abbreviated so that it stays readable in a
 * sentence, while [shorthand] keeps the numeric fields compact.
 *
 * [shorthand] is deliberately a plain `String` rather than a resource: `km` and `mi` are
 * international symbols and are not translated, which also keeps [com.gasodoapp.gasodo.core.settings.Units]
 * usable from plain unit tests.
 */
enum class Distance(
    @param:StringRes val nameRes: Int,
    @param:StringRes val singularNameRes: Int,
    val shorthand: String,
) {
    KILOMETER(R.string.unit_distance_kilometers, R.string.unit_distance_kilometer, "km"),
    MILE(R.string.unit_distance_miles, R.string.unit_distance_mile, "mi");

    /**
     * Localised plural name, for the unit selectors.
     */
    @Composable
    fun name(): String = stringResource(nameRes)

    /**
     * Localised singular name, for labels such as `Price per Kilometer`.
     */
    @Composable
    fun singularName(): String = stringResource(singularNameRes)

    companion object {

        /**
         * Entry used when nothing has been stored yet.
         */
        val DEFAULT: Distance = KILOMETER

        /**
         * Resolves a persisted [name] (an enum constant name), falling back to [DEFAULT] when the
         * value is absent or unknown.
         *
         * An unknown name can only happen after downgrading from a version that knew more entries,
         * or from a hand edited preferences file, so treating it as the default keeps the app
         * usable instead of crashing on `valueOf`.
         */
        fun fromName(name: String?): Distance =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
