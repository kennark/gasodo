package com.gasodoapp.gasodo.core.enums

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gasodoapp.gasodo.R

/**
 * Colour scheme the app should use.
 *
 * [SYSTEM] defers to the device setting, the others override it explicitly.
 *
 * [labelRes] is the user facing name and [descriptionRes] the one line explanation shown under
 * the selector. Both are resolved at the UI layer so the wording follows the app language.
 */
enum class ThemeMode(
    @param:StringRes val labelRes: Int,
    @param:StringRes val descriptionRes: Int,
) {
    SYSTEM(R.string.theme_mode_system, R.string.settings_theme_system_description),
    LIGHT(R.string.theme_mode_light, R.string.settings_theme_light_description),
    DARK(R.string.theme_mode_dark, R.string.settings_theme_dark_description);

    /**
     * Whether the app should render with the dark colour scheme.
     *
     * [SYSTEM] reads the device setting, which is why this has to be resolved inside a composable.
     */
    @Composable
    fun isDarkTheme(): Boolean = when (this) {
        SYSTEM -> isSystemInDarkTheme()
        LIGHT -> false
        DARK -> true
    }

    /**
     * Localised name of this mode, for rendering inside a composable.
     */
    @Composable
    fun label(): String = stringResource(labelRes)

    /**
     * Localised explanation of what this mode means, for rendering inside a composable.
     */
    @Composable
    fun description(): String = stringResource(descriptionRes)

    companion object {

        /**
         * Entry used when nothing has been stored yet.
         */
        val DEFAULT: ThemeMode = SYSTEM

        /**
         * Resolves a persisted [name] (an enum constant name), falling back to [DEFAULT] when the
         * value is absent or unknown.
         *
         * An unknown name can only happen after downgrading from a version that knew more modes,
         * or from a hand edited preferences file, so treating it as the default keeps the app
         * usable instead of crashing on `valueOf`.
         */
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
