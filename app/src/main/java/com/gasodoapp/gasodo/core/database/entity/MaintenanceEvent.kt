package com.gasodoapp.gasodo.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gasodoapp.gasodo.core.database.BaseColumns
import java.math.BigDecimal
import java.util.UUID

/**
 * Room entity for maintenance/service events.
 */
@Entity(
    tableName = "maintenance_events",
    foreignKeys = [
        ForeignKey(
            entity = SavedLocation::class,
            parentColumns = ["id"],
            childColumns = ["saved_location_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("saved_location_id")]
)
data class MaintenanceEvent(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    @Embedded val base: BaseColumns,

    @ColumnInfo("service_types") val serviceTypes: List<String>,
    @ColumnInfo("provider_name") val providerName: String?,
    @ColumnInfo("parts_used") val partsUsed: List<String>?,
    @ColumnInfo("total_cost") val totalCost: BigDecimal?
)
