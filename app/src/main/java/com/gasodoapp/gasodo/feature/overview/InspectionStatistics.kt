package com.gasodoapp.gasodo.feature.overview

import java.math.BigDecimal

/**
 * Everything the inspection card shows for the selected period: how the inspections were distributed
 * over the three outcomes, both as counts and as a share of the total, so the donut chart and its
 * legend can be built from the same numbers.
 *
 * A default instance describes a period without inspections, so the card can be rendered from the
 * state object alone instead of the screen holding the events.
 *
 * @param totalCount number of inspections in the period, used to decide whether the card is shown.
 * @param passCount inspections that ended with a pass.
 * @param failCount inspections that ended with a failure.
 * @param conditionalPassCount inspections that passed subject to a follow-up.
 * @param passPercentage share of [totalCount] that passed, between zero and a hundred.
 * @param failPercentage share of [totalCount] that failed, between zero and a hundred.
 * @param conditionalPassPercentage share of [totalCount] that passed conditionally, between zero and
 * a hundred.
 */
data class InspectionStatistics(
    val totalCount: Int = 0,
    val passCount: Int = 0,
    val failCount: Int = 0,
    val conditionalPassCount: Int = 0,
    val passPercentage: BigDecimal = BigDecimal.ZERO,
    val failPercentage: BigDecimal = BigDecimal.ZERO,
    val conditionalPassPercentage: BigDecimal = BigDecimal.ZERO,
)
