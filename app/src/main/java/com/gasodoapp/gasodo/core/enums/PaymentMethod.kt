package com.gasodoapp.gasodo.core.enums

/**
 * Payment methods available for refueling events.
 */
enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    CARD("Card"),
    MOBILE_PAYMENT("Mobile Payment"),
    OTHER("Other");

    override fun toString(): String = displayName
}
