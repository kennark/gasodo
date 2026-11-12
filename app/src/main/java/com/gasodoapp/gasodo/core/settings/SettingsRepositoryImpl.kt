package com.gasodoapp.gasodo.core.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.gasodoapp.gasodo.core.enums.ThemeMode
import com.gasodoapp.gasodo.core.enums.units.Currency
import com.gasodoapp.gasodo.core.enums.units.Distance
import com.gasodoapp.gasodo.core.enums.units.Liquid
import com.gasodoapp.gasodo.core.settings.SettingsRepositoryKeys.CURRENCY_SYMBOL_KEY
import com.gasodoapp.gasodo.core.settings.SettingsRepositoryKeys.DISTANCE_UNIT_KEY
import com.gasodoapp.gasodo.core.settings.SettingsRepositoryKeys.DYNAMIC_THEME_KEY
import com.gasodoapp.gasodo.core.settings.SettingsRepositoryKeys.LIQUID_UNIT_KEY
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
        readablePreferences()
            .map { preferences ->
                AppSettings(
                    themeMode = ThemeMode.fromName(preferences[THEME_MODE_KEY]),
                    useDynamicTheme = preferences[DYNAMIC_THEME_KEY] ?: true
                )
            }

    override val units: Flow<Units> =
        readablePreferences()
            .map { preferences ->
                Units(
                    distance = Distance.fromName(preferences[DISTANCE_UNIT_KEY]),
                    liquid = Liquid.fromName(preferences[LIQUID_UNIT_KEY]),
                    currency = Currency.fromName(preferences[CURRENCY_SYMBOL_KEY]),
                )
            }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    override suspend fun setDynamicTheme(dynamic: Boolean) {
        dataStore.edit { preferences ->
            preferences[DYNAMIC_THEME_KEY] = dynamic
        }
    }

    override suspend fun setDistanceUnit(unit: Distance) {
        dataStore.edit { preferences ->
            preferences[DISTANCE_UNIT_KEY] = unit.name
        }
    }

    override suspend fun setLiquidUnit(unit: Liquid) {
        dataStore.edit { preferences ->
            preferences[LIQUID_UNIT_KEY] = unit.name
        }
    }

    override suspend fun setCurrencySymbol(symbol: Currency) {
        dataStore.edit { preferences ->
            preferences[CURRENCY_SYMBOL_KEY] = symbol.name
        }
    }

    /**
     * Raw preferences, with unreadable files replaced by an empty set.
     *
     * A corrupt or unreadable file must not crash the app, falling back to the defaults lets every
     * `map` below resolve its own default through the enum `fromName` helpers.
     */
    private fun readablePreferences(): Flow<Preferences> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
}
