package com.gasodoapp.gasodo.core.enums

/**
 * Types of events that can be tracked in the car refuel tracker.
 */
enum class EventType(val displayName: String) {
    REFUEL("Refueling"),
    MAINTENANCE("Maintenance"),
    INSPECTION("Inspection");

    override fun toString(): String = displayName
}
