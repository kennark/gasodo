package com.gasodoapp.gasodo.core.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.gasodoapp.gasodo.core.enums.ThemeMode
import com.gasodoapp.gasodo.core.enums.units.Distance
import com.gasodoapp.gasodo.core.enums.units.Liquid
import com.gasodoapp.gasodo.core.settings.SettingsRepositoryKeys.DISTANCE_UNIT_KEY
import com.gasodoapp.gasodo.core.settings.SettingsRepositoryKeys.LIQUID_UNIT_KEY
import com.gasodoapp.gasodo.core.settings.SettingsRepositoryKeys.THEME_MODE_KEY
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Unit tests for [SettingsRepositoryImpl].
 * Tests run against a real [Preferences] data store backed by a temporary file.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepository

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = TestScope(UnconfinedTestDispatcher()),
            produceFile = { temporaryFolder.newFile("settings.preferences_pb") }
        )
        repository = SettingsRepositoryImpl(dataStore)
    }

    @Test
    fun `theme mode defaults to SYSTEM`() = runTest {
        // Act
        val settings = repository.settings.first()

        // Assert
        assertThat(settings.themeMode).isEqualTo(ThemeMode.SYSTEM)
    }

    @Test
    fun `setThemeMode DARK is emitted by the settings flow`() = runTest {
        // Act
        repository.setThemeMode(ThemeMode.DARK)
        val settings = repository.settings.first()

        // Assert
        assertThat(settings.themeMode).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun `setThemeMode LIGHT is emitted by the settings flow`() = runTest {
        // Act
        repository.setThemeMode(ThemeMode.LIGHT)
        val settings = repository.settings.first()

        // Assert
        assertThat(settings.themeMode).isEqualTo(ThemeMode.LIGHT)
    }

    @Test
    fun `setThemeMode overwrites the previously stored value`() = runTest {
        // Act
        repository.setThemeMode(ThemeMode.DARK)
        repository.setThemeMode(ThemeMode.LIGHT)
        val settings = repository.settings.first()

        // Assert
        assertThat(settings.themeMode).isEqualTo(ThemeMode.LIGHT)
    }

    @Test
    fun `setThemeMode SYSTEM is stored like any other mode`() = runTest {
        // Act
        repository.setThemeMode(ThemeMode.DARK)
        repository.setThemeMode(ThemeMode.SYSTEM)
        val settings = repository.settings.first()

        // Assert
        assertThat(settings.themeMode).isEqualTo(ThemeMode.SYSTEM)
    }

    @Test
    fun `an unknown stored value falls back to SYSTEM`() = runTest {
        // Arrange
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = "SEPIA"
        }

        // Act
        val settings = repository.settings.first()

        // Assert
        assertThat(settings.themeMode).isEqualTo(ThemeMode.SYSTEM)
    }

    @Test
    fun `units default to KILOMETER and LITER`() = runTest {
        // Act
        val units = repository.units.first()

        // Assert
        assertThat(units.distance).isEqualTo(Distance.KILOMETER)
        assertThat(units.liquid).isEqualTo(Liquid.LITER)
    }

    @Test
    fun `setDistanceUnit MILE is emitted by the units flow`() = runTest {
        // Act
        repository.setDistanceUnit(Distance.MILE)
        val units = repository.units.first()

        // Assert
        assertThat(units.distance).isEqualTo(Distance.MILE)
    }

    @Test
    fun `setLiquidUnit GALLON is emitted by the units flow`() = runTest {
        // Act
        repository.setLiquidUnit(Liquid.GALLON)
        val units = repository.units.first()

        // Assert
        assertThat(units.liquid).isEqualTo(Liquid.GALLON)
    }

    @Test
    fun `setDistanceUnit overwrites the previously stored value`() = runTest {
        // Act
        repository.setDistanceUnit(Distance.MILE)
        repository.setDistanceUnit(Distance.KILOMETER)
        val units = repository.units.first()

        // Assert
        assertThat(units.distance).isEqualTo(Distance.KILOMETER)
    }

    @Test
    fun `setLiquidUnit overwrites the previously stored value`() = runTest {
        // Act
        repository.setLiquidUnit(Liquid.GALLON)
        repository.setLiquidUnit(Liquid.LITER)
        val units = repository.units.first()

        // Assert
        assertThat(units.liquid).isEqualTo(Liquid.LITER)
    }

    @Test
    fun `storing one unit does not change the other`() = runTest {
        // Act
        repository.setDistanceUnit(Distance.MILE)
        repository.setLiquidUnit(Liquid.GALLON)
        val units = repository.units.first()

        // Assert
        assertThat(units.distance).isEqualTo(Distance.MILE)
        assertThat(units.liquid).isEqualTo(Liquid.GALLON)
    }

    @Test
    fun `an unknown stored distance unit falls back to KILOMETER`() = runTest {
        // Arrange
        dataStore.edit { preferences ->
            preferences[DISTANCE_UNIT_KEY] = "FURLONG"
        }

        // Act
        val units = repository.units.first()

        // Assert
        assertThat(units.distance).isEqualTo(Distance.KILOMETER)
    }

    @Test
    fun `an unknown stored liquid unit falls back to LITER`() = runTest {
        // Arrange
        dataStore.edit { preferences ->
            preferences[LIQUID_UNIT_KEY] = "PINT"
        }

        // Act
        val units = repository.units.first()

        // Assert
        assertThat(units.liquid).isEqualTo(Liquid.LITER)
    }
}
