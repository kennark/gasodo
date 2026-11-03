package com.gasodoapp.gasodo.feature.settings

import com.gasodoapp.gasodo.core.enums.ThemeMode
import com.gasodoapp.gasodo.core.settings.AppSettings
import com.gasodoapp.gasodo.core.settings.SettingsRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [SettingsScreenViewModel].
 * Tests cover the initial state and the theme mode selection.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsScreenViewModelTest {

    private lateinit var repository: SettingsRepository
    private lateinit var viewModel: SettingsScreenViewModel
    private lateinit var stored: MutableStateFlow<AppSettings>

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = mockk()
        stored = MutableStateFlow(AppSettings())
        every { repository.settings } returns stored
        coEvery { repository.setThemeMode(any()) } just runs
        viewModel = SettingsScreenViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `settings default to SYSTEM`() = runTest {
        // Arrange
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.settings.collect {}
        }

        // Act
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.settings.value).isEqualTo(AppSettings())
        assertThat(viewModel.settings.value.themeMode).isEqualTo(ThemeMode.SYSTEM)

        job.cancel()
    }

    @Test
    fun `stored theme mode is exposed by the settings state`() = runTest {
        // Arrange
        stored.value = AppSettings(themeMode = ThemeMode.DARK)
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.settings.collect {}
        }

        // Act
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.settings.value.themeMode).isEqualTo(ThemeMode.DARK)

        job.cancel()
    }

    @Test
    fun `onThemeModeChange DARK stores the value in the repository`() = runTest {
        // Act
        viewModel.onThemeModeChange(ThemeMode.DARK)
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { repository.setThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun `onThemeModeChange LIGHT stores the value in the repository`() = runTest {
        // Act
        viewModel.onThemeModeChange(ThemeMode.LIGHT)
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { repository.setThemeMode(ThemeMode.LIGHT) }
    }

    @Test
    fun `onThemeModeChange SYSTEM goes back to following the device`() = runTest {
        // Act
        viewModel.onThemeModeChange(ThemeMode.SYSTEM)
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { repository.setThemeMode(ThemeMode.SYSTEM) }
    }
}
