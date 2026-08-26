package com.gasodoapp.gasodo.core.utils

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Math precision and conversion utils for BigDecimal numbers
 */
object BigDecimalUtils {
    val CONTEXT = MathContext(20, RoundingMode.HALF_EVEN)
    const val SCALE = 5
    val ROUNDING_MODE = RoundingMode.HALF_EVEN
}

@JvmName("toDisplayStringNullable")
fun BigDecimal?.toDisplayString(decimalPoints: Int): String? =
    this?.toDisplayString(decimalPoints)

@JvmName("toDisplayStringNonNull")
fun BigDecimal.toDisplayString(decimalPoints: Int): String =
    this.setScale(decimalPoints, RoundingMode.HALF_EVEN).toPlainString()