package com.gasodoapp.gasodo.core.enums

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [InspectionStatus].
 *
 * The user facing wording lives in string resources, which cannot be read from a plain JVM test,
 * so these tests guard the resource mapping rather than the English text.
 */
class InspectionStatusTest {

    @Test
    fun `every status maps to a string resource`() {
        InspectionStatus.entries.forEach { status ->
            assertThat(status.labelRes).isNotEqualTo(0)
        }
    }

    @Test
    fun `label resources are unique`() {
        val labelRes = InspectionStatus.entries.map { it.labelRes }

        assertThat(labelRes).containsNoDuplicates()
    }

    @Test
    fun `there are exactly 3 inspection statuses`() {
        assertThat(InspectionStatus.entries.size).isEqualTo(3)
    }
}
