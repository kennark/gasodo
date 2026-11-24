package com.gasodoapp.gasodo.feature.overview

import java.math.BigDecimal
import java.time.LocalDate

data class TimelineDataPoint(
    val time: LocalDate,
    val value: BigDecimal
)

/**
 * One selectable series the timeline chart sheet can plot.
 *
 * The chart renderer knows nothing about what the numbers mean: everything it needs to present the
 * series — how to name it, what to caption the axis with, how many decimals to label it with — is
 * carried here by the caller. That is what lets the same canvas draw a consumption curve, a cost
 * curve and a price curve.
 *
 * @param label text of the selector chip, for example `Consumption`.
 * @param description wording of the line under the sheet title, naming the plotted trend.
 * @param unitLabel unit suffix the values are expressed in, for example `L/100 km`.
 * @param dataPoints values sorted by ascending [TimelineDataPoint.time].
 * @param valueDecimals decimals the axis labels are rendered with, so a price keeps its precision
 * while a cost stays readable.
 * @param emptyStateSubtitle explains what the user has to log to get this series plotted, shown
 * when there are too few points to draw a trend.
 */
data class TimelineChartSeries(
    val label: String,
    val description: String,
    val unitLabel: String,
    val dataPoints: List<TimelineDataPoint>,
    val valueDecimals: Int = 2,
    val emptyStateSubtitle: String,
)