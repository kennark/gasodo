package com.gasodoapp.gasodo.feature.overview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
 * Bottom sheet presenting the trend of the series selected among [series].
 *
 * The sheet always opens, so a user who taps the chart button is never left wondering whether the
 * tap registered: when the selected series holds fewer than two values there is nothing to plot,
 * and an explanation of what to log replaces the curve. Only the selected series is drawn, never
 * several at once.
 *
 * @param title heading of the sheet.
 * @param series selectable series, the first one being the default selection.
 * @param domainStart first date of the selected period, or `null` when no period is selected.
 * @param domainEnd last date of the selected period, or `null` when no period is selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineChartBottomSheet(
    title: String,
    series: List<TimelineChartSeries>,
    domainStart: LocalDate?,
    domainEnd: LocalDate?,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    // Surviving the configuration change keeps the metric the user picked selected on rotation.
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        TimelineChartSheetBody(
            title = title,
            series = series,
            selectedIndex = selectedIndex,
            onSelectIndex = { index -> selectedIndex = index },
            domainStart = domainStart,
            domainEnd = domainEnd,
        )
    }
}

/**
 * Content of [TimelineChartBottomSheet], split out so it can be previewed and composed without a
 * sheet hosting it.
 *
 * @param series selectable series. An index outside [series] falls back to the first one, so a
 * caller can never select a series that does not exist.
 */
@Composable
fun TimelineChartSheetBody(
    title: String,
    series: List<TimelineChartSeries>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    domainStart: LocalDate?,
    domainEnd: LocalDate?,
    modifier: Modifier = Modifier,
) {
    if (series.isEmpty()) {
        return
    }

    val selected = series[selectedIndex.coerceIn(series.indices)]

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        // The extra line names what is currently plotted together with its unit, so the curve is
        // never left unexplained now that the sheet hosts several metrics.
        Text(
            text = stringResource(
                R.string.overview_chart_selection_description,
                selected.description,
                selected.unitLabel,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        // Wraps rather than ellipsizing: a long translated label pushes the row to a second line
        // instead of hiding a metric behind a truncation.
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            series.forEachIndexed { index, item ->
                FilterChip(
                    selected = item === selected,
                    onClick = { onSelectIndex(index) },
                    label = { Text(item.label) },
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            if (selected.dataPoints.size < 2) {
                NoDataCard(
                    title = stringResource(R.string.overview_chart_no_data_title),
                    subtitle = selected.emptyStateSubtitle,
                )
            } else {
                TimelineChart(
                    dataPoints = selected.dataPoints,
                    valueUnitLabel = selected.unitLabel,
                    valueDecimals = selected.valueDecimals,
                    contentDescription = stringResource(
                        R.string.overview_chart_content_description,
                        selected.description,
                        selected.dataPoints.size,
                    ),
                    domainStart = domainStart,
                    domainEnd = domainEnd,
                )
            }
        }
    }
}

/**
 * Hand drawn timeline curve: straight segments through the points, area fill underneath, and a
 * dated x axis.
 *
 * Drawn on a [Canvas] instead of pulling in a charting library because the whole chart is a handful
 * of lines and dots, and an extra dependency would not earn its keep. Every colour and text style
 * comes from [MaterialTheme], so the chart follows the light and dark palettes.
 *
 * Renders nothing when fewer than two points are supplied, since a single point describes no trend;
 * callers that need to explain the absence to the user should show their own empty state instead.
 *
 * @param dataPoints values sorted by ascending [TimelineDataPoint.time].
 * @param valueUnitLabel unit suffix the values are expressed in, captioned above the plot.
 * @param contentDescription accessibility summary of the series, since a canvas exposes no text.
 * @param domainStart first date of the selected period, or `null` to use the earliest data point.
 * @param domainEnd last date of the selected period, or `null` to use the latest data point.
 * @param valueDecimals decimals the axis labels are rendered with.
 */
@Composable
fun TimelineChart(
    dataPoints: List<TimelineDataPoint>,
    valueUnitLabel: String,
    contentDescription: String,
    domainStart: LocalDate?,
    domainEnd: LocalDate?,
    modifier: Modifier = Modifier,
    valueDecimals: Int = 2,
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
            .semantics { this.contentDescription = contentDescription },
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
            val labelLayout =
                textMeasurer.measure(gridValue.toDisplayString(valueDecimals), labelStyle)
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
fun TimelineChartPreview() {
    TimelineChart(
        dataPoints = listOf(
            TimelineDataPoint(LocalDate.of(2025, 1, 4), BigDecimal("7.42")),
            TimelineDataPoint(LocalDate.of(2025, 1, 19), BigDecimal("6.98")),
            TimelineDataPoint(LocalDate.of(2025, 2, 2), BigDecimal("7.15")),
            TimelineDataPoint(LocalDate.of(2025, 3, 15), BigDecimal("6.62")),
            TimelineDataPoint(LocalDate.of(2025, 3, 21), BigDecimal("6.80")),
            TimelineDataPoint(LocalDate.of(2025, 5, 2), BigDecimal("6.31")),
        ),
        valueUnitLabel = "L/100 km",
        contentDescription = "Fuel consumption trend chart with 6 data points",
        domainStart = LocalDate.of(2025, 1, 1),
        domainEnd = LocalDate.of(2025, 5, 31),
    )
}

@Preview(showBackground = true)
@Composable
fun TimelineChartFlatPreview() {
    TimelineChart(
        dataPoints = listOf(
            TimelineDataPoint(LocalDate.of(2025, 2, 3), BigDecimal("6.50")),
            TimelineDataPoint(LocalDate.of(2025, 2, 21), BigDecimal("6.50")),
            TimelineDataPoint(LocalDate.of(2025, 3, 12), BigDecimal("6.50")),
            TimelineDataPoint(LocalDate.of(2025, 4, 8), BigDecimal("6.50")),
        ),
        valueUnitLabel = "L/100 km",
        contentDescription = "Fuel consumption trend chart with 4 data points",
        domainStart = LocalDate.of(2025, 2, 1),
        domainEnd = LocalDate.of(2025, 4, 30),
    )
}

@Preview(showBackground = true)
@Composable
fun TimelineChartTwoPointsPreview() {
    TimelineChart(
        dataPoints = listOf(
            TimelineDataPoint(LocalDate.of(2025, 1, 12), BigDecimal("8.10")),
            TimelineDataPoint(LocalDate.of(2025, 3, 30), BigDecimal("7.05")),
        ),
        valueUnitLabel = "L/100 km",
        contentDescription = "Fuel consumption trend chart with 2 data points",
        domainStart = LocalDate.of(2025, 1, 1),
        domainEnd = LocalDate.of(2025, 3, 31),
    )
}

@Preview(showBackground = true)
@Composable
fun TimelineChartPriceDecimalsPreview() {
    TimelineChart(
        dataPoints = listOf(
            TimelineDataPoint(LocalDate.of(2025, 1, 12), BigDecimal("1.239")),
            TimelineDataPoint(LocalDate.of(2025, 2, 8), BigDecimal("1.512")),
            TimelineDataPoint(LocalDate.of(2025, 3, 30), BigDecimal("1.401")),
        ),
        valueUnitLabel = "€/L",
        contentDescription = "Fuel price trend chart with 3 data points",
        domainStart = LocalDate.of(2025, 1, 1),
        domainEnd = LocalDate.of(2025, 3, 31),
        valueDecimals = 3,
    )
}

private val previewSeries = listOf(
    TimelineChartSeries(
        label = "Consumption",
        description = "Fuel consumption",
        unitLabel = "L/100 km",
        dataPoints = listOf(
            TimelineDataPoint(LocalDate.of(2025, 1, 4), BigDecimal("7.42")),
            TimelineDataPoint(LocalDate.of(2025, 2, 2), BigDecimal("7.15")),
            TimelineDataPoint(LocalDate.of(2025, 3, 15), BigDecimal("6.62")),
            TimelineDataPoint(LocalDate.of(2025, 5, 2), BigDecimal("6.31")),
        ),
        valueDecimals = 2,
        emptyStateSubtitle = "Log at least three full fill-ups in this period",
    ),
    TimelineChartSeries(
        label = "Cost",
        description = "Fuel cost",
        unitLabel = "€/100 km",
        dataPoints = listOf(
            TimelineDataPoint(LocalDate.of(2025, 1, 4), BigDecimal("8.90")),
            TimelineDataPoint(LocalDate.of(2025, 2, 2), BigDecimal("9.40")),
            TimelineDataPoint(LocalDate.of(2025, 3, 15), BigDecimal("8.10")),
            TimelineDataPoint(LocalDate.of(2025, 5, 2), BigDecimal("7.65")),
        ),
        valueDecimals = 2,
        emptyStateSubtitle = "Log at least three full fill-ups in this period",
    ),
    TimelineChartSeries(
        label = "Price",
        description = "Fuel price",
        unitLabel = "€/L",
        dataPoints = listOf(
            TimelineDataPoint(LocalDate.of(2025, 1, 4), BigDecimal("1.239")),
            TimelineDataPoint(LocalDate.of(2025, 2, 2), BigDecimal("1.512")),
            TimelineDataPoint(LocalDate.of(2025, 3, 15), BigDecimal("1.401")),
            TimelineDataPoint(LocalDate.of(2025, 5, 2), BigDecimal("1.355")),
        ),
        valueDecimals = 3,
        emptyStateSubtitle = "Log at least two refuels with a price in this period",
    ),
)

@Preview(showBackground = true)
@Composable
fun TimelineChartSheetBodyPreview() {
    TimelineChartSheetBody(
        title = "Fuel Trends",
        series = previewSeries,
        selectedIndex = 0,
        onSelectIndex = {},
        domainStart = LocalDate.of(2025, 1, 1),
        domainEnd = LocalDate.of(2025, 5, 31),
    )
}

@Preview(showBackground = true)
@Composable
fun TimelineChartSheetBodyPricePreview() {
    TimelineChartSheetBody(
        title = "Fuel Trends",
        series = previewSeries,
        selectedIndex = 2,
        onSelectIndex = {},
        domainStart = LocalDate.of(2025, 1, 1),
        domainEnd = LocalDate.of(2025, 5, 31),
    )
}

@Preview(showBackground = true)
@Composable
fun TimelineChartSheetBodyEmptyPreview() {
    TimelineChartSheetBody(
        title = "Fuel Trends",
        series = previewSeries.map { series ->
            series.copy(dataPoints = emptyList())
        },
        selectedIndex = 1,
        onSelectIndex = {},
        domainStart = LocalDate.of(2025, 1, 1),
        domainEnd = LocalDate.of(2025, 5, 31),
    )
}
