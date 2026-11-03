package com.gasodoapp.gasodo.core.enums

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * Colour scheme the app should use.
 *
 * [SYSTEM] defers to the device setting, the others override it explicitly.
 */
enum class ThemeMode(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark");

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

    override fun toString(): String = displayName
}
