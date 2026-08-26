package com.gasodoapp.gasodo.core.enums

/**
 * Types of fuel available for refueling events.
 */
enum class FuelType(val displayName: String) {
    PETROL("Petrol"),
    DIESEL("Diesel"),
    PREMIUM("Premium"),
    ELECTRIC("Electric"),
    HYBRID("Hybrid"),
    LPG("LPG"),
    OTHER("Other");

    override fun toString(): String = displayName
}
