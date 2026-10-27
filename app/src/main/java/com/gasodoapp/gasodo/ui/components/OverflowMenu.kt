package com.gasodoapp.gasodo.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import com.gasodoapp.gasodo.ui.icons.more_vert

/**
 * A single entry in [OverflowMenu]
 */
data class OverflowMenuItem(
    val label: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
)

/**
 * Overflow button for use in the `actions` slot of [TopBarScaffold].
 *
 * The popup is anchored to the button via the enclosing [Box] rather than to the whole
 * actions row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverflowMenu(
    items: List<OverflowMenuItem>,
    modifier: Modifier = Modifier,
    contentDescription: String = "More options",
) {
    var expanded by remember { mutableStateOf(value = false) }
    val groupInteractionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier) {
        // Icon buttons in a menu anchor should expose a tooltip for accessibility.
        TooltipBox(
            positionProvider =
                TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
            tooltip = {
                PlainTooltip { Text(contentDescription) }
            },
            state = rememberTooltipState(),
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(imageVector = more_vert, contentDescription = contentDescription)
            }
        }

        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(index = 0, count = 1),
                interactionSource = groupInteractionSource,
            ) {
                items.forEachIndexed { index, (label, onClick, icon) ->
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onClick()
                        },
                        text = { Text(label) },
                        shape = menuItemShape(index, items.size),
                        leadingIcon = icon?.let { vector ->
                            {
                                Icon(
                                    imageVector = vector,
                                    contentDescription = null,
                                    modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun menuItemShape(index: Int, count: Int): Shape = when {
    count == 1 -> MenuDefaults.standaloneItemShape
    index == 0 -> MenuDefaults.leadingItemShape
    index == count - 1 -> MenuDefaults.trailingItemShape
    else -> MenuDefaults.middleItemShape
}
