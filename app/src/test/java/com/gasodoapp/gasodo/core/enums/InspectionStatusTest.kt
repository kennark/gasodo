package com.gasodoapp.gasodo.core.enums

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InspectionStatusTest {

    @Test
    fun `all inspection statuses have correct display names`() {
        assertThat(InspectionStatus.PASS.displayName).isEqualTo("Pass")
        assertThat(InspectionStatus.FAIL.displayName).isEqualTo("Fail")
        assertThat(InspectionStatus.CONDITIONAL_PASS.displayName).isEqualTo("Conditional Pass")
    }

    @Test
    fun `toString returns displayName for each inspection status`() {
        assertThat(InspectionStatus.PASS.toString()).isEqualTo("Pass")
        assertThat(InspectionStatus.FAIL.toString()).isEqualTo("Fail")
        assertThat(InspectionStatus.CONDITIONAL_PASS.toString()).isEqualTo("Conditional Pass")
    }

    @Test
    fun `there are exactly 3 inspection statuses`() {
        assertThat(InspectionStatus.entries.size).isEqualTo(3)
    }
}