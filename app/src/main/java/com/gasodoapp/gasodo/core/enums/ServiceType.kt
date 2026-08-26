package com.gasodoapp.gasodo.core.enums

/**
 * Different service types to track (brake change, oil change, etc..)
 */
enum class ServiceType(val displayName: String) {
    BRAKE_DISKS("Brake Disks"),
    BRAKE_PADS("Brake Pads"),
    OIL_CHANGE("Oil Change");

    override fun toString(): String = displayName
}