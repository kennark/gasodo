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

    /**
     * Whether to derive the colour scheme from the wallpaper through Material You, instead of using
     * the app's own palette.
     *
     * Defaults to `true`. Ignored below Android 12, where the platform cannot provide
     * wallpaper colours.
     */
    val useDynamicTheme: Boolean = true,
)
