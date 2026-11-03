package com.gasodoapp.gasodo.core.settings

import com.gasodoapp.gasodo.core.enums.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Repository for user preferences.
 */
interface SettingsRepository {

    /**
     * Current preferences. Emits again whenever any value changes.
     */
    val settings: Flow<AppSettings>

    /**
     * Stores the [mode] the app should render with, overriding the system setting.
     */
    suspend fun setThemeMode(mode: ThemeMode)
}
