package com.gasodoapp.gasodo.core.enums

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [FuelType].
 *
 * The user facing wording lives in string resources, which cannot be read from a plain JVM test,
 * so these tests guard the resource mapping rather than the English text.
 */
class FuelTypeTest {

    @Test
    fun `every fuel type maps to a string resource`() {
        FuelType.entries.forEach { type ->
            assertThat(type.labelRes).isNotEqualTo(0)
        }
    }

    @Test
    fun `label resources are unique`() {
        val labelRes = FuelType.entries.map { it.labelRes }

        assertThat(labelRes).containsNoDuplicates()
    }

    @Test
    fun `there are exactly 7 fuel types`() {
        assertThat(FuelType.entries.size).isEqualTo(7)
    }
}
