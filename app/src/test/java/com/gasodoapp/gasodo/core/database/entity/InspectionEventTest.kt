package com.gasodoapp.gasodo.core.database.entity

import com.gasodoapp.gasodo.core.database.BaseColumns
import com.gasodoapp.gasodo.core.enums.InspectionStatus
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InspectionEventTest {
    @Test
    fun `constructor with all parameters sets correct values`() {
        val base = BaseColumns(
            date = 1700000000,
            mileage = 50000,
            savedLocationId = null,
            photoUris = emptyList(),
            notes = "Annual inspection"
        )

        val event = InspectionEvent(
            base = base,
            status = InspectionStatus.PASS,
            findings = listOf("All systems operational")
        )

        assertThat(event.base).isEqualTo(base)
        assertThat(event.status).isEqualTo(InspectionStatus.PASS)
        assertThat(event.findings).isEqualTo(listOf("All systems operational"))
    }
}