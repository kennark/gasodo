package com.gasodoapp.gasodo.core.enums.units

/**
 * Unit used to display and enter distances.
 *
 * [displayName] is the plural form the unit selectors list, [singularName] is the form used by
 * labels that read as a quantity of one (for example `Price per Kilometer`) and [shorthand] is the
 * suffix rendered next to a value (for example `km`).
 *
 * A full name is always spelled out rather than abbreviated so that it stays readable in a
 * sentence, while [shorthand] keeps the numeric fields compact.
 */
enum class Distance(val displayName: String, val singularName: String, val shorthand: String) {
    KILOMETER("Kilometers", "Kilometer", "km"),
    MILE("Miles", "Mile", "mi");

    override fun toString(): String = displayName

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
