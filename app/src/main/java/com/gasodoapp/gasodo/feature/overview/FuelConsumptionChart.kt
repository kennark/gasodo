package com.gasodoapp.gasodo.feature.overview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gasodoapp.gasodo.R
import com.gasodoapp.gasodo.core.utils.toDisplayString
import com.gasodoapp.gasodo.ui.components.NoDataCard
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

/**
 * Bottom sheet presenting the fuel consumption trend for the selected period.
 *
 * The sheet always opens, so a user who taps the chart button is never left wondering whether the
 * tap registered: when the period holds fewer than two consumption values there is nothing to plot,
 * and an explanation of what to log replaces the curve.
 *
 * @param dataPoints consumption values sorted by ascending [TimelineDataPoint.time].
 * @param valueUnitLabel unit suffix the values are expressed in, for example `L/100 km`.
 * @param domainStart first date of the selected period, or `null` when no period is selected.
 * @param domainEnd last date of the selected period, or `null` when no period is selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelConsumptionBottomSheet(
    dataPoints: List<TimelineDataPoint>,
    valueUnitLabel: String,
    domainStart: LocalDate?,
    domainEnd: LocalDate?,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Text(
            text = stringResource(R.string.overview_fuel_consumption_chart_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        Text(
            text = valueUnitLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            if (dataPoints.size < 2) {
                NoDataCard(
                    title = stringResource(R.string.overview_chart_no_data_title),
                    subtitle = stringResource(R.string.overview_chart_no_data_subtitle),
                )
            } else {
                FuelConsumptionLineChart(
                    dataPoints = dataPoints,
                    valueUnitLabel = valueUnitLabel,
                    domainStart = domainStart,
                    domainEnd = domainEnd,
                )
            }
        }
    }
}

/**
 * Hand drawn consumption curve: straight segments through the fill-ups, area fill underneath, and a
 * dated x axis.
 *
 * Drawn on a [Canvas] instead of pulling in a charting library because the whole chart is a handful
 * of lines and dots, and an extra dependency would not earn its keep. Every colour and text style
 * comes from [MaterialTheme], so the chart follows the light and dark palettes.
 *
 * Renders nothing when fewer than two points are supplied, since a single point describes no trend;
 * callers that need to explain the absence to the user should show their own empty state instead.
 *
 * @param dataPoints consumption values sorted by ascending [TimelineDataPoint.time].
 * @param valueUnitLabel unit suffix the values are expressed in, captioned above the plot.
 * @param domainStart first date of the selected period, or `null` to use the earliest data point.
 * @param domainEnd last date of the selected period, or `null` to use the latest data point.
 */
@Composable
fun FuelConsumptionLineChart(
    dataPoints: List<TimelineDataPoint>,
    valueUnitLabel: String,
    domainStart: LocalDate?,
    domainEnd: LocalDate?,
    modifier: Modifier = Modifier,
) {
    if (dataPoints.size < 2) {
        return
    }

    val values = remember(dataPoints) { dataPoints.map(TimelineDataPoint::value) }
    val bounds = remember(values) { ChartBounds.of(values) }
    val fractions = remember(dataPoints, domainStart, domainEnd) {
        xFractions(dataPoints.map(TimelineDataPoint::time), domainStart, domainEnd)
    }

    // The requested period is what the user asked about, so it wins over the extent of the data;
    // without a period the data's own first and last dates are the only meaningful endpoints.
    val axisStart = domainStart ?: dataPoints.first().time
    val axisEnd = domainEnd ?: dataPoints.last().time
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT) }
    val axisDateLabels = remember(axisStart, axisEnd) {
        // A period that spans a single day has no interior, so the endpoints serve as the midpoint.
        val spanDays = ChronoUnit.DAYS.between(axisStart, axisEnd)
        val midDate = if (spanDays <= 0L) axisStart else axisStart.plusDays(spanDays / 2)
        listOf(axisStart, midDate, axisEnd).map { date -> date.format(dateFormatter) }
    }

    val chartColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val textMeasurer = rememberTextMeasurer()

    val chartDescription =
        stringResource(R.string.overview_chart_content_description, dataPoints.size)

    val leftGutter = 40.dp
    val bottomGutter = 20.dp
    val topInset = 18.dp
    val rightInset = 4.dp
    val labelGap = 4.dp
    val gridlineCount = 4
    val strokeWidth = 2.dp
    val pointRadius = 3.dp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            // A Canvas exposes no text to accessibility services, so the series is summarised here.
            .semantics { contentDescription = chartDescription },
    ) {
        val plotLeft = leftGutter.toPx()
        val plotRight = (size.width - rightInset.toPx()).coerceAtLeast(plotLeft)
        val plotTop = topInset.toPx()
        val plotBottom = (size.height - bottomGutter.toPx()).coerceAtLeast(plotTop)
        val plotWidth = plotRight - plotLeft
        val plotHeight = plotBottom - plotTop

        // Gridlines are evenly spaced in pixels and labelled with the value that pixel stands for,
        // which keeps the axis readable even when the bounds were padded to avoid a flat curve.
        val valueSpan = bounds.max.subtract(bounds.min)
        val gridSteps = gridlineCount - 1
        repeat(gridlineCount) { index ->
            val fraction = index.toFloat() / gridSteps
            val y = plotBottom - (plotHeight * fraction)

            drawLine(
                color = gridColor,
                start = Offset(plotLeft, y),
                end = Offset(plotRight, y),
                strokeWidth = 1.dp.toPx(),
            )

            val gridValue =
                bounds.min.add(valueSpan.multiply(BigDecimal.valueOf(fraction.toDouble())))
            val labelLayout = textMeasurer.measure(gridValue.toDisplayString(2), labelStyle)
            drawText(
                textLayoutResult = labelLayout,
                topLeft = Offset(
                    x = clampLabelX(
                        x = plotLeft - labelLayout.size.width - labelGap.toPx(),
                        width = labelLayout.size.width,
                        canvasWidth = size.width,
                    ),
                    y = (y - (labelLayout.size.height / 2f)).coerceIn(
                        minimumValue = 0f,
                        maximumValue = (size.height - labelLayout.size.height).coerceAtLeast(0f),
                    ),
                ),
            )
        }

        // Resolve the points once: the fill, the line and the dots all reuse the same coordinates.
        val points = fractions.mapIndexed { index, fraction ->
            Offset(
                x = plotLeft + (fraction * plotWidth),
                // yFraction counts from the bottom edge while screen y grows downwards.
                y = plotBottom - (yFraction(values[index], bounds) * plotHeight),
            )
        }

        val areaPath = Path().apply {
            moveTo(points.first().x, plotBottom)
            points.forEach { point -> lineTo(point.x, point.y) }
            lineTo(points.last().x, plotBottom)
            close()
        }
        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(chartColor.copy(alpha = 0.25f), Color.Transparent),
                startY = plotTop,
                endY = plotBottom,
            ),
        )

        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { point -> lineTo(point.x, point.y) }
        }
        drawPath(
            path = linePath,
            color = chartColor,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
        )

        points.forEach { point ->
            drawCircle(color = chartColor, radius = pointRadius.toPx(), center = point)
        }

        // The dates sit in the bottom gutter, below every plotted element so they stay legible even
        // where the curve reaches the axis.
        val dateLabelsHeight = textMeasurer.measure(axisDateLabels.first(), labelStyle).size.height
        val dateLabelTop = minOf(
            plotBottom + labelGap.toPx(),
            (size.height - dateLabelsHeight).coerceAtLeast(0f),
        )
        axisDateLabels.forEachIndexed { index, label ->
            val layout = textMeasurer.measure(label, labelStyle)
            val preferredX = when (index) {
                0 -> plotLeft
                1 -> (plotLeft + (plotWidth / 2f)) - (layout.size.width / 2f)
                else -> plotRight - layout.size.width
            }
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    x = clampLabelX(
                        x = preferredX,
                        width = layout.size.width,
                        canvasWidth = size.width,
                    ),
                    y = dateLabelTop,
                ),
            )
        }

        // The axis numbers are meaningless without the unit, so the chart captions itself for the
        // callers that show it outside the bottom sheet's subtitle.
        val unitLayout = textMeasurer.measure(valueUnitLabel, labelStyle)
        drawText(
            textLayoutResult = unitLayout,
            topLeft = Offset(
                x = clampLabelX(
                    x = plotRight - unitLayout.size.width,
                    width = unitLayout.size.width,
                    canvasWidth = size.width,
                ),
                y = ((topInset.toPx() - unitLayout.size.height) / 2f).coerceAtLeast(0f),
            ),
        )
    }
}

/**
 * Pushes a label back inside the canvas so it never gets clipped at either edge.
 *
 * [x] is the desired left edge of a label [width] pixels wide; the result is the closest position
 * that keeps the whole label visible.
 */
private fun clampLabelX(x: Float, width: Int, canvasWidth: Float): Float =
    x.coerceIn(0f, (canvasWidth - width).coerceAtLeast(0f))

@Preview(showBackground = true)
@Composable
fun FuelConsumptionLineChartPreview() {
    FuelConsumptionLineChart(
        dataPoints = listOf(
            TimelineDataPoint(LocalDate.of(2025, 1, 4), BigDecimal("7.42")),
            TimelineDataPoint(LocalDate.of(2025, 1, 19), BigDecimal("6.98")),
            TimelineDataPoint(LocalDate.of(2025, 2, 2), BigDecimal("7.15")),
            TimelineDataPoint(LocalDate.of(2025, 3, 15), BigDecimal("6.62")),
            TimelineDataPoint(LocalDate.of(2025, 3, 21), BigDecimal("6.80")),
            TimelineDataPoint(LocalDate.of(2025, 5, 2), BigDecimal("6.31")),
        ),
        valueUnitLabel = "L/100 km",
        domainStart = LocalDate.of(2025, 1, 1),
        domainEnd = LocalDate.of(2025, 5, 31),
    )
}

@Preview(showBackground = true)
@Composable
fun FuelConsumptionLineChartFlatPreview() {
    FuelConsumptionLineChart(
        dataPoints = listOf(
            TimelineDataPoint(LocalDate.of(2025, 2, 3), BigDecimal("6.50")),
            TimelineDataPoint(LocalDate.of(2025, 2, 21), BigDecimal("6.50")),
            TimelineDataPoint(LocalDate.of(2025, 3, 12), BigDecimal("6.50")),
            TimelineDataPoint(LocalDate.of(2025, 4, 8), BigDecimal("6.50")),
        ),
        valueUnitLabel = "L/100 km",
        domainStart = LocalDate.of(2025, 2, 1),
        domainEnd = LocalDate.of(2025, 4, 30),
    )
}

@Preview(showBackground = true)
@Composable
fun FuelConsumptionLineChartTwoPointsPreview() {
    FuelConsumptionLineChart(
        dataPoints = listOf(
            TimelineDataPoint(LocalDate.of(2025, 1, 12), BigDecimal("8.10")),
            TimelineDataPoint(LocalDate.of(2025, 3, 30), BigDecimal("7.05")),
        ),
        valueUnitLabel = "L/100 km",
        domainStart = LocalDate.of(2025, 1, 1),
        domainEnd = LocalDate.of(2025, 3, 31),
    )
}
