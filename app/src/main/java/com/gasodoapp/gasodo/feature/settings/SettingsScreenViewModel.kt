package com.gasodoapp.gasodo.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gasodoapp.gasodo.core.enums.ThemeMode
import com.gasodoapp.gasodo.core.enums.units.Currency
import com.gasodoapp.gasodo.core.enums.units.Distance
import com.gasodoapp.gasodo.core.enums.units.FuelConsumption
import com.gasodoapp.gasodo.core.enums.units.Liquid
import com.gasodoapp.gasodo.core.settings.AppSettings
import com.gasodoapp.gasodo.core.settings.SettingsRepository
import com.gasodoapp.gasodo.core.settings.Units
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Exposes the stored preferences and forwards user edits to [SettingsRepository].
 */
@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> =
        repository.settings
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AppSettings(),
            )

    val units: StateFlow<Units> =
        repository.units
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = Units(),
            )

    /**
     * Stores the selected theme, including [ThemeMode.SYSTEM] to go back to following the device.
     */
    fun onThemeModeChange(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    /**
     * Stores the unit distances and mileage are shown in.
     */
    fun onDistanceUnitChange(unit: Distance) {
        viewModelScope.launch {
            repository.setDistanceUnit(unit)
        }
    }

    /**
     * Stores the unit fuel volumes are shown in.
     */
    fun onLiquidUnitChange(unit: Liquid) {
        viewModelScope.launch {
            repository.setLiquidUnit(unit)
        }
    }

    /**
     * Stores the currency prices and totals are shown in.
     */
    fun onCurrencySymbolChange(symbol: Currency) {
        viewModelScope.launch {
            repository.setCurrencySymbol(symbol)
        }
    }

    /**
     * Stores the setting for Material You dynamic theme.
     */
    fun onDynamicThemeChange(dynamic: Boolean) {
        viewModelScope.launch {
            repository.setDynamicTheme(dynamic)
        }
    }

    /**
     * Stores the ratio fuel consumption is presented as.
     */
    fun onFuelConsumptionUnitChange(unit: FuelConsumption) {
        viewModelScope.launch {
            repository.setFuelConsumption(unit)
        }
    }
}
