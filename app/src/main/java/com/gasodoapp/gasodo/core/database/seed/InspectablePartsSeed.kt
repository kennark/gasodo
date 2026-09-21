package com.gasodoapp.gasodo.core.database.seed

import com.gasodoapp.gasodo.core.database.entity.InspectablePart

/**
 * Default data to seed into Inspectable Parts.
 */
object InspectablePartsSeed {
    val list = listOf(
        "Brakes",
        "Engine Oil",
        "Tires",
        "Undercarriage",
        "Filters",
        "Body damage/rust"
    ).map { InspectablePart(partName = it) }
}