package com.gasodoapp.gasodo

import com.gasodoapp.gasodo.core.enums.ThemeMode
import com.gasodoapp.gasodo.core.settings.AppSettings
import com.gasodoapp.gasodo.core.settings.SettingsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [MainViewModel].
 * Tests cover the preferences the theme is resolved from before the UI tree is composed.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var repository: SettingsRepository
    private lateinit var stored: MutableStateFlow<AppSettings>

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = mockk()
        stored = MutableStateFlow(AppSettings())
        every { repository.settings } returns stored
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * The flow is started eagerly, so the value is available without an active collector.
     */
    @Test
    fun `theme mode defaults to SYSTEM`() = runTest {
        // Arrange
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        // Act
        val themeMode = viewModel.themeMode.value

        // Assert
        assertThat(themeMode).isEqualTo(ThemeMode.SYSTEM)
    }

    @Test
    fun `stored theme mode is exposed before collection starts`() = runTest {
        // Arrange
        stored.value = AppSettings(themeMode = ThemeMode.DARK)
        val viewModel = MainViewModel(repository)

        // Act
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.themeMode.value).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun `dynamic theme defaults to enabled`() = runTest {
        // Arrange
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        // Act
        val useDynamicTheme = viewModel.useDynamicTheme.value

        // Assert
        assertThat(useDynamicTheme).isTrue()
    }

    @Test
    fun `stored dynamic theme is exposed before collection starts`() = runTest {
        // Arrange
        stored.value = AppSettings(useDynamicTheme = false)
        val viewModel = MainViewModel(repository)

        // Act
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.useDynamicTheme.value).isFalse()
    }

    /**
     * A change stored by the settings screen has to reach the already running activity, otherwise
     * toggling the switch would need a restart to take effect.
     */
    @Test
    fun `a stored change is picked up by the exposed state`() = runTest {
        // Arrange
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        // Act
        stored.value = AppSettings(themeMode = ThemeMode.LIGHT, useDynamicTheme = false)
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.themeMode.value).isEqualTo(ThemeMode.LIGHT)
        assertThat(viewModel.useDynamicTheme.value).isFalse()
    }

    /**
     * The two appearance settings are independent, so one must not overwrite the other.
     */
    @Test
    fun `theme mode and dynamic theme are resolved independently`() = runTest {
        // Arrange
        stored.value = AppSettings(themeMode = ThemeMode.DARK, useDynamicTheme = true)
        val viewModel = MainViewModel(repository)

        // Act
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.themeMode.value).isEqualTo(ThemeMode.DARK)
        assertThat(viewModel.useDynamicTheme.value).isTrue()
    }
}
