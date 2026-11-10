package com.gasodoapp.gasodo.core.enums

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `DEFAULT is SYSTEM`() {
        assertThat(ThemeMode.DEFAULT).isEqualTo(ThemeMode.SYSTEM)
    }

    @Test
    fun `every mode maps to a label and a description resource`() {
        ThemeMode.entries.forEach { mode ->
            assertThat(mode.labelRes).isNotEqualTo(0)
            assertThat(mode.descriptionRes).isNotEqualTo(0)
        }
    }

    @Test
    fun `label and description resources are unique`() {
        assertThat(ThemeMode.entries.map { it.labelRes }).containsNoDuplicates()
        assertThat(ThemeMode.entries.map { it.descriptionRes }).containsNoDuplicates()
    }

    @Test
    fun `stored enum names resolve to their entry`() {
        assertThat(ThemeMode.fromName("SYSTEM")).isEqualTo(ThemeMode.SYSTEM)
        assertThat(ThemeMode.fromName("LIGHT")).isEqualTo(ThemeMode.LIGHT)
        assertThat(ThemeMode.fromName("DARK")).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun `a missing stored value falls back to the default`() {
        assertThat(ThemeMode.fromName(null)).isEqualTo(ThemeMode.DEFAULT)
    }

    @Test
    fun `an unknown stored value falls back to the default`() {
        assertThat(ThemeMode.fromName("SEPIA")).isEqualTo(ThemeMode.DEFAULT)
    }
}
