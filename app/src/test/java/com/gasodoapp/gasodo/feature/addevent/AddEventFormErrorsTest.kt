package com.gasodoapp.gasodo.feature.addevent

import androidx.annotation.StringRes
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [FieldError] and [AddEventFormErrors].
 *
 * The user facing message of a [FieldError] is a string resource resolved by the composable
 * `FieldError.message()`, which cannot be read from a plain JVM test. These tests therefore assert
 * the resource mapping and the pure state helpers instead of the English wording.
 */
class AddEventFormErrorsTest {

    /**
     * Reads the resource id a [FieldError] maps to.
     *
     * Mirrors the `when` in `FieldError.message()`; keeping it here means a new entry added to the
     * enum without a resource makes this test fail to compile.
     */
    @StringRes
    private fun FieldError.resourceId(): Int = when (this) {
        FieldError.MILEAGE_INVALID -> com.gasodoapp.gasodo.R.string.field_error_mileage_invalid
        FieldError.DATE_MILEAGE_CONFLICT -> com.gasodoapp.gasodo.R.string.field_error_date_mileage_conflict
        FieldError.AMOUNT_INVALID -> com.gasodoapp.gasodo.R.string.field_error_amount_invalid
        FieldError.AMOUNT_NOT_POSITIVE -> com.gasodoapp.gasodo.R.string.field_error_amount_not_positive
        FieldError.COST_INVALID -> com.gasodoapp.gasodo.R.string.field_error_cost_invalid
        FieldError.COST_NEGATIVE -> com.gasodoapp.gasodo.R.string.field_error_cost_negative
        FieldError.PRICE_PER_LITER_INVALID ->
            com.gasodoapp.gasodo.R.string.field_error_price_per_liquid_invalid

        FieldError.PRICE_PER_LITER_NOT_POSITIVE ->
            com.gasodoapp.gasodo.R.string.field_error_price_per_liquid_not_positive
    }

    @Test
    fun `every error maps to a string resource`() {
        FieldError.entries.forEach { error ->
            assertThat(error.resourceId()).isNotEqualTo(0)
        }
    }

    @Test
    fun `each error maps to its own string resource`() {
        val resourceIds = FieldError.entries.map { it.resourceId() }

        assertThat(resourceIds).containsNoDuplicates()
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
