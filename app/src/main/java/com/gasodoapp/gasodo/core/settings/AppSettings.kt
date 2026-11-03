package com.gasodoapp.gasodo.core.settings

import com.gasodoapp.gasodo.core.enums.ThemeMode

/**
 * Immutable snapshot of every user configurable preference.
 *
 * Grouping the values in a single data class keeps [SettingsRepository] stable when new settings
 * are introduced later on.
 */
data class AppSettings(
    /**
     * Colour scheme to render the app with.
     *
     * Defaults to [ThemeMode.SYSTEM].
     */
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
