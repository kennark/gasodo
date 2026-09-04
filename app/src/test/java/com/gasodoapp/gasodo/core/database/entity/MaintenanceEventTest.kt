package com.gasodoapp.gasodo.core.database.entity

import com.gasodoapp.gasodo.core.database.BaseColumns
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
            totalCost = BigDecimal("75.50")
        )

        assertThat(event.base).isEqualTo(base)
        assertThat(event.totalCost).isEqualTo(BigDecimal("75.50"))
    }
}