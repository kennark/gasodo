package com.gasodoapp.gasodo.feature.addevent

/**
 * Problems that can be attached to a single field of the add/edit event form.
 *
 * Empty fields are intentionally valid: every column in the database is nullable, so the user
 * may leave anything blank. These errors only cover values that cannot be stored as entered
 * (unparsable numbers, values that break the math) and the date/mileage consistency rule.
 *
 * The enum deliberately carries no user facing text: it keeps the ViewModel free of
 * `Context`/resource lookups (so validation stays a pure, unit testable function) while the
 * wording lives in the UI layer via [message].
 */
enum class FieldError {
    /** Mileage does not fit in a `Long`, e.g. a pasted 20 digit number. */
    MILEAGE_INVALID,

    /** Mileage is ordered inconsistently against the date of an existing event. */
    DATE_MILEAGE_CONFLICT,

    /** Amount is not a parsable decimal, e.g. `1,2,3`. */
    AMOUNT_INVALID,

    /** Amount must be greater than 0 because the price per liter is derived from it. */
    AMOUNT_NOT_POSITIVE,

    /** Total cost is not a parsable decimal. */
    COST_INVALID,

    /** Total cost is below 0. */
    COST_NEGATIVE,

    /** Price per liter is not a parsable decimal. */
    PRICE_PER_LITER_INVALID,

    /** Price per liter must be greater than 0. */
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
 */
fun FieldError.message(): String = when (this) {
    FieldError.MILEAGE_INVALID -> "Mileage is too large"
    FieldError.DATE_MILEAGE_CONFLICT -> "Mileage or date are not in order with existing events"

    FieldError.AMOUNT_INVALID -> "Amount is not a valid number"
    FieldError.AMOUNT_NOT_POSITIVE -> "Amount must be greater than 0"

    FieldError.COST_INVALID -> "Total cost is not a valid number"
    FieldError.COST_NEGATIVE -> "Total cost cannot be negative"

    FieldError.PRICE_PER_LITER_INVALID -> "Price per liter is not a valid number"
    FieldError.PRICE_PER_LITER_NOT_POSITIVE -> "Price per liter must be greater than 0"
}
