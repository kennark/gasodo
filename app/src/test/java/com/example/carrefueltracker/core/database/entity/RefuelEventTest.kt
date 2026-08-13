package com.example.carrefueltracker.core.database.entity

import com.example.carrefueltracker.core.database.BaseColumns
import com.example.carrefueltracker.core.enums.PaymentMethod
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.math.BigDecimal
import java.util.UUID

class RefuelEventTest {

    @Test
    fun `constructor with all parameters sets correct values`() {
        val base = BaseColumns(
            date = 1700000000,
            mileage = 50000,
            savedLocationId = null,
            photoUris = listOf(),
            notes = "Regular fill"
        )

        val event = RefuelEvent(
            base = base,
            amountLiters = BigDecimal("45.5"),
            pricePerLiter = BigDecimal("6.50"),
            totalCost = BigDecimal("295.75"),
            paymentMethod = PaymentMethod.CARD,
            fullFillUp = true
        )

        assertThat(event.base).isEqualTo(base)
        assertThat(event.amountLiters).isEqualTo(BigDecimal("45.5"))
        assertThat(event.pricePerLiter).isEqualTo(BigDecimal("6.50"))
        assertThat(event.totalCost).isEqualTo(BigDecimal("295.75"))
        assertThat(event.paymentMethod).isEqualTo(PaymentMethod.CARD)
        assertThat(event.fullFillUp).isTrue()
    }

    @Test
    fun `constructor with nullable fields handles null values`() {
        val base = BaseColumns(
            date = 1700000000,
            mileage = 50000,
            savedLocationId = null,
            photoUris = listOf(),
            notes = ""
        )

        val event = RefuelEvent(
            base = base,
            amountLiters = null,
            pricePerLiter = null,
            totalCost = null,
            paymentMethod = null,
            fullFillUp = false
        )

        assertThat(event.base).isEqualTo(base)
        assertThat(event.amountLiters).isNull()
        assertThat(event.pricePerLiter).isNull()
        assertThat(event.totalCost).isNull()
        assertThat(event.paymentMethod).isNull()
        assertThat(event.fullFillUp).isFalse()
    }

    @Test
    fun `equality works for identical refuel events`() {
        val id = UUID.randomUUID()
        val base = BaseColumns(
            date = 1700000000,
            mileage = 50000,
            savedLocationId = null,
            photoUris = listOf(),
            notes = "Regular fill"
        )

        val event1 = RefuelEvent(
            id = id,
            base = base,
            amountLiters = BigDecimal("45.5"),
            pricePerLiter = BigDecimal("6.50"),
            totalCost = BigDecimal("295.75"),
            paymentMethod = PaymentMethod.CARD,
            fullFillUp = true
        )

        val event2 = RefuelEvent(
            id = id,
            base = base,
            amountLiters = BigDecimal("45.5"),
            pricePerLiter = BigDecimal("6.50"),
            totalCost = BigDecimal("295.75"),
            paymentMethod = PaymentMethod.CARD,
            fullFillUp = true
        )

        assertThat(event2).isEqualTo(event1)
    }
}