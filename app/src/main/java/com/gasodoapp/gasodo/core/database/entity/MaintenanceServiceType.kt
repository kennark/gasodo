package com.gasodoapp.gasodo.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Services to be used in Maintenance events.
 */
@Entity(tableName = "maintenance_service_types")
data class MaintenanceServiceType(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("service_id") val id: Long = 0,
    @ColumnInfo("service_name") val serviceName: String,
    @ColumnInfo("notes") val notes: String? = null
)
