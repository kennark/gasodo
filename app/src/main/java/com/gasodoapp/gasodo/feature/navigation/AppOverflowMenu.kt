package com.gasodoapp.gasodo.feature.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gasodoapp.gasodo.ui.components.OverflowMenu
import com.gasodoapp.gasodo.ui.components.OverflowMenuItem

/**
 * Every destination that is reachable from the overflow menu, in display order
 */
private val manageDestinations = listOf(
    Destinations.MANAGE_MAINTENANCE_SERVICE_TYPES,
    Destinations.MANAGE_INSPECTABLE_PARTS,
    Destinations.MANAGE_SAVED_LOCATIONS,
)

/**
 * Overflow menu shown in the app bar.
 *
 * @param onNavigateToManageDestination invoked with the selected manage destination.
 */
@Composable
fun AppOverflowMenu(
    onNavigateToManageDestination: (Destinations) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Items to show in the menu
    val items = manageDestinations.map { destination ->
        OverflowMenuItem(
            label = destination.label,
            onClick = { onNavigateToManageDestination(destination) }
        )
    }

    OverflowMenu(items = items, modifier = modifier)
}
