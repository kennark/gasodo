package com.gasodoapp.gasodo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gasodoapp.gasodo.core.enums.ThemeMode
import com.gasodoapp.gasodo.core.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Exposes the preferences that have to be known before the UI tree is composed.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    /**
     * Stored theme choice.
     *
     * Resolving [ThemeMode.SYSTEM] to the actual device setting is left to the composable, because
     * `isSystemInDarkTheme()` has to be read there to react to configuration changes.
     */
    val themeMode: StateFlow<ThemeMode> =
        settingsRepository.settings
            .map { it.themeMode }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = ThemeMode.SYSTEM,
            )

    /**
     * Stored choice between the Material You wallpaper colours and the app's own colour scheme.
     *
     * Defaults to `true`, so installations that never touched the new setting keep the palette
     * they had before it existed. Whether the platform can actually serve wallpaper colours is
     * checked in [com.gasodoapp.gasodo.ui.theme.GasodoTheme], not here.
     */
    val useDynamicTheme: StateFlow<Boolean> =
        settingsRepository.settings
            .map { it.useDynamicTheme }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = true
            )
}
