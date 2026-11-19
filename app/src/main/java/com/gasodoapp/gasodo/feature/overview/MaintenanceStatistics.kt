package com.gasodoapp.gasodo.feature.overview

import java.math.BigDecimal

/**
 * Everything the maintenance card shows for the selected period.
 *
 * A default instance describes a period without maintenance events, so the card can be rendered from
 * the state object alone instead of the screen holding the events.
 *
 * @param eventCount number of maintenance events in the period, used to decide whether the card is
 * shown.
 * @param totalCost sum of every maintenance event of the period.
 * @param actionCount number of services performed across those events, which is what the card
 * renders as "N actions".
 * @param topActions the most frequent services, at most three, sorted by descending count.
 */
data class MaintenanceStatistics(
    val eventCount: Int = 0,
    val totalCost: BigDecimal = BigDecimal.ZERO,
    val actionCount: Int = 0,
    val topActions: List<TopMaintenanceAction> = emptyList(),
)
