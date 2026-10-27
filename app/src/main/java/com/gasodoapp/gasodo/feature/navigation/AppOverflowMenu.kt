package com.gasodoapp.gasodo.feature.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gasodoapp.gasodo.ui.components.OverflowMenu
import com.gasodoapp.gasodo.ui.components.OverflowMenuItem

/**
 * Overflow menu shown in the app bar.
 */
@Composable
fun AppOverflowMenu(
    onNavigateToMaintenanceServiceTypes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Items to show in the menu
    val items = listOf(
        OverflowMenuItem(
            label = Destinations.MANAGE_MAINTENANCE_SERVICE_TYPES.label,
            onClick = onNavigateToMaintenanceServiceTypes
        ),
    )

    OverflowMenu(items = items, modifier = modifier)
}
