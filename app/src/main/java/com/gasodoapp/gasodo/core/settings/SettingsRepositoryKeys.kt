package com.gasodoapp.gasodo.core.settings

import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsRepositoryKeys {

    val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

    val DISTANCE_UNIT_KEY = stringPreferencesKey("distance_unit")

    val LIQUID_UNIT_KEY = stringPreferencesKey("liquid_unit")

    val CURRENCY_SYMBOL_KEY = stringPreferencesKey("currency_symbol")
}
