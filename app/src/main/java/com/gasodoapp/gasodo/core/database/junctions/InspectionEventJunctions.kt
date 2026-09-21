package com.gasodoapp.gasodo.core.database.junctions

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.gasodoapp.gasodo.core.database.entity.InspectablePart
import com.gasodoapp.gasodo.core.database.entity.InspectionEvent
import com.gasodoapp.gasodo.core.database.entity.UsedInspectablePart

/**
 * Junctions to combine many-to-many relationships.
 */

data class InspectionEventWithParts(
    @Embedded val event: InspectionEvent,
    @Relation(
        parentColumn = "id",
        entityColumn = "service_id",
        associateBy = Junction(
            value = UsedInspectablePart::class,
            parentColumn = "inspection_event_id",
            entityColumn = "part_id"
        )
    )
    val parts: List<InspectablePart>
)

data class PartWithInspectionEvents(
    @Embedded val part: InspectablePart,
    @Relation(
        parentColumn = "service_id",
        entityColumn = "id",
        associateBy = Junction(
            value = UsedInspectablePart::class,
            parentColumn = "part_id",
            entityColumn = "inspection_event_id"
        )
    )
    val events: List<InspectionEvent>
)