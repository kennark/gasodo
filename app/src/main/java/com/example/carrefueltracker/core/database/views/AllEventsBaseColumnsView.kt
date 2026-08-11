package com.example.carrefueltracker.core.database.views

import androidx.room.DatabaseView
import com.example.carrefueltracker.core.enums.EventType
import java.util.UUID

/**
 * View consisting of [com.example.carrefueltracker.core.database.BaseColumns] from 3 tables.
 */
@DatabaseView(
    """
        SELECT id, 'REFUEL' as eventType, date, mileage, saved_location_id, photo_uris, notes
        FROM refuel_events
        UNION ALL
        SELECT id, 'MAINTENANCE' as eventType, date, mileage, saved_location_id, photo_uris, notes
        FROM maintenance_events
        UNION ALL
        SELECT id, 'INSPECTION' as eventType, date, mileage, saved_location_id, photo_uris, notes
        FROM inspection_events
    """
)
data class AllEventsBaseColumnsView(
    val id: UUID,
    val eventType: EventType,
    val date: Long?,
    val mileage: Long?,
    val savedLocationId: UUID?,
    val photoUris: List<String>,
    val notes: String,
)
