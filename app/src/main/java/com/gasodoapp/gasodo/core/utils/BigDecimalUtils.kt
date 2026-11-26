package com.gasodoapp.gasodo.core.utils

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Locale

/**
 * Math precision and conversion utils for BigDecimal numbers
 */
object BigDecimalUtils {
    val CONTEXT = MathContext(20, RoundingMode.HALF_EVEN)
    const val SCALE = 5
    val ROUNDING_MODE = RoundingMode.HALF_EVEN

    internal val displayLocale: Locale
        get() = Locale.getDefault()
}

/**
 * Renders the amount with [decimalPoints] decimals and the separators of [locale].
 *
 * Grouping is applied, so `1234.5` in [Locale.US] becomes `1,234.50`. Pass `groupingUsed = false`
 * for values the user reads as an identifier rather than as a number, such as a mileage.
 */
@JvmName("toDisplayStringNonNull")
fun BigDecimal.toDisplayString(
    decimalPoints: Int,
    locale: Locale = BigDecimalUtils.displayLocale,
    groupingUsed: Boolean = true,
): String = NumberFormatting.format(this, decimalPoints, locale, groupingUsed)

@JvmName("toDisplayStringNullable")
fun BigDecimal?.toDisplayString(
    decimalPoints: Int,
    locale: Locale = BigDecimalUtils.displayLocale,
    groupingUsed: Boolean = true,
): String? = this?.toDisplayString(decimalPoints, locale, groupingUsed)

/**
 * Renders a whole number, such as a mileage or a count, with the separators of [locale].
 */
@JvmName("toDisplayStringLong")
fun Long.toDisplayString(
    locale: Locale = BigDecimalUtils.displayLocale,
    groupingUsed: Boolean = true,
): String = NumberFormatting.format(this, locale, groupingUsed)
