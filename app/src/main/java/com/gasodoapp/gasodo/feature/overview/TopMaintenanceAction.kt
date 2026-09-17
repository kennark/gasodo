package com.gasodoapp.gasodo.feature.overview

import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType

/**
 * A maintenance service type and the number of times it appears in maintenance events.
 */
data class TopMaintenanceAction(
    val serviceType: MaintenanceServiceType,
    val count: Int
)
