package com.gasodoapp.gasodo.feature.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gasodoapp.gasodo.ui.components.OverflowMenu
import com.gasodoapp.gasodo.ui.components.OverflowMenuItem

/**
 * Every destination that is reachable from the overflow menu, in display order
 * Different menu groups are separated in own lists
 */
private val overflowDestinations = listOf(
    listOf(
        Destinations.MANAGE_MAINTENANCE_SERVICE_TYPES,
        Destinations.MANAGE_INSPECTABLE_PARTS,
        Destinations.MANAGE_SAVED_LOCATIONS,
    ),
    listOf(
        Destinations.SETTINGS,
        Destinations.ABOUT,
    )
)

/**
 * Overflow menu shown in the app bar.
 *
 * @param onNavigateToManageDestination invoked with the selected manage destination.
 * @param extraItems screen specific entries, shown in their own group above the shared ones.
 */
@Composable
fun AppOverflowMenu(
    onNavigateToManageDestination: (Destinations) -> Unit,
    modifier: Modifier = Modifier,
    extraItems: List<OverflowMenuItem> = emptyList(),
) {
    // Items to show in the menu
    val sharedItems = overflowDestinations.map { listOfDestinations ->
        listOfDestinations.map { destination ->
            OverflowMenuItem(
                label = stringResource(destination.labelRes),
                onClick = { onNavigateToManageDestination(destination) }
            )
        }
    }

    // The caller supplied entries form their own leading group, omitted when there are none so
    // screens that do not use this parameter keep their current grouping.
    val items = listOfNotNull(extraItems.takeIf { it.isNotEmpty() }) + sharedItems

    OverflowMenu(items = items, modifier = modifier)
}
