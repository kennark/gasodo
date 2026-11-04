package com.gasodoapp.gasodo.core.enums.units

/**
 * Unit used to display and enter distances.
 */
enum class Distance(val displayName: String, val shorthand: String) {
    KILOMETER("Kilometers", "km"),
    MILE("Miles", "mi");

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
