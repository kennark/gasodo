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
import com.gasodoapp.gasodo.feature.inspection.InspectionScreen
import com.gasodoapp.gasodo.feature.maintenance.MaintenanceScreen
import com.gasodoapp.gasodo.feature.manage.inspectablepart.InspectablePartScreen
import com.gasodoapp.gasodo.feature.manage.maintenanceservicetype.MaintenanceServiceTypeScreen
import com.gasodoapp.gasodo.feature.manage.savedlocation.SavedLocationScreen
import com.gasodoapp.gasodo.feature.overview.OverviewScreen
import com.gasodoapp.gasodo.feature.refuel.RefuelScreen
import java.util.UUID

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
        val onNavigateToManageDestination: (Destinations) -> Unit = { destination ->
            navController.navigate(destination.route)
        }

        Destinations.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    Destinations.MANAGE_MAINTENANCE_SERVICE_TYPES -> MaintenanceServiceTypeScreen(
                        onBack = { navController.popBackStack() }
                    )

                    Destinations.MANAGE_INSPECTABLE_PARTS -> InspectablePartScreen(
                        onBack = { navController.popBackStack() }
                    )

                    Destinations.MANAGE_SAVED_LOCATIONS -> SavedLocationScreen(
                        onBack = { navController.popBackStack() }
                    )
                    Destinations.OVERVIEW -> OverviewScreen(
                        onNavigateToManageDestination = onNavigateToManageDestination
                    )

                    Destinations.REFUELS -> RefuelScreen(
                        onNavigateToManageDestination = onNavigateToManageDestination,
                        onNavigateToEdit = { id: UUID ->
                            navController.navigate(editEventRoute(EventType.REFUEL, id))
                        }
                    )

                    Destinations.MAINTENANCE -> MaintenanceScreen(
                        onNavigateToManageDestination = onNavigateToManageDestination,
                        onNavigateToEdit = { id: UUID ->
                            navController.navigate(editEventRoute(EventType.MAINTENANCE, id))
                        }
                    )

                    Destinations.INSPECTION -> InspectionScreen(
                        onNavigateToManageDestination = onNavigateToManageDestination,
                        onNavigateToEdit = { id: UUID ->
                            navController.navigate(editEventRoute(EventType.INSPECTION, id))
                        }
                    )
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
            dialogProperties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            )
        ) {
            AddEventScreen(
                onDismiss = { navController.popBackStack() },
                onSnackbarMessage = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle["snackbar_text"] = "Event saved"
                }
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
            dialogProperties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            )
        ) {
            AddEventScreen(
                onDismiss = { navController.popBackStack() },
                onSnackbarMessage = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle["snackbar_text"] = "Event updated"
                }
            )
        }
    }
}