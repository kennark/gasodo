package com.gasodoapp.gasodo.core.database.entity

import com.gasodoapp.gasodo.core.database.BaseColumns
import com.gasodoapp.gasodo.core.enums.PaymentMethod
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class MaintenanceEventTest {
    @Test
    fun `constructor with all parameters sets correct values`() {
        val base = BaseColumns(
            date = LocalDate.of(2023, 11, 14),
            mileage = 50000,
            savedLocationId = null,
            photoUris = emptyList(),
            notes = "Regular oil change"
        )

        val event = MaintenanceEvent(
            base = base,
            totalCost = BigDecimal("75.50"),
            paymentMethod = PaymentMethod.CASH
        )

        assertThat(event.base).isEqualTo(base)
        assertThat(event.totalCost).isEqualTo(BigDecimal("75.50"))
        assertThat(event.paymentMethod).isEqualTo(PaymentMethod.CASH)
    }

    @Test
    fun `constructor with nullable fields handles null values`() {
        val base = BaseColumns(
            date = LocalDate.of(2023, 11, 14),
            mileage = 50000,
            savedLocationId = null,
            photoUris = emptyList(),
            notes = ""
        )

        val event = MaintenanceEvent(
            base = base,
            totalCost = null,
            paymentMethod = null
        )

        assertThat(event.base).isEqualTo(base)
        assertThat(event.totalCost).isNull()
        assertThat(event.paymentMethod).isNull()
    }

    @Test
    fun `equality includes payment method`() {
        val id = UUID.randomUUID()
        val base = BaseColumns(
            date = LocalDate.of(2023, 11, 14),
            mileage = 50000,
            savedLocationId = null,
            photoUris = emptyList(),
            notes = "Regular oil change"
        )

        val event = MaintenanceEvent(
            id = id,
            base = base,
            totalCost = BigDecimal("75.50"),
            paymentMethod = PaymentMethod.CARD
        )

        val sameEvent = MaintenanceEvent(
            id = id,
            base = base,
            totalCost = BigDecimal("75.50"),
            paymentMethod = PaymentMethod.CARD
        )

        val otherPaymentMethod = MaintenanceEvent(
            id = id,
            base = base,
            totalCost = BigDecimal("75.50"),
            paymentMethod = PaymentMethod.CASH
        )

        assertThat(sameEvent).isEqualTo(event)
        assertThat(otherPaymentMethod).isNotEqualTo(event)
    }
}
