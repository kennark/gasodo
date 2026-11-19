package com.gasodoapp.gasodo.feature.overview

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
)
