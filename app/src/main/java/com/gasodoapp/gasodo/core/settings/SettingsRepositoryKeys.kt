package com.gasodoapp.gasodo.core.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsRepositoryKeys {

    val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

    val DYNAMIC_THEME_KEY = booleanPreferencesKey("use_dynamic_theme")

    val DISTANCE_UNIT_KEY = stringPreferencesKey("distance_unit")

    val LIQUID_UNIT_KEY = stringPreferencesKey("liquid_unit")

    val CURRENCY_SYMBOL_KEY = stringPreferencesKey("currency_symbol")

    val FUEL_CONSUMPTION_KEY = stringPreferencesKey("fuel_consumption")
}
