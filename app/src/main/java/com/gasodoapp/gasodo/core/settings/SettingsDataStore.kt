package com.gasodoapp.gasodo.core.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private const val SETTINGS_DATA_STORE_NAME = "settings"

/**
 * Application wide preferences data store.
 *
 * The delegate must be declared exactly once per file, the Android Context is the receiver so the
 * same instance is returned for every call.
 */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = SETTINGS_DATA_STORE_NAME
)
