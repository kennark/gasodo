package com.example.carrefueltracker.feature.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.carrefueltracker.core.enums.EventType
import com.example.carrefueltracker.ui.icons.add
import com.example.carrefueltracker.ui.icons.build
import com.example.carrefueltracker.ui.icons.close
import com.example.carrefueltracker.ui.icons.content_paste_search
import com.example.carrefueltracker.ui.icons.local_gas_station

@Composable
fun NavigationBarFrame() {
    val navController = rememberNavController()
    val startDestination = BottomBarDestinations.OVERVIEW
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val bottomNavRoutes = BottomBarDestinations.entries.map { it.route }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
    var fabMenuExpanded by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    FloatingActionButtonMenu(
        expanded = fabMenuExpanded,
        button = {
            ToggleFloatingActionButton(
                modifier =
                    Modifier
                        .animateFloatingActionButton(
                            visible = currentRoute != addEventRoute || fabMenuExpanded,
                            alignment = Alignment.BottomEnd,
                        )
                        .focusRequester(focusRequester),
                checked = fabMenuExpanded,
                onCheckedChange = { fabMenuExpanded = !fabMenuExpanded },
            ) {
                val imageVector by remember {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) close else add
                    }
                }
                Icon(
                    painter = rememberVectorPainter(imageVector),
                    contentDescription = null,
                    modifier = Modifier.animateIcon({ checkedProgress }),
                )
            }
        }
    ) {
        FloatingActionButtonMenuItem(
            onClick = {
                fabMenuExpanded = false
                navController.navigate(addEventRoute(EventType.REFUEL))
            },
            icon = { Icon(local_gas_station, null) },
            text = { Text(EventType.REFUEL.toString()) }
        )
        FloatingActionButtonMenuItem(
            onClick = {
                fabMenuExpanded = false
                navController.navigate(addEventRoute(EventType.MAINTENANCE))
            },
            icon = { Icon(build, null) },
            text = { Text(EventType.MAINTENANCE.toString()) }
        )
        FloatingActionButtonMenuItem(
            onClick = {
                fabMenuExpanded = false
                navController.navigate(addEventRoute(EventType.INSPECTION))
            },
            icon = { Icon(content_paste_search, null) },
            text = { Text(EventType.INSPECTION.toString()) }
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
                        imageVector = destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = { Text(destination.label) }
            )
        }
    }
}