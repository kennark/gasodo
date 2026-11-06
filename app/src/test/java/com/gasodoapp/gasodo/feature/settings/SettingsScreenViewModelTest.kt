package com.gasodoapp.gasodo.feature.settings

import com.gasodoapp.gasodo.core.enums.ThemeMode
import com.gasodoapp.gasodo.core.enums.units.Currency
import com.gasodoapp.gasodo.core.enums.units.Distance
import com.gasodoapp.gasodo.core.enums.units.Liquid
import com.gasodoapp.gasodo.core.settings.AppSettings
import com.gasodoapp.gasodo.core.settings.SettingsRepository
import com.gasodoapp.gasodo.core.settings.Units
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
 * Tests cover the initial state and the theme and unit selections.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsScreenViewModelTest {

    private lateinit var repository: SettingsRepository
    private lateinit var viewModel: SettingsScreenViewModel
    private lateinit var stored: MutableStateFlow<AppSettings>
    private lateinit var storedUnits: MutableStateFlow<Units>

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = mockk()
        stored = MutableStateFlow(AppSettings())
        storedUnits = MutableStateFlow(Units())
        every { repository.settings } returns stored
        every { repository.units } returns storedUnits
        coEvery { repository.setThemeMode(any()) } just runs
        coEvery { repository.setDistanceUnit(any()) } just runs
        coEvery { repository.setLiquidUnit(any()) } just runs
        coEvery { repository.setCurrencySymbol(any()) } just runs
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

    @Test
    fun `units default to KILOMETER and LITER`() = runTest {
        // Arrange
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.units.collect {}
        }

        // Act
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.units.value).isEqualTo(Units())
        assertThat(viewModel.units.value.distance).isEqualTo(Distance.KILOMETER)
        assertThat(viewModel.units.value.liquid).isEqualTo(Liquid.LITER)

        job.cancel()
    }

    @Test
    fun `stored units are exposed by the units state`() = runTest {
        // Arrange
        storedUnits.value = Units(distance = Distance.MILE, liquid = Liquid.GALLON)
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.units.collect {}
        }

        // Act
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.units.value.distance).isEqualTo(Distance.MILE)
        assertThat(viewModel.units.value.liquid).isEqualTo(Liquid.GALLON)

        job.cancel()
    }

    @Test
    fun `onDistanceUnitChange MILE stores the value in the repository`() = runTest {
        // Act
        viewModel.onDistanceUnitChange(Distance.MILE)
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { repository.setDistanceUnit(Distance.MILE) }
    }

    @Test
    fun `onLiquidUnitChange GALLON stores the value in the repository`() = runTest {
        // Act
        viewModel.onLiquidUnitChange(Liquid.GALLON)
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { repository.setLiquidUnit(Liquid.GALLON) }
    }

    @Test
    fun `units default to EUR`() = runTest {
        // Arrange
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.units.collect {}
        }

        // Act
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.units.value.currency).isEqualTo(Currency.EUR)

        job.cancel()
    }

    @Test
    fun `stored currency is exposed by the units state`() = runTest {
        // Arrange
        storedUnits.value = Units(currency = Currency.EUR)
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.units.collect {}
        }

        // Act
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.units.value.currency).isEqualTo(Currency.EUR)

        job.cancel()
    }

    @Test
    fun `onCurrencySymbolChange EUR stores the value in the repository`() = runTest {
        // Act
        viewModel.onCurrencySymbolChange(Currency.EUR)
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { repository.setCurrencySymbol(Currency.EUR) }
    }

    @Test
    fun `onCurrencySymbolChange does not touch the other units`() = runTest {
        // Act
        viewModel.onCurrencySymbolChange(Currency.GBP)
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 0) { repository.setDistanceUnit(any()) }
        coVerify(exactly = 0) { repository.setLiquidUnit(any()) }
    }
}
