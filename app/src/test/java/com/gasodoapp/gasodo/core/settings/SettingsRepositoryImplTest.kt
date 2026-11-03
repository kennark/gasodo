package com.gasodoapp.gasodo.core.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.gasodoapp.gasodo.core.enums.ThemeMode
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
}
