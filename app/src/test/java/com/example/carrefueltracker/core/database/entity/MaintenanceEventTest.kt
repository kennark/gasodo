package com.example.carrefueltracker.core.database.entity

import com.example.carrefueltracker.core.database.BaseColumns
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.math.BigDecimal

class MaintenanceEventTest {
    @Test
    fun `constructor with all parameters sets correct values`() {
        val base = BaseColumns(
            date = 1700000000,
            mileage = 50000,
            savedLocationId = null,
            photoUris = emptyList(),
            notes = "Regular oil change"
        )

        val event = MaintenanceEvent(
            base = base,
            serviceTypes = listOf("Oil Change"),
            providerName = "Shell Service",
            partsUsed = listOf("Oil Filter"),
            totalCost = BigDecimal("75.50")
        )

        assertThat(event.base).isEqualTo(base)
        assertThat(event.serviceTypes).isEqualTo(listOf("Oil Change"))
        assertThat(event.providerName).isEqualTo("Shell Service")
        assertThat(event.partsUsed).isEqualTo(listOf("Oil Filter"))
        assertThat(event.totalCost).isEqualTo(BigDecimal("75.50"))
    }
}