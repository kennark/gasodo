package com.example.carrefueltracker.core.enums

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PaymentMethodTest {

    @Test
    fun `all payment methods have correct display names`() {
        assertThat(PaymentMethod.CASH.displayName).isEqualTo("Cash")
        assertThat(PaymentMethod.CARD.displayName).isEqualTo("Card")
        assertThat(PaymentMethod.MOBILE_PAYMENT.displayName).isEqualTo("Mobile Payment")
        assertThat(PaymentMethod.OTHER.displayName).isEqualTo("Other")
    }

    @Test
    fun `toString returns displayName for each payment method`() {
        assertThat(PaymentMethod.CASH.toString()).isEqualTo("Cash")
        assertThat(PaymentMethod.CARD.toString()).isEqualTo("Card")
        assertThat(PaymentMethod.MOBILE_PAYMENT.toString()).isEqualTo("Mobile Payment")
        assertThat(PaymentMethod.OTHER.toString()).isEqualTo("Other")
    }

    @Test
    fun `there are exactly 4 payment methods`() {
        assertThat(PaymentMethod.entries.size).isEqualTo(4)
    }
}