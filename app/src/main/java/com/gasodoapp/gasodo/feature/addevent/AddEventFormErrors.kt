package com.gasodoapp.gasodo.feature.addevent

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gasodoapp.gasodo.R
import com.gasodoapp.gasodo.core.enums.units.Liquid

/**
 * Problems that can be attached to a single field of the add/edit event form.
 *
 * Empty fields are intentionally valid: every column in the database is nullable, so the user
 * may leave anything blank. These errors only cover values that cannot be stored as entered
 * (unparsable numbers, values that break the math) and the date/mileage consistency rule.
 *
 * The enum deliberately carries no user facing text: it keeps the ViewModel free of
 * `Context`/resource lookups (so validation stays a pure, unit testable function) while the
 * wording lives in the UI layer via [message], which reads it from string resources.
 */
enum class FieldError {
    /** Mileage does not fit in a `Long`, e.g. a pasted 20 digit number. */
    MILEAGE_INVALID,

    /** Mileage is ordered inconsistently against the date of an existing event. */
    DATE_MILEAGE_CONFLICT,

    /** Amount is not a parsable decimal, e.g. `1,2,3`. */
    AMOUNT_INVALID,

    /** Amount must be greater than 0 because the price per volume unit is derived from it. */
    AMOUNT_NOT_POSITIVE,

    /** Total cost is not a parsable decimal. */
    COST_INVALID,

    /** Total cost is below 0. */
    COST_NEGATIVE,

    /** Price per volume unit is not a parsable decimal. */
    PRICE_PER_LITER_INVALID,

    /** Price per volume unit must be greater than 0. */
    PRICE_PER_LITER_NOT_POSITIVE,
}

/**
 * Per field validation state of the form. A non null value means the field currently holds an
 * invalid value and the UI should render the corresponding message underneath it.
 */
data class AddEventFormErrors(
    val mileage: FieldError? = null,
    val date: FieldError? = null,
    val amount: FieldError? = null,
    val cost: FieldError? = null,
    val pricePerLiter: FieldError? = null,
) {
    val hasAnyError: Boolean
        get() = mileage != null ||
                date != null ||
                amount != null ||
                cost != null ||
                pricePerLiter != null

    /**
     * Number of fields that currently report an error.
     */
    val errorCount: Int
        get() = listOf(mileage, date, amount, cost, pricePerLiter).count { it != null }
}

/**
 * User facing message for a [FieldError].
 *
 * [liquid] only affects the price per volume errors, which spell out the unit the user picked so
 * that the message matches the label of the field it is attached to (`Price per Gallon` for an
 * imperial setup rather than a hardcoded `liter`).
 */
@Composable
fun FieldError.message(liquid: Liquid = Liquid.DEFAULT): String = when (this) {
    FieldError.MILEAGE_INVALID -> stringResource(R.string.field_error_mileage_invalid)
    FieldError.DATE_MILEAGE_CONFLICT -> stringResource(R.string.field_error_date_mileage_conflict)

    FieldError.AMOUNT_INVALID -> stringResource(R.string.field_error_amount_invalid)
    FieldError.AMOUNT_NOT_POSITIVE -> stringResource(R.string.field_error_amount_not_positive)

    FieldError.COST_INVALID -> stringResource(R.string.field_error_cost_invalid)
    FieldError.COST_NEGATIVE -> stringResource(R.string.field_error_cost_negative)

    FieldError.PRICE_PER_LITER_INVALID ->
        stringResource(R.string.field_error_price_per_liquid_invalid, liquid.singularName())

    FieldError.PRICE_PER_LITER_NOT_POSITIVE ->
        stringResource(R.string.field_error_price_per_liquid_not_positive, liquid.singularName())
}
