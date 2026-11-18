package com.gasodoapp.gasodo.feature.overview

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Unit tests for [ChartGeometry], the pure projection maths behind the overview timeline chart.
 *
 * A wrong projection is invisible in a screenshot review — the chart still "looks like a chart" —
 * but it is very visible to a user reading the axis. Every function here therefore promises that a
 * value maps to a fraction of `0f..1f` of the plot area (`0f` = left/bottom edge, `1f` =
 * right/top edge) without ever producing NaN or an out-of-frame coordinate.
 *
 * All `BigDecimal` expectations are built from strings, never from doubles, so the assertions do
 * not inherit binary floating point noise.
 */
class ChartGeometryTest {

    private companion object {

        /**
         * A 30 day domain: `ChronoUnit.DAYS.between(start, end) == 30`. The span is deliberately
         * not a power of two and the sample dates are deliberately irregular, so an index-based
         * (ordinal) axis cannot accidentally satisfy the expectations below.
         */
        val THIRTY_DAY_START: LocalDate = LocalDate.of(2025, 1, 1)
        val THIRTY_DAY_END: LocalDate = LocalDate.of(2025, 1, 31)

        /**
         * A 10 day domain. Its midpoint falls on an actual calendar day, so a date can be expected
         * at exactly half the plot area rather than at an awkward repeating fraction.
         */
        val TEN_DAY_START: LocalDate = LocalDate.of(2025, 3, 1)
        val TEN_DAY_END: LocalDate = LocalDate.of(2025, 3, 11)

        /**
         * Single precision cannot represent `1/30`, `10/30` or `20/30` exactly, so projections of
         * dates that do not fall on a nice fraction are compared with this tolerance. A third of a
         * pixel on a phone-width chart is far inside the tolerance, while the wrong spacing (index
         * based) is off by hundreds of pixels.
         */
        const val EPSILON: Float = 0.001f
    }

    /** Numeric BigDecimal comparison; `equals` on BigDecimal is scale sensitive, value is not. */
    private fun assertBigDecimalEquals(expected: String, actual: BigDecimal) {
        assertThat(actual).isEquivalentAccordingToCompareTo(BigDecimal(expected))
    }

    // region Horizontal projection: xFraction

    @Test
    fun `xFraction maps the domain start to the left edge`() {
        assertThat(xFraction(THIRTY_DAY_START, THIRTY_DAY_START, THIRTY_DAY_END)).isEqualTo(0f)
    }

    @Test
    fun `xFraction maps the domain end to the right edge`() {
        assertThat(xFraction(THIRTY_DAY_END, THIRTY_DAY_START, THIRTY_DAY_END)).isEqualTo(1f)
    }

    @Test
    fun `xFraction maps a midpoint date to the centre`() {
        // 2025-03-06 is 5 days into a 10 day span.
        val midpoint = LocalDate.of(2025, 3, 6)

        assertThat(xFraction(midpoint, TEN_DAY_START, TEN_DAY_END)).isWithin(EPSILON).of(0.5f)
    }

    /**
     * The axis is a real timeline, not an ordinal scale: the position of a point depends on its
     * *date*, never on how many points sit before it. Two refuels a day apart must be drawn close
     * together even when a month of empty road lies between them and the next entry.
     *
     * On a 30 day domain, day 10 must land at 10/30 and day 20 at 20/30 — not at 1/3 and 2/3 of
     * *some list index*. If this test fails, the axis has silently become index based.
     */
    @Test
    fun `xFraction spaces dates by their distance in days`() {
        val day10 = THIRTY_DAY_START.plusDays(10)
        val day20 = THIRTY_DAY_START.plusDays(20)

        assertThat(xFraction(day10, THIRTY_DAY_START, THIRTY_DAY_END))
            .isWithin(EPSILON)
            .of(10f / 30f)
        assertThat(xFraction(day20, THIRTY_DAY_START, THIRTY_DAY_END))
            .isWithin(EPSILON)
            .of(20f / 30f)
    }

    /**
     * A point can never be drawn outside the frame: data occasionally falls just outside the
     * selected range (a range picked at day granularity in one time zone, a point captured at
     * midnight), and it must be pinned to the nearest edge instead of overflowing the plot area.
     */
    @Test
    fun `xFraction clamps a date before the domain start to the left edge`() {
        val beforeDomain = THIRTY_DAY_START.minusDays(5)

        assertThat(xFraction(beforeDomain, THIRTY_DAY_START, THIRTY_DAY_END)).isEqualTo(0f)
    }

    @Test
    fun `xFraction clamps a date after the domain end to the right edge`() {
        val afterDomain = THIRTY_DAY_END.plusDays(5)

        assertThat(xFraction(afterDomain, THIRTY_DAY_START, THIRTY_DAY_END)).isEqualTo(1f)
    }

    /**
     * A zero-length domain would divide by zero. Returning `0f` keeps the point on the left edge
     * and, crucially, keeps the value finite: a NaN reaching the layout turns into an invisible or
     * wildly misplaced point rather than an obviously broken one.
     */
    @Test
    fun `xFraction with a zero length domain returns zero and is not NaN`() {
        val fraction = xFraction(THIRTY_DAY_START, THIRTY_DAY_START, THIRTY_DAY_START)

        assertThat(fraction).isEqualTo(0f)
        assertThat(fraction.isNaN()).isFalse()
    }

    // endregion

    // region Horizontal projection: xFractions and its fallbacks

    /**
     * With a usable domain the points keep their date spacing. The series below is deliberately
     * irregular ([start, start + 2 days, end]); evenly spaced output (0f, 0.5f, 1f) would mean the
     * domain was ignored.
     */
    @Test
    fun `xFractions distributes points by date rather than by index`() {
        val times = listOf(
            THIRTY_DAY_START,
            THIRTY_DAY_START.plusDays(2),
            THIRTY_DAY_END,
        )

        val fractions = xFractions(times, THIRTY_DAY_START, THIRTY_DAY_END)

        assertThat(fractions).hasSize(3)
        assertThat(fractions[0]).isEqualTo(0f)
        assertThat(fractions[1]).isWithin(EPSILON).of(2f / 30f)
        assertThat(fractions[2]).isEqualTo(1f)
        // The middle point is nowhere near the middle of the plot: the axis is not ordinal.
        assertThat(fractions[1]).isNotWithin(0.01f).of(0.5f)
    }

    /**
     * A single-day selection has no span to project onto. Collapsing every point onto the left
     * edge would stack them into one unreadable dot, so even index spacing is used instead to keep
     * the points distinguishable.
     */
    @Test
    fun `xFractions falls back to index spacing for a single day domain`() {
        val times = listOf(
            THIRTY_DAY_START,
            THIRTY_DAY_START,
            THIRTY_DAY_START,
        )

        val fractions = xFractions(times, THIRTY_DAY_START, THIRTY_DAY_START)

        assertThat(fractions).hasSize(3)
        assertThat(fractions.first()).isEqualTo(0f)
        assertThat(fractions.last()).isWithin(EPSILON).of(1f)
    }

    @Test
    fun `xFractions falls back to index spacing when the domain is null`() {
        val times = listOf(
            THIRTY_DAY_START,
            THIRTY_DAY_START.plusDays(3),
            THIRTY_DAY_END,
        )

        val fractions = xFractions(times, null, null)

        assertThat(fractions).hasSize(3)
        assertThat(fractions.first()).isEqualTo(0f)
        assertThat(fractions[1]).isWithin(EPSILON).of(0.5f)
        assertThat(fractions.last()).isWithin(EPSILON).of(1f)
    }

    @Test
    fun `xFractions falls back to index spacing when only one domain bound is null`() {
        val times = listOf(THIRTY_DAY_START, THIRTY_DAY_END)

        val fractions = xFractions(times, THIRTY_DAY_START, null)

        assertThat(fractions).containsExactly(0f, 1f).inOrder()
    }

    @Test
    fun `xFractions returns an empty list for empty input`() {
        assertThat(xFractions(emptyList(), THIRTY_DAY_START, THIRTY_DAY_END)).isEmpty()
    }

    /**
     * One point and no domain has no spacing to compute: `1f / (size - 1)` would divide by zero.
     * The single point sits on the left edge instead.
     */
    @Test
    fun `xFractions returns a single zero for one point without a usable domain`() {
        val fractions = xFractions(listOf(THIRTY_DAY_START), null, null)

        assertThat(fractions).containsExactly(0f)
    }

    /**
     * An inverted range (end before start) is not a usable domain. Treating it as one would push
     * every point to the left edge through clamping, so the index fallback keeps the series
     * readable while the user is still dragging the range picker.
     */
    @Test
    fun `xFractions falls back to index spacing for a descending domain`() {
        val times = listOf(
            THIRTY_DAY_START,
            THIRTY_DAY_START.plusDays(3),
            THIRTY_DAY_END,
        )

        val fractions = xFractions(times, THIRTY_DAY_END, THIRTY_DAY_START)

        assertThat(fractions).hasSize(3)
        assertThat(fractions.first()).isEqualTo(0f)
        assertThat(fractions[1]).isWithin(EPSILON).of(0.5f)
        assertThat(fractions.last()).isWithin(EPSILON).of(1f)
    }

    // endregion

    // region ChartBounds

    /**
     * The bounds are the scale the renderer maps against, not the extremes of the data. They must
     * be strictly wider than the data so the curve never touches the frame — a point drawn on the
     * very edge of the plot area looks clipped.
     */
    @Test
    fun `ChartBounds of a normal series is padded around the data`() {
        val values = listOf(
            BigDecimal("2.50"),
            BigDecimal("4.00"),
            BigDecimal("3.10"),
        )

        val bounds = ChartBounds.of(values)

        assertThat(bounds.min).isLessThan(BigDecimal("2.50"))
        assertThat(bounds.max).isGreaterThan(BigDecimal("4.00"))
        // The padding is proportional to the span (0.1 * 1.50 = 0.15), so the bounds sit one step
        // of that outside the data. They are compared with a narrow window rather than exactly:
        // `padded` builds its ratio via `BigDecimal(Double)`, which carries the binary expansion of
        // 0.1 (0.1000000000000000055511151231257827...), so the last digits are long and noisy.
        assertThat(bounds.min.toFloat()).isWithin(0.01f).of(2.35f)
        assertThat(bounds.max.toFloat()).isWithin(0.01f).of(4.15f)
    }

    /**
     * A flat series has zero span, so proportional padding would add nothing and leave a
     * degenerate scale. Without the magnitude fallback every point would land on the same pixel
     * row with a division by zero behind the projection.
     */
    @Test
    fun `ChartBounds of a flat series pads around the repeated value`() {
        val values = listOf(BigDecimal("5.00"), BigDecimal("5.00"), BigDecimal("5.00"))

        val bounds = ChartBounds.of(values)

        assertThat(bounds.min).isLessThan(BigDecimal("5.00"))
        assertThat(bounds.max).isGreaterThan(BigDecimal("5.00"))
        // Guard the division-by-zero case directly: the two bounds must not collapse into one.
        assertThat(bounds.min.compareTo(bounds.max)).isLessThan(0)
    }

    /** All-zero data is the worst case of a flat series; the fallback still has to open a band. */
    @Test
    fun `ChartBounds of an all zero series still brackets zero`() {
        val bounds = ChartBounds.of(
            listOf(BigDecimal.ZERO, BigDecimal("0.00"), BigDecimal("0")),
        )

        assertThat(bounds.min).isLessThan(BigDecimal.ZERO)
        assertThat(bounds.max).isGreaterThan(BigDecimal.ZERO)
    }

    /** An empty chart still needs a scale, otherwise every coordinate becomes NaN. */
    @Test
    fun `ChartBounds of an empty series is the unit range`() {
        val bounds = ChartBounds.of(emptyList())

        assertThat(bounds.min).isEqualTo(BigDecimal.ZERO)
        assertThat(bounds.max).isEqualTo(BigDecimal.ONE)
    }

    /** `padded(0.0)` must be an identity so callers can opt out of the padding entirely. */
    @Test
    fun `padded with a zero ratio returns the bounds unchanged`() {
        val bounds = ChartBounds(BigDecimal("2.50"), BigDecimal("10.00"))

        val padded = bounds.padded(0.0)

        assertBigDecimalEquals("2.50", padded.min)
        assertBigDecimalEquals("10.00", padded.max)
    }

    // endregion

    // region Vertical projection: yFraction

    @Test
    fun `yFraction maps the lower bound to the bottom edge`() {
        val bounds = ChartBounds(BigDecimal("2.00"), BigDecimal("8.00"))

        assertThat(yFraction(bounds.min, bounds)).isEqualTo(0f)
    }

    @Test
    fun `yFraction maps the upper bound to the top edge`() {
        val bounds = ChartBounds(BigDecimal("2.00"), BigDecimal("8.00"))

        // `(max - min) / (max - min)` is exactly one, so this edge is exact rather than approximate.
        assertThat(yFraction(bounds.max, bounds)).isEqualTo(1f)
    }

    /**
     * Values can fall outside the padded bounds (a rounding difference between the axis and the
     * point, a value added after the bounds were computed). They must be pinned to the nearest edge
     * and stay finite, so the point is clipped rather than drawn off-canvas or not at all.
     */
    @Test
    fun `yFraction clamps values outside the bounds`() {
        val bounds = ChartBounds(BigDecimal("2.00"), BigDecimal("8.00"))

        val tooHigh = yFraction(BigDecimal("99.00"), bounds)
        val tooLow = yFraction(BigDecimal("-99.00"), bounds)

        assertThat(tooHigh).isEqualTo(1f)
        assertThat(tooLow).isEqualTo(0f)
        assertThat(tooHigh.isNaN()).isFalse()
        assertThat(tooLow.isNaN()).isFalse()
    }

    /**
     * A degenerate scale (min == max) has no meaningful position and dividing by its zero span
     * would yield NaN or infinity, both of which poison the layout. Centring the value keeps the
     * chart drawable. [ChartBounds.of] never produces such bounds, but the renderer must not have
     * to depend on that.
     */
    @Test
    fun `yFraction centres the value when the bounds are degenerate`() {
        val bounds = ChartBounds(BigDecimal("5.00"), BigDecimal("5.00"))

        val fraction = yFraction(BigDecimal("5.00"), bounds)

        assertThat(fraction).isEqualTo(0.5f)
        assertThat(fraction.isNaN()).isFalse()
        assertThat(fraction.isInfinite()).isFalse()
    }

    // endregion
}
