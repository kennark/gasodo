package com.gasodoapp.gasodo.feature.overview

import java.math.BigDecimal
import java.time.LocalDate

data class TimelineDataPoint(
    val time: LocalDate,
    val value: BigDecimal
)
