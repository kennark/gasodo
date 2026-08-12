package com.example.carrefueltracker.feature.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.carrefueltracker.ui.icons.add

@Composable
fun NavigationBarFrame() {
    val navController = rememberNavController()
    val startDestination = BottomBarDestinations.OVERVIEW
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val bottomNavRoutes = BottomBarDestinations.entries.map { it.route }
    Scaffold(
        floatingActionButton = {
            FAB(currentRoute, navController)
        },
        bottomBar = {
            AnimatedVisibility(
                visible = currentRoute in bottomNavRoutes,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                BottomNavigationBar(currentRoute, navController)
            }
        }
    ) { innerPadding ->
        AppNavHost(navController, startDestination, modifier = Modifier.padding(innerPadding))
    }
}

@Composable
private fun FAB(
    currentRoute: String?,
    navController: NavHostController
) {
    if (currentRoute != Destinations.ADD.route)
        FloatingActionButton(
            onClick = {
                navController.navigate(route = Destinations.ADD.route)
            },

            ) {
            Icon(
                imageVector = add,
                contentDescription = Destinations.ADD.name
            )
        }
}

@Composable
private fun BottomNavigationBar(
    currentRoute: String?,
    navController: NavHostController
) {
    NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
        BottomBarDestinations.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = {
                    navController.navigate(route = destination.route) {
                        popUpTo(navController.graph.startDestinationId)
                        restoreState = true
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        painterResource(destination.icon),
                        contentDescription = destination.label
                    )
                },
                label = { Text(destination.label) }
            )
        }
    }
}