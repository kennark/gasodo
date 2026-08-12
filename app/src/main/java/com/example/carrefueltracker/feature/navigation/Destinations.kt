package com.example.carrefueltracker.feature.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.carrefueltracker.ui.icons.local_gas_station
import com.example.carrefueltracker.ui.icons.overview


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
    )
}