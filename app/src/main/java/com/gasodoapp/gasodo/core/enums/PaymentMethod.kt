package com.gasodoapp.gasodo.core.enums

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gasodoapp.gasodo.R

/**
 * Payment methods available for refueling events.
 *
 * [labelRes] is the user facing name, resolved through [label] so it follows the app language.
 */
enum class PaymentMethod(@param:StringRes val labelRes: Int) {
    CASH(R.string.payment_method_cash),
    CARD(R.string.payment_method_card),
    MOBILE_PAYMENT(R.string.payment_method_mobile_payment),
    OTHER(R.string.payment_method_other);

    /**
     * Localised name of this payment method, for rendering inside a composable.
     */
    @Composable
    fun label(): String = stringResource(labelRes)
}
