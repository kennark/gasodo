package com.gasodoapp.gasodo.core.utils

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Builds the locale aware formatters behind the `toDisplayString` helpers.
 *
 * Kept internal on purpose: every screen renders numbers through those extensions, so all values
 * in the app share the same decimals policy, the same rounding and the same locale.
 *
 * The locale is resolved by the caller on every call instead of being captured once, while the
 * formatters themselves are memoised per locale. Switching the device language therefore renders
 * the new separators straight away, with no app restart.
 */
internal object NumberFormatting {

    /**
     * Formatters are reused because building one is comparatively expensive.
     * [DecimalFormat] is not thread safe, so the cache lives per thread and is keyed
     * by locale, decimals and grouping.
     */
    private val formatterCache = object : ThreadLocal<MutableMap<String, DecimalFormat>>() {
        override fun initialValue(): MutableMap<String, DecimalFormat> = HashMap()
    }

    /**
     * Renders an amount with exactly [decimals] decimals, using the separators of [locale].
     */
    fun format(value: BigDecimal, decimals: Int, locale: Locale, groupingUsed: Boolean): String {
        // The value is rounded here, with the app's own policy and exactly the requested scale, so
        // BigDecimalUtils.ROUNDING_MODE stays the single rounding rule and the formatter only has
        // to insert separators.
        val rounded = value.setScale(decimals, BigDecimalUtils.ROUNDING_MODE)
        // A small negative amount can round to zero while keeping its sign, which would render as
        // "-0.00". Zero has no sign, so it is rebuilt without one.
        val normalized = if (rounded.signum() == 0) BigDecimal.ZERO.setScale(decimals) else rounded
        return formatter(locale, decimals, groupingUsed).format(normalized)
    }

    /**
     * Renders a whole number, such as a mileage, with the separators of [locale].
     */
    fun format(value: Long, locale: Locale, groupingUsed: Boolean): String =
        formatter(locale, 0, groupingUsed).format(value)

    private fun formatter(locale: Locale, decimals: Int, groupingUsed: Boolean): DecimalFormat {
        val cache =
            formatterCache.get() ?: HashMap<String, DecimalFormat>().also(formatterCache::set)
        val key = "${locale.toLanguageTag()}|$decimals|$groupingUsed"
        return cache.getOrPut(key) {
            DecimalFormat(
                pattern(decimals, groupingUsed),
                DecimalFormatSymbols.getInstance(locale),
            ).apply {
                // Keeps the digits of the BigDecimal intact: without this the formatter routes the
                // value through a double and loses precision on the long ones.
                isParseBigDecimal = true
            }
        }
    }

    /**
     * Patterns always spell the placeholders `.` and `,`, whatever the locale;
     * [DecimalFormatSymbols] swaps in the characters that locale actually writes.
     */
    private fun pattern(decimals: Int, groupingUsed: Boolean): String {
        val integerPart = if (groupingUsed) "#,##0" else "0"
        if (decimals <= 0) return integerPart
        return integerPart + "." + "0".repeat(decimals)
    }
}
