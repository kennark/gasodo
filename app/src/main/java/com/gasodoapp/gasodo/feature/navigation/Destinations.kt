package com.gasodoapp.gasodo.feature.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.gasodoapp.gasodo.core.enums.EventType
import com.gasodoapp.gasodo.ui.icons.build
import com.gasodoapp.gasodo.ui.icons.content_paste_search
import com.gasodoapp.gasodo.ui.icons.local_gas_station
import com.gasodoapp.gasodo.ui.icons.overview
import java.util.UUID

enum class DialogDestinations(
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

enum class Destinations(
    val route: String,
    val label: String,
    val showBottomBar: Boolean = false,
    val icon: ImageVector? = null
) {
    MANAGE_MAINTENANCE_SERVICE_TYPES(
        "manage/maintenance-service-types",
        "Maintenance Services"
    ),
    MANAGE_INSPECTABLE_PARTS(
        "manage/inspectable-parts",
        "Inspectable Parts"
    ),
    OVERVIEW(
        "overview",
        "Overview",
        true,
        overview
    ),
    REFUELS(
        "refuels",
        "Refuels",
        true,
        local_gas_station
    ),
    MAINTENANCE(
        "maintenance",
        "Maintenance",
        true,
        build
    ),
    INSPECTION(
        "inspection",
        "Inspection",
        true,
        content_paste_search
    );

    companion object {
        /**
         * Resolves a navigation route back to its [Destinations] entry.
         *
         * Returns `null` for routes that don't belong to this enum (for example dialog
         * routes or a not-yet-initialized back stack).
         */
        fun getFromRoute(route: String?): Destinations? =
            entries.firstOrNull { it.route == route }
    }
}

const val ADD_EVENT_TYPE_ARG = "type"
const val EDIT_EVENT_ID_ARG = "id"

// route pattern used in NavHost registration
val addEventRoute = "${DialogDestinations.ADD.route}/{$ADD_EVENT_TYPE_ARG}"
val editEventRoute = "${DialogDestinations.EDIT.route}/{$ADD_EVENT_TYPE_ARG}/{$EDIT_EVENT_ID_ARG}"

// helper to build a concrete navigable route
fun addEventRoute(type: EventType) = "${DialogDestinations.ADD.route}/${type.name}"
fun editEventRoute(type: EventType, id: UUID) =
    "${DialogDestinations.EDIT.route}/${type.name}/${id}"