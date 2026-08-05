package com.example.carrefueltracker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.carrefueltracker.core.database.BaseColumns
import com.example.carrefueltracker.core.enums.InspectionStatus
import java.util.UUID

/**
 * Room entity for inspection events.
 */
@Entity(
    tableName = "inspection_events",
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
data class InspectionEvent(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    @Embedded val base: BaseColumns,

    @ColumnInfo("status") val status: InspectionStatus?,
    @ColumnInfo("findings") val findings: List<String> = emptyList()
)
