package com.example.carrefueltracker.feature.navigation

import com.example.carrefueltracker.R


enum class Destinations(
    val route: String,
    val label: String,
    val icon: Int
) {
    OVERVIEW(
        "overview",
        "Overview",
        R.drawable.overview
    ),
    REFUELS(
        "refuels",
        "Refuels",
        R.drawable.local_gas_station_24px
    ),
    ADD(
        "add",
        "Add Event",
        R.drawable.add_24px
    )
}