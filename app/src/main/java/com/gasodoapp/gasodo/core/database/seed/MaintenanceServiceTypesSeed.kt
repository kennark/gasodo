package com.gasodoapp.gasodo.core.database.seed

import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType

/**
 * Default data to seed into Maintenance Service Types.
 */
object MaintenanceServiceTypesSeed {
    val list = listOf(
        "Brake Change",
        "Oil Change",
        "Air Filter",
        "Cabin Air Filter",
        "Tire Rotation",
        "Tire Change",
        "Window Wipers",
        "Engine Coolant",
        "Wash/Detailing",
        "Engine Belts"
    ).map { MaintenanceServiceType(serviceName = it) }
}