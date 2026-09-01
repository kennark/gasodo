package com.gasodoapp.gasodo.core.database.junctions

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.gasodoapp.gasodo.core.database.entity.MaintenanceEvent
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.core.database.entity.UsedMaintenanceService

/**
 * Junctions to combine many-to-many relationships.
 */

data class MaintenanceEventWithServices(
    @Embedded val event: MaintenanceEvent,
    @Relation(
        parentColumn = "event_id",
        entityColumn = "service_id",
        associateBy = Junction(UsedMaintenanceService::class)
    )
    val services: List<MaintenanceServiceType>
)

data class ServiceTypeWithMaintenanceEvents(
    @Embedded val event: MaintenanceServiceType,
    @Relation(
        parentColumn = "service_id",
        entityColumn = "event_id",
        associateBy = Junction(UsedMaintenanceService::class)
    )
    val services: List<MaintenanceEvent>
)
