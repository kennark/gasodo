package com.gasodoapp.gasodo.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.UUID

/**
 * Reference table for many-to-many relation between Inspection events and Inspectable parts.
 */
@Entity(
    tableName = "used_inspectable_parts",
    primaryKeys = ["inspection_event_id", "part_id"],
    foreignKeys = [
        ForeignKey(
            entity = InspectionEvent::class,
            parentColumns = ["id"],
            childColumns = ["inspection_event_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = InspectablePart::class,
            parentColumns = ["service_id"],
            childColumns = ["part_id"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [Index("part_id")]
)
data class UsedInspectablePart(
    @ColumnInfo("inspection_event_id") val inspectionEventId: UUID,
    @ColumnInfo("part_id") val partId: Long
)