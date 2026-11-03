package com.gasodoapp.gasodo.core.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.gasodoapp.gasodo.core.enums.ThemeMode
import com.gasodoapp.gasodo.core.settings.SettingsRepositoryKeys.THEME_MODE_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/**
 * Implementation of [SettingsRepository] backed by Preferences DataStore.
 */
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<AppSettings> =
        dataStore.data
            // A corrupt or unreadable file must not crash the app, fall back to the defaults.
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { preferences ->
                AppSettings(themeMode = preferences.readThemeMode())
            }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }
}

/**
 * Reads the stored [ThemeMode], falling back to [ThemeMode.SYSTEM] for a missing or unknown value.
 *
 * An unknown name can only happen after downgrading from a version that knew more modes, so
 * treating it as the default keeps the app usable instead of crashing on `valueOf`.
 */
private fun Preferences.readThemeMode(): ThemeMode {
    val stored = this[THEME_MODE_KEY] ?: return ThemeMode.SYSTEM
    return ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.SYSTEM
}
