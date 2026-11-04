package com.gasodoapp.gasodo.core.settings

import com.gasodoapp.gasodo.core.enums.ThemeMode

/**
 * Immutable snapshot of the general user configurable preferences.
 *
 * Grouping the values in a single data class keeps [SettingsRepository] stable when new settings
 * are introduced later on. Unit preferences live in their own [Units] class.
 */
data class AppSettings(
    /**
     * Colour scheme to render the app with.
     *
     * Defaults to [ThemeMode.SYSTEM].
     */
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
