package com.gasodoapp.gasodo.feature.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import com.gasodoapp.gasodo.core.enums.EventType
import com.gasodoapp.gasodo.feature.addevent.AddEventScreen
import com.gasodoapp.gasodo.feature.overview.OverviewScreen
import com.gasodoapp.gasodo.feature.refuel.RefuelScreen
import java.util.UUID

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: BottomBarDestinations,
    modifier: Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.route,
        modifier = modifier
    ) {
        BottomBarDestinations.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    BottomBarDestinations.OVERVIEW -> OverviewScreen()
                    BottomBarDestinations.REFUELS -> RefuelScreen(onNavigateToEdit = { id: UUID ->
                        navController.navigate(editEventRoute(EventType.REFUEL, id))
                    })
                }
            }
        }

        dialog(
            route = addEventRoute,
            arguments = listOf(
                navArgument(ADD_EVENT_TYPE_ARG) {
                    type = NavType.EnumType(EventType::class.java)
                }
            ),
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            AddEventScreen(
                onDismiss = { navController.popBackStack() },
            )
        }

        dialog(
            route = editEventRoute,
            arguments = listOf(
                navArgument(ADD_EVENT_TYPE_ARG) {
                    type = NavType.EnumType(EventType::class.java)
                },
                navArgument(EDIT_EVENT_ID_ARG) {
                    type = NavType.StringType
                }
            ),
            dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            AddEventScreen(
                onDismiss = { navController.popBackStack() },
            )
        }
    }
}