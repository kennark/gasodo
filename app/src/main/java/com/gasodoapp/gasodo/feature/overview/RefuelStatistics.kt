package com.gasodoapp.gasodo.feature.overview

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gasodoapp.gasodo.R
import com.gasodoapp.gasodo.core.settings.Units
import java.math.BigDecimal

/**
 * Everything the refuel card shows for the selected period.
 *
 * A default instance describes a period without refuels, so the card can be rendered from the state
 * object alone instead of the screen holding the events.
 *
 * @param eventCount number of refuels in the period, used to decide whether the card is shown.
 * @param totalCost sum of every refuel of the period.
 * @param totalLiters volume refuelled over the period.
 * @param averagePricePerLiter mean price paid per liquid unit, or `null` when no refuel carries a
 * price, so the card can render its "no data" state instead of a meaningless zero.
 * @param totalMileage distance between the highest and the lowest mileage of the period.
 * @param fuelConsumption consumption between the first and the last full fill-up, expressed in the
 * unit picked in the settings: either liquid per hundred distance units (`L/100 km`) or distance per
 * liquid unit (`mi/gal`). Both are computed from the same events, only the ratio is inverted, which
 * is why a single field can serve both.
 * @param fuelCost cost of the fuel burnt over a hundred distance units (for example `€/100 km`). It
 * deliberately ignores [com.gasodoapp.gasodo.core.settings.Units.fuelConsumption]: the label
 * rendered next to it
 * ([com.gasodoapp.gasodo.core.settings.Units.costPerDistanceLabel]) always reads as a cost per
 * hundred distance units.
 * @param fuelConsumptionChart consumption of every interval between two full fill-ups, one point per
 * interval, ordered by ascending date. Empty when no interval can be measured.
 */
data class RefuelStatistics(
    val eventCount: Int = 0,
    val totalCost: BigDecimal = BigDecimal.ZERO,
    val totalLiters: BigDecimal = BigDecimal.ZERO,
    val averagePricePerLiter: BigDecimal? = null,
    val totalMileage: Long? = null,
    val fuelConsumption: BigDecimal? = null,
    val fuelCost: BigDecimal? = null,
    val fuelConsumptionChart: List<TimelineDataPoint> = emptyList(),
    val fuelCostChart: List<TimelineDataPoint> = emptyList(),
    val fuelPriceChart: List<TimelineDataPoint> = emptyList()
)


/**
 * The refuel figures as selectable chart series, in the order the selector presents them:
 * consumption, then cost, then price.
 *
 * Kept next to the screen rather than inside the chart component so the renderer stays free of any
 * knowledge about fuel: it only sees labels and numbers. The wording is resolved here because the
 * labels are translated and the unit labels come from the user's settings.
 *
 * A series is returned even when it has too few points to be plotted, so the selector always offers
 * the full set of metrics; the sheet turns the empty ones into its explanation card.
 *
 * @param units user preferences the unit labels are read from.
 */
@Composable
fun RefuelStatistics.toChartSeries(units: Units): List<TimelineChartSeries> = listOf(
    TimelineChartSeries(
        label = stringResource(R.string.overview_chart_type_consumption),
        description = stringResource(R.string.overview_chart_consumption_description),
        unitLabel = units.consumptionLabel,
        dataPoints = fuelConsumptionChart,
        valueDecimals = 2,
        emptyStateSubtitle = stringResource(R.string.overview_chart_no_data_subtitle),
    ),
    TimelineChartSeries(
        label = stringResource(R.string.overview_chart_type_cost),
        description = stringResource(R.string.overview_chart_cost_description),
        // The cost is always a cost per hundred distance units, whatever ratio the consumption
        // is shown as, so the label ignores Units.fuelConsumption for the same reason the card
        // figure does.
        unitLabel = units.costPerDistanceLabel,
        dataPoints = fuelCostChart,
        valueDecimals = 2,
        emptyStateSubtitle = stringResource(R.string.overview_chart_no_data_subtitle),
    ),
    TimelineChartSeries(
        label = stringResource(R.string.overview_chart_type_price),
        description = stringResource(R.string.overview_chart_price_description),
        unitLabel = units.pricePerLiquidLabel,
        dataPoints = fuelPriceChart,
        // A price is stored with its third decimal, and the view model deliberately keeps it: only
        // the renderer decides how much of it to show, and rounding here would throw it away.
        valueDecimals = 3,
        // A price needs no full fill-up, only a logged price, so the advice differs from the other
        // two series.
        emptyStateSubtitle = stringResource(R.string.overview_chart_price_no_data_subtitle),
    ),
)