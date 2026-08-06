package com.example.carrefueltracker.feature.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.carrefueltracker.feature.addevent.AddEventScreen
import com.example.carrefueltracker.feature.overview.OverviewScreen
import com.example.carrefueltracker.feature.refuel.RefuelScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Destinations,
    modifier: Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.route,
        modifier = modifier
    ) {
        Destinations.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destinations.OVERVIEW -> OverviewScreen()
                    Destinations.REFUELS -> RefuelScreen()
                    Destinations.ADD -> AddEventScreen({ navController.popBackStack() })
                }
            }
        }
    }
}