package com.gasodoapp.gasodo.core.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.gasodoapp.gasodo.core.enums.ThemeMode
import com.gasodoapp.gasodo.core.enums.units.Currency
import com.gasodoapp.gasodo.core.enums.units.Distance
import com.gasodoapp.gasodo.core.enums.units.Liquid
import com.gasodoapp.gasodo.core.settings.SettingsRepositoryKeys.CURRENCY_SYMBOL_KEY
import com.gasodoapp.gasodo.core.settings.SettingsRepositoryKeys.DISTANCE_UNIT_KEY
import com.gasodoapp.gasodo.core.settings.SettingsRepositoryKeys.DYNAMIC_THEME_KEY
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
    fun `dynamic theme defaults to enabled`() = runTest {
        // Act
        val settings = repository.settings.first()

        // Assert
        assertThat(settings.useDynamicTheme).isTrue()
    }

    @Test
    fun `setDynamicTheme false is emitted by the settings flow`() = runTest {
        // Act
        repository.setDynamicTheme(false)
        val settings = repository.settings.first()

        // Assert
        assertThat(settings.useDynamicTheme).isFalse()
    }

    @Test
    fun `setDynamicTheme overwrites the previously stored value`() = runTest {
        // Act
        repository.setDynamicTheme(false)
        repository.setDynamicTheme(true)
        val settings = repository.settings.first()

        // Assert
        assertThat(settings.useDynamicTheme).isTrue()
    }

    /**
     * The two appearance settings are stored under separate keys, so toggling one must leave the
     * other alone.
     */
    @Test
    fun `storing the dynamic theme does not change the theme mode`() = runTest {
        // Act
        repository.setThemeMode(ThemeMode.DARK)
        repository.setDynamicTheme(false)
        val settings = repository.settings.first()

        // Assert
        assertThat(settings.themeMode).isEqualTo(ThemeMode.DARK)
        assertThat(settings.useDynamicTheme).isFalse()
    }

    @Test
    fun `storing the theme mode does not change the dynamic theme`() = runTest {
        // Act
        repository.setDynamicTheme(false)
        repository.setThemeMode(ThemeMode.LIGHT)
        val settings = repository.settings.first()

        // Assert
        assertThat(settings.themeMode).isEqualTo(ThemeMode.LIGHT)
        assertThat(settings.useDynamicTheme).isFalse()
    }

    /**
     * Covers preferences written before the dynamic theme existed, where the key is simply absent.
     */
    @Test
    fun `settings without a stored dynamic theme fall back to enabled`() = runTest {
        // Arrange
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = ThemeMode.DARK.name
        }

        // Act
        val settings = repository.settings.first()

        // Assert
        assertThat(settings.useDynamicTheme).isTrue()
        assertThat(settings.themeMode).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun `storing the dynamic theme does not change the units`() = runTest {
        // Act
        repository.setCurrencySymbol(Currency.CHF)
        repository.setDynamicTheme(false)
        val units = repository.units.first()

        // Assert
        assertThat(units.currency).isEqualTo(Currency.CHF)
        assertThat(units.distance).isEqualTo(Distance.KILOMETER)
        assertThat(units.liquid).isEqualTo(Liquid.LITER)
    }

    /**
     * Guards the persisted key name, so a rename does not silently reset every user's choice.
     */
    @Test
    fun `the dynamic theme is stored under the use_dynamic_theme key`() {
        assertThat(DYNAMIC_THEME_KEY.name).isEqualTo("use_dynamic_theme")
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

    @Test
    fun `currency defaults to EUR`() = runTest {
        // Act
        val units = repository.units.first()

        // Assert
        assertThat(units.currency).isEqualTo(Currency.EUR)
    }

    @Test
    fun `setCurrencySymbol EUR is emitted by the units flow`() = runTest {
        // Act
        repository.setCurrencySymbol(Currency.EUR)
        val units = repository.units.first()

        // Assert
        assertThat(units.currency).isEqualTo(Currency.EUR)
    }

    @Test
    fun `setCurrencySymbol overwrites the previously stored value`() = runTest {
        // Act
        repository.setCurrencySymbol(Currency.EUR)
        repository.setCurrencySymbol(Currency.GBP)
        val units = repository.units.first()

        // Assert
        assertThat(units.currency).isEqualTo(Currency.GBP)
    }

    @Test
    fun `storing a currency does not change the distance or liquid unit`() = runTest {
        // Act
        repository.setCurrencySymbol(Currency.JPY)
        val units = repository.units.first()

        // Assert
        assertThat(units.currency).isEqualTo(Currency.JPY)
        assertThat(units.distance).isEqualTo(Distance.KILOMETER)
        assertThat(units.liquid).isEqualTo(Liquid.LITER)
    }

    @Test
    fun `storing a distance or liquid unit does not change the currency`() = runTest {
        // Act
        repository.setCurrencySymbol(Currency.CHF)
        repository.setDistanceUnit(Distance.MILE)
        repository.setLiquidUnit(Liquid.GALLON)
        val units = repository.units.first()

        // Assert
        assertThat(units.currency).isEqualTo(Currency.CHF)
        assertThat(units.distance).isEqualTo(Distance.MILE)
        assertThat(units.liquid).isEqualTo(Liquid.GALLON)
    }

    @Test
    fun `an unknown stored currency falls back to EUR`() = runTest {
        // Arrange
        dataStore.edit { preferences ->
            preferences[CURRENCY_SYMBOL_KEY] = "XYZ"
        }

        // Act
        val units = repository.units.first()

        // Assert
        assertThat(units.currency).isEqualTo(Currency.EUR)
    }

    @Test
    fun `a currency stored as its display name falls back to EUR`() = runTest {
        // Arrange
        dataStore.edit { preferences ->
            preferences[CURRENCY_SYMBOL_KEY] = "US Dollar"
        }

        // Act
        val units = repository.units.first()

        // Assert
        assertThat(units.currency).isEqualTo(Currency.EUR)
    }
}
