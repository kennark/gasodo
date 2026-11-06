package com.gasodoapp.gasodo.feature.addevent

import com.gasodoapp.gasodo.core.enums.units.Liquid
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for the user facing wording of [FieldError].
 */
class AddEventFormErrorsTest {

    @Test
    fun `every error has a non blank message`() {
        FieldError.entries.forEach { error ->
            assertThat(error.message()).isNotEmpty()
        }
    }

    @Test
    fun `messages read the singular volume unit of the selected liquid`() {
        assertThat(FieldError.PRICE_PER_LITER_INVALID.message(Liquid.LITER))
            .isEqualTo("Price per Liter is not a valid number")
        assertThat(FieldError.PRICE_PER_LITER_INVALID.message(Liquid.GALLON))
            .isEqualTo("Price per Gallon is not a valid number")

        assertThat(FieldError.PRICE_PER_LITER_NOT_POSITIVE.message(Liquid.LITER))
            .isEqualTo("Price per Liter must be greater than 0")
        assertThat(FieldError.PRICE_PER_LITER_NOT_POSITIVE.message(Liquid.GALLON))
            .isEqualTo("Price per Gallon must be greater than 0")
    }

    /**
     * The volume unit only changes the price per volume wording, every other message is about a
     * value that has no unit attached.
     */
    @Test
    fun `other messages do not depend on the volume unit`() {
        val unaffected = FieldError.entries -
                setOf(FieldError.PRICE_PER_LITER_INVALID, FieldError.PRICE_PER_LITER_NOT_POSITIVE)

        unaffected.forEach { error ->
            assertThat(error.message(Liquid.GALLON)).isEqualTo(error.message(Liquid.LITER))
        }
    }

    @Test
    fun `the default message uses the default liquid`() {
        assertThat(FieldError.PRICE_PER_LITER_INVALID.message())
            .isEqualTo(FieldError.PRICE_PER_LITER_INVALID.message(Liquid.DEFAULT))
    }

    @Test
    fun `hasAnyError is false for a clean form`() {
        assertThat(AddEventFormErrors().hasAnyError).isFalse()
        assertThat(AddEventFormErrors().errorCount).isEqualTo(0)
    }

    @Test
    fun `errorCount counts every reported field`() {
        val errors = AddEventFormErrors(
            mileage = FieldError.MILEAGE_INVALID,
            date = FieldError.DATE_MILEAGE_CONFLICT,
            amount = FieldError.AMOUNT_INVALID,
            cost = FieldError.COST_NEGATIVE,
            pricePerLiter = FieldError.PRICE_PER_LITER_NOT_POSITIVE,
        )

        assertThat(errors.hasAnyError).isTrue()
        assertThat(errors.errorCount).isEqualTo(5)
    }
}
