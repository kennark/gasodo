package com.gasodoapp.gasodo.feature.overview

import com.gasodoapp.gasodo.core.utils.BigDecimalUtils
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Pure projection maths for the timeline chart.
 *
 * Kept out of the composables so the mapping from a data point to a fraction of the plot area can
 * be verified by plain unit tests: a wrong projection is invisible in a screenshot review but very
 * visible to a user reading the axis.
 *
 * Every function returns a fraction in `0f..1f` of the plot area, where `0f` is the left or bottom
 * edge and `1f` is the right or top edge. The mapping to pixels is the caller's concern.
 */

/**
 * Fraction of the horizontal plot area [date] occupies within `[domainStart, domainEnd]`.
 *
 * The axis is a real timeline rather than an ordinal scale, so refuels that are close together in
 * time are drawn close together even when several other points lie between them. A date outside the
 * domain is clamped to the nearest edge so a point can never be drawn outside the frame.
 *
 * [domainStart] must be before [domainEnd]; callers with a degenerate range should use
 * [xFractions] instead, which falls back to index spacing.
 */
internal fun xFraction(date: LocalDate, domainStart: LocalDate, domainEnd: LocalDate): Float {
    val spanDays = ChronoUnit.DAYS.between(domainStart, domainEnd)
    if (spanDays <= 0L) {
        return 0f
    }

    val offsetDays = ChronoUnit.DAYS.between(domainStart, date)
    return (offsetDays.toDouble() / spanDays.toDouble()).toFloat().coerceIn(0f, 1f)
}

/**
 * Fractions of the horizontal plot area for [times], in the order they are given.
 *
 * Distributes the points by date when [domainStart] and [domainEnd] describe a usable range. When
 * they do not — a single-day selection, or no selection at all — even index spacing is used
 * instead, so the points stay distinguishable rather than collapsing onto the left edge.
 */
internal fun xFractions(
    times: List<LocalDate>,
    domainStart: LocalDate?,
    domainEnd: LocalDate?,
): List<Float> {
    if (times.isEmpty()) {
        return emptyList()
    }

    if (domainStart != null && domainEnd != null && domainEnd.isAfter(domainStart)) {
        return times.map { time -> xFraction(time, domainStart, domainEnd) }
    }

    // Even spacing needs at least two points to have a step; a single point sits on the left edge.
    if (times.size == 1) {
        return listOf(0f)
    }

    val step = 1f / (times.size - 1)
    return times.indices.map { index -> index * step }
}

/**
 * Vertical extent the plotted values span, already inflated so the curve does not touch the frame.
 *
 * [min] and [max] are the bounds the renderer maps against, not the extremes of the data.
 */
internal data class ChartBounds(val min: BigDecimal, val max: BigDecimal) {

    /**
     * Same bounds widened by [ratio] on both ends.
     *
     * A flat series (every value identical) has no span to widen, so it is padded relative to its
     * own magnitude instead. Without that the scale would be zero and every point would land on the
     * same pixel row with a division by zero behind it.
     */
    fun padded(ratio: Double = 0.1): ChartBounds {
        // `BigDecimal(ratio)` would capture the binary expansion of the double, so a ratio of 0.1
        // would become 0.1000000000000000055511... and push the bounds off by an invisible but
        // unreproducible amount. Routing through the decimal string keeps the padding exact.
        val paddingRatio = BigDecimal(ratio.toString())
        val span = max - min

        val padding = if (span.signum() <= 0) {
            // Fall back to 1 so a series of zeroes still gets a readable band around it.
            max.abs().max(BigDecimal.ONE).multiply(paddingRatio)
        } else {
            span.multiply(paddingRatio)
        }

        return ChartBounds(min.subtract(padding), max.add(padding))
    }

    companion object {

        /**
         * Bounds covering [values] with room to spare, for the renderer to map against.
         *
         * Returns a unit range when there is nothing to plot, which keeps an empty chart from
         * producing NaN coordinates.
         */
        fun of(values: List<BigDecimal>, paddingRatio: Double = 0.1): ChartBounds {
            if (values.isEmpty()) {
                return ChartBounds(BigDecimal.ZERO, BigDecimal.ONE)
            }

            return ChartBounds(
                min = values.min(),
                max = values.max(),
            ).padded(paddingRatio)
        }
    }
}

/**
 * Fraction of the vertical plot area [value] occupies within [bounds].
 *
 * The result counts from the bottom edge, so a caller drawing to screen coordinates has to invert
 * it. Values outside [bounds] are clamped to the edges for the same reason dates are in
 * [xFraction].
 */
internal fun yFraction(value: BigDecimal, bounds: ChartBounds): Float {
    val span = bounds.max - bounds.min
    if (span.signum() <= 0) {
        // A degenerate scale has no meaningful position, so centre the value rather than divide by
        // zero. ChartBounds.of never produces this, but the renderer must not depend on that.
        return 0.5f
    }

    return (value - bounds.min)
        .divide(span, BigDecimalUtils.CONTEXT)
        .toFloat()
        .coerceIn(0f, 1f)
}
