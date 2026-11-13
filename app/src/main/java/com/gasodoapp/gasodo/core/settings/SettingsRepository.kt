package com.gasodoapp.gasodo.core.settings

import com.gasodoapp.gasodo.core.enums.ThemeMode
import com.gasodoapp.gasodo.core.enums.units.Currency
import com.gasodoapp.gasodo.core.enums.units.Distance
import com.gasodoapp.gasodo.core.enums.units.FuelConsumption
import com.gasodoapp.gasodo.core.enums.units.Liquid
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
     * Current unit preferences. Emits again whenever either unit changes.
     *
     * Separate from [settings] so consumers that only format values do not depend on unrelated
     * preferences.
     */
    val units: Flow<Units>

    /**
     * Stores the [mode] the app should render with, overriding the system setting.
     */
    suspend fun setThemeMode(mode: ThemeMode)

    /**
     * Stores whether the app uses the Material You wallpaper colours or its own colour scheme.
     *
     * Only has a visible effect on Android 12 and above.
     */
    suspend fun setDynamicTheme(dynamic: Boolean)

    /**
     * Stores the unit used for distances and mileage.
     */
    suspend fun setDistanceUnit(unit: Distance)

    /**
     * Stores the unit used for fuel volumes.
     */
    suspend fun setLiquidUnit(unit: Liquid)

    /**
     * Stores the currency prices and totals are shown in.
     */
    suspend fun setCurrencySymbol(symbol: Currency)

    /**
     * Stores the ratio fuel consumption is presented as.
     */
    suspend fun setFuelConsumption(unit: FuelConsumption)
}
