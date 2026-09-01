package com.gasodoapp.gasodo.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.UUID

/**
 * Reference table for many-to-many relation between Maintenance events and Service types.
 */
@Entity(
    tableName = "used_maintenance_services",
    primaryKeys = ["event_id", "service_id"],
    foreignKeys = [
        ForeignKey(
            entity = MaintenanceEvent::class,
            parentColumns = ["event_id"],
            childColumns = ["event_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MaintenanceServiceType::class,
            parentColumns = ["service_id"],
            childColumns = ["service_id"],
            onDelete = ForeignKey.CASCADE
        ),

    ],
    indices = [Index("service_id")]

)
data class UsedMaintenanceService(
    @ColumnInfo("event_id") val maintenanceEventId: UUID,
    @ColumnInfo("service_id") val serviceId: Long
)
