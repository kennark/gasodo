package com.example.carrefueltracker.feature.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.carrefueltracker.core.enums.EventType
import com.example.carrefueltracker.ui.icons.local_gas_station
import com.example.carrefueltracker.ui.icons.overview
import java.util.UUID


enum class BottomBarDestinations(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    OVERVIEW(
        "overview",
        "Overview",
        overview
    ),
    REFUELS(
        "refuels",
        "Refuels",
        local_gas_station
    )
}

enum class Destinations(
    val route: String,
    val label: String,
) {
    ADD(
        "add",
        "Add Event"
    ),
    EDIT(
        "edit",
        "Edit Event"
    )
}

const val ADD_EVENT_TYPE_ARG = "type"
const val EDIT_EVENT_ID_ARG = "id"

// route pattern used in NavHost registration
val addEventRoute = "${Destinations.ADD.route}/{$ADD_EVENT_TYPE_ARG}"
val editEventRoute = "${Destinations.EDIT.route}/{$ADD_EVENT_TYPE_ARG}/{$EDIT_EVENT_ID_ARG}"

// helper to build a concrete navigable route
fun addEventRoute(type: EventType) = "${Destinations.ADD.route}/${type.name}"
fun editEventRoute(type: EventType, id: UUID) = "${Destinations.EDIT.route}/${type.name}/${id}"