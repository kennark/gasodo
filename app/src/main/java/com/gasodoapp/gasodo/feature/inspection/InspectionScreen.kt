package com.gasodoapp.gasodo.feature.inspection

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.gasodoapp.gasodo.R
import com.gasodoapp.gasodo.core.database.entity.InspectablePart
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.database.junctions.InspectionEventWithParts
import com.gasodoapp.gasodo.core.settings.Units
import com.gasodoapp.gasodo.core.utils.toDisplayString
import com.gasodoapp.gasodo.feature.navigation.AppOverflowMenu
import com.gasodoapp.gasodo.feature.navigation.Destinations
import com.gasodoapp.gasodo.ui.components.DeleteDialog
import com.gasodoapp.gasodo.ui.components.ExtraDataText
import com.gasodoapp.gasodo.ui.components.MediumLabelText
import com.gasodoapp.gasodo.ui.components.NoDataCard
import com.gasodoapp.gasodo.ui.components.OverflowMenuItem
import com.gasodoapp.gasodo.ui.components.TopBarScaffold
import com.gasodoapp.gasodo.ui.icons.close
import com.gasodoapp.gasodo.ui.icons.delete
import com.gasodoapp.gasodo.ui.icons.edit
import com.gasodoapp.gasodo.ui.icons.expand_circle_down
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

@Composable
fun InspectionScreen(
    onNavigateToManageDestination: (Destinations) -> Unit,
    viewModel: InspectionScreenViewModel = hiltViewModel<InspectionScreenViewModel>(),
    onNavigateToEdit: (id: UUID) -> Unit
) {

    val pagedItems = viewModel.pagedItems.collectAsLazyPagingItems()
    val units by viewModel.units.collectAsStateWithLifecycle()
    val selection by viewModel.selection.collectAsStateWithLifecycle()
    val expandedIds by viewModel.expandedIds.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Back leaves selection mode instead of the screen, matching the clear button.
    BackHandler(enabled = selection.active) { viewModel.onClearSelection() }

    // Leaving the screen always drops the selection and closes every card, so coming back starts
    // from a clean, fully collapsed list.
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onClearSelection()
            viewModel.onCollapseAll()
        }
    }

    TopBarScaffold(
        title = if (selection.active) {
            pluralStringResource(
                R.plurals.common_selection_count,
                selection.count,
                selection.count
            )
        } else {
            stringResource(R.string.inspection_title)
        },
        navigationIcon = if (selection.active) {
            {
                IconButton(onClick = viewModel::onClearSelection) {
                    Icon(
                        imageVector = close,
                        contentDescription = stringResource(R.string.cd_clear_selection)
                    )
                }
            }
        } else {
            null
        },
        actions = {
            if (selection.active) {
                FilledIconButton(
                    onClick = { showDeleteDialog = true },
                    enabled = selection.count > 0,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = delete,
                        contentDescription = stringResource(R.string.cd_delete_selected)
                    )
                }
            } else {
                AppOverflowMenu(
                    onNavigateToManageDestination = onNavigateToManageDestination,
                    extraItems = listOf(
                        OverflowMenuItem(
                            label = stringResource(R.string.common_select_event),
                            onClick = viewModel::onSelectClick
                        )
                    )
                )
            }
        }
    ) { paddingValues ->
        MainContent(
            Modifier.padding(paddingValues),
            pagedItems,
            onDeleteEvent = viewModel::onDeleteInspectionEvent,
            onNavigateToEdit,
            viewModel::getSavedLocation,
            units = units,
            selection = selection,
            onEventTap = viewModel::onEventTap,
            onEventLongPress = viewModel::onEventLongPress,
            expandedIds = expandedIds,
            onToggleExpanded = viewModel::onToggleExpanded
        )
    }

    if (showDeleteDialog) {
        DeleteDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirmRequest = { viewModel.onDeleteSelected() },
            title = pluralStringResource(
                R.plurals.common_delete_selected_title,
                selection.count,
                selection.count
            )
        )
    }
}

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    pagedItems: LazyPagingItems<InspectionEventWithParts>,
    onDeleteEvent: (InspectionEventWithParts) -> Unit,
    onNavigateToEdit: (id: UUID) -> Unit,
    getSavedLocation: suspend (id: UUID) -> SavedLocation?,
    units: Units = Units(),
    selection: InspectionSelection = InspectionSelection(),
    onEventTap: (UUID) -> Unit = {},
    onEventLongPress: (UUID) -> Unit = {},
    expandedIds: Set<UUID> = emptySet(),
    onToggleExpanded: (UUID) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(
            pagedItems.itemCount,
            key = pagedItems.itemKey { it.event.id }
        ) { index ->
            val event = pagedItems[index]
            if (event != null) {
                InspectionEventCard(
                    eventWithParts = event,
                    onDeleteEvent = onDeleteEvent,
                    onNavigateToEdit = onNavigateToEdit,
                    getSavedLocation = getSavedLocation,
                    units = units,
                    isSelected = event.event.id in selection.ids,
                    selectionMode = selection.active,
                    onClick = { onEventTap(event.event.id) },
                    onLongClick = { onEventLongPress(event.event.id) },
                    isExpanded = event.event.id in expandedIds,
                    onToggleExpand = { onToggleExpanded(event.event.id) }
                )
            }
        }

        item {
            NoDataCard(
                title = stringResource(R.string.inspection_list_end_title),
                subtitle = stringResource(R.string.inspection_list_end_subtitle)
            )
        }
    }
}

@Composable
fun InspectionEventCard(
    eventWithParts: InspectionEventWithParts,
    modifier: Modifier = Modifier,
    onDeleteEvent: (InspectionEventWithParts) -> Unit,
    onNavigateToEdit: (id: UUID) -> Unit,
    getSavedLocation: suspend (id: UUID) -> SavedLocation?,
    units: Units = Units(),
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = {},
) {
    val hapticFeedback = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { selected = isSelected }
            .combinedClickable(
                onClick = {
                    // In selection mode a tap toggles the selection; outside of it the whole
                    // card mimics the expand button.
                    if (selectionMode) onClick() else onToggleExpand()
                },
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = if (isSelected) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    MediumLabelText(stringResource(R.string.common_date))
                    val dateString =
                        eventWithParts.event.base.date
                            ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                            .orEmpty()
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (eventWithParts.event.base.mileage != null) {
                        Text(
                            text = stringResource(
                                R.string.common_value_with_unit,
                                eventWithParts.event.base.mileage.toDisplayString(),
                                units.distanceLabel
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        // Spacer for displaying same size cards
                        Spacer(Modifier.height(MaterialTheme.typography.bodyMedium.lineHeight.value.dp))
                    }
                }

                Column(Modifier.weight(0.8f)) {

                    MediumLabelText(stringResource(R.string.inspection_result))

                    Text(
                        text = eventWithParts.event.status?.label()
                            ?: stringResource(R.string.common_no_value),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (eventWithParts.parts.isNotEmpty())
                    Text(
                        text = pluralStringResource(
                            R.plurals.inspection_inspected_area_count,
                            eventWithParts.parts.size,
                            eventWithParts.parts.size
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                else
                    Spacer(Modifier)

                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(durationMillis = 250)
                )
                // Expand button for showing additional info
                FilledTonalIconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.rotate(rotation)
                ) {
                    Icon(
                        imageVector = expand_circle_down,
                        contentDescription = stringResource(
                            if (isExpanded) R.string.cd_collapse_details
                            else R.string.cd_expand_details
                        )
                    )
                }
            }
            if (isExpanded) {
                // Only the extra data that is actually set is rendered, so an inspection without
                // parts, a location or notes shows just the action buttons.
                val hasParts = eventWithParts.parts.isNotEmpty()
                val hasLocation = eventWithParts.event.base.savedLocationId != null
                val hasNotes = eventWithParts.event.base.notes.isNotBlank()
                val hasExtraData = hasParts || hasLocation || hasNotes

                // Resolved here rather than per row so the query only runs for open cards. The
                // visibility above is keyed off the id, so the layout does not shift once the
                // name arrives.
                var location: SavedLocation? by remember(
                    eventWithParts.event.base.savedLocationId
                ) {
                    mutableStateOf(null)
                }
                LaunchedEffect(eventWithParts.event.base.savedLocationId) {
                    location = eventWithParts.event.base.savedLocationId?.let {
                        getSavedLocation(it)
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasExtraData) {
                        Text(
                            text = stringResource(R.string.common_extra_details),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (hasParts) {
                            Column(
                                Modifier.fillMaxWidth()
                            ) {
                                MediumLabelText(stringResource(R.string.inspection_inspected_areas))

                                if (eventWithParts.parts.size > 3) {
                                    for (part in eventWithParts.parts.subList(0, 3)) {
                                        ExtraDataText(part.partName)
                                    }

                                    var showAllPartsDialog by remember { mutableStateOf(false) }

                                    TextButton(onClick = { showAllPartsDialog = true }) {
                                        Text(
                                            pluralStringResource(
                                                R.plurals.inspection_show_all_areas,
                                                eventWithParts.parts.size,
                                                eventWithParts.parts.size
                                            )
                                        )
                                    }
                                    if (showAllPartsDialog)
                                        InspectedPartsBottomSheet(
                                            parts = eventWithParts.parts,
                                            onDismissRequest = { showAllPartsDialog = false }
                                        )
                                } else {
                                    for (serviceType in eventWithParts.parts) {
                                        ExtraDataText(serviceType.partName)
                                    }
                                }
                            }
                        }

                        if (hasLocation) {
                            Column(
                                Modifier.fillMaxWidth()
                            ) {
                                MediumLabelText(stringResource(R.string.common_location))
                                location?.let { ExtraDataText(it.name) }
                            }
                        }

                        if (hasNotes) {
                            Column(
                                Modifier.fillMaxWidth()
                            ) {
                                MediumLabelText(stringResource(R.string.common_notes))
                                ExtraDataText(eventWithParts.event.base.notes)
                            }
                        }
                    }

                    var showDeleteDialog by remember { mutableStateOf(false) }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (!selectionMode) {
                            FilledIconButton(
                                onClick = {
                                    showDeleteDialog = true
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(
                                    imageVector = delete,
                                    contentDescription = stringResource(R.string.cd_delete_event)
                                )
                            }
                        }
                        FilledIconButton(onClick = {
                            onNavigateToEdit(eventWithParts.event.id)
                        }) {
                            Icon(
                                imageVector = edit,
                                contentDescription = stringResource(R.string.cd_edit_event)
                            )
                        }
                    }

                    if (showDeleteDialog)
                        DeleteDialog(
                            onDismissRequest = {
                                showDeleteDialog = false
                            },
                            onConfirmRequest = {
                                onDeleteEvent(eventWithParts)
                            })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectedPartsBottomSheet(
    parts: List<InspectablePart>,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Text(
            text = pluralStringResource(
                R.plurals.inspection_parts_title,
                parts.size,
                parts.size
            ),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(parts) { service ->

                Text(
                    service.partName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}