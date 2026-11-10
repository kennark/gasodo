package com.gasodoapp.gasodo.core.enums

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [PaymentMethod].
 *
 * The user facing wording lives in string resources, which cannot be read from a plain JVM test,
 * so these tests guard the resource mapping rather than the English text.
 */
class PaymentMethodTest {

    @Test
    fun `every payment method maps to a string resource`() {
        PaymentMethod.entries.forEach { method ->
            assertThat(method.labelRes).isNotEqualTo(0)
        }
    }

    @Test
    fun `label resources are unique`() {
        val labelRes = PaymentMethod.entries.map { it.labelRes }

        assertThat(labelRes).containsNoDuplicates()
    }

    @Test
    fun `there are exactly 4 payment methods`() {
        assertThat(PaymentMethod.entries.size).isEqualTo(4)
    }
}
