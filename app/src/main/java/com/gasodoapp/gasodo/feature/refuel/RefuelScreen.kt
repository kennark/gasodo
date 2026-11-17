package com.gasodoapp.gasodo.feature.refuel

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.gasodoapp.gasodo.R
import com.gasodoapp.gasodo.core.database.BaseColumns
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.enums.PaymentMethod
import com.gasodoapp.gasodo.core.settings.Units
import com.gasodoapp.gasodo.core.utils.toDisplayString
import com.gasodoapp.gasodo.feature.navigation.AppOverflowMenu
import com.gasodoapp.gasodo.feature.navigation.Destinations
import com.gasodoapp.gasodo.ui.components.DeleteDialog
import com.gasodoapp.gasodo.ui.components.ExtraDataText
import com.gasodoapp.gasodo.ui.components.LoadingPlaceholder
import com.gasodoapp.gasodo.ui.components.MediumLabelText
import com.gasodoapp.gasodo.ui.components.NoDataCard
import com.gasodoapp.gasodo.ui.components.NoDataText
import com.gasodoapp.gasodo.ui.components.OverflowMenuItem
import com.gasodoapp.gasodo.ui.components.TopBarScaffold
import com.gasodoapp.gasodo.ui.icons.check_box
import com.gasodoapp.gasodo.ui.icons.check_box_outline_blank
import com.gasodoapp.gasodo.ui.icons.close
import com.gasodoapp.gasodo.ui.icons.delete
import com.gasodoapp.gasodo.ui.icons.edit
import com.gasodoapp.gasodo.ui.icons.expand_circle_down
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

@Composable
fun RefuelScreen(
    onNavigateToManageDestination: (Destinations) -> Unit,
    viewModel: RefuelScreenViewModel = hiltViewModel<RefuelScreenViewModel>(),
    onNavigateToEdit: (id: UUID) -> Unit
) {
    val pagedItems = viewModel.pagedItems.collectAsLazyPagingItems()
    val units by viewModel.units.collectAsStateWithLifecycle()
    val selection by viewModel.selection.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Back leaves selection mode instead of the screen, matching the clear button.
    BackHandler(enabled = selection.active) { viewModel.onClearSelection() }

    // Leaving the screen always drops the selection, so coming back starts from a clean list.
    DisposableEffect(Unit) {
        onDispose { viewModel.onClearSelection() }
    }

    TopBarScaffold(
        title = if (selection.active) {
            pluralStringResource(
                R.plurals.common_selection_count,
                selection.count,
                selection.count
            )
        } else {
            stringResource(R.string.refuel_title)
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
            viewModel::onDeleteEvent,
            onNavigateToEdit,
            getSavedLocation = viewModel::getSavedLocation,
            units = units,
            selection = selection,
            onEventTap = viewModel::onEventTap,
            onEventLongPress = viewModel::onEventLongPress
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
private fun MainContent(
    modifier: Modifier = Modifier,
    pagedItems: LazyPagingItems<RefuelEvent>,
    onDeleteEvent: (RefuelEvent) -> Unit,
    onNavigateToEdit: (id: UUID) -> Unit,
    getSavedLocation: suspend (id: UUID) -> SavedLocation?,
    units: Units = Units(),
    selection: RefuelSelection = RefuelSelection(),
    onEventTap: (UUID) -> Unit = {},
    onEventLongPress: (UUID) -> Unit = {},
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
            key = pagedItems.itemKey { it.id }
        ) { index ->
            val refuel = pagedItems[index]
            if (refuel != null) {
                RefuelEventRow(
                    refuelEvent = refuel,
                    onDeleteEvent = onDeleteEvent,
                    onNavigateToEdit = onNavigateToEdit,
                    getSavedLocation = getSavedLocation,
                    units = units,
                    isSelected = refuel.id in selection.ids,
                    selectionMode = selection.active,
                    onClick = { onEventTap(refuel.id) },
                    onLongClick = { onEventLongPress(refuel.id) }
                )
            } else {
                // Show placeholder for empty slots while paging
                LoadingPlaceholder()
            }
        }
        item {
            NoDataCard(
                title = stringResource(R.string.refuel_list_end_title),
                subtitle = stringResource(R.string.refuel_list_end_subtitle)
            )
        }
    }
}

/**
 * Displays a single refuel event row/card.
 */
@Composable
fun RefuelEventRow(
    refuelEvent: RefuelEvent,
    modifier: Modifier = Modifier,
    onDeleteEvent: (RefuelEvent) -> Unit,
    onNavigateToEdit: (id: UUID) -> Unit,
    getSavedLocation: suspend (id: UUID) -> SavedLocation?,
    units: Units = Units(),
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val isExpanded = remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { selected = isSelected }
            .combinedClickable(
                onClick = onClick,
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
                        refuelEvent.base.date
                            ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                            .orEmpty()
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (refuelEvent.base.mileage != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = refuelEvent.base.mileage.toString(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = units.distanceLabel,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Column(Modifier.weight(0.7f)) {
                    MediumLabelText(stringResource(R.string.common_amount))
                    val amount = refuelEvent.amountLiters ?: BigDecimal.ZERO
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = amount.toDisplayString(2),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = units.liquidLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Column(Modifier.weight(0.5f)) {
                    MediumLabelText(stringResource(R.string.common_cost))

                    val total = refuelEvent.totalCost ?: BigDecimal.ZERO
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = total.toDisplayString(2),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = units.currencyLabel,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    val price = refuelEvent.pricePerLiter ?: BigDecimal.ZERO
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = price.toDisplayString(3),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = units.pricePerLiquidLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Full Fill-Up with icon together at the back (right)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.common_full_fill_up),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (refuelEvent.fullFillUp) {
                        Icon(
                            imageVector = check_box,
                            contentDescription = stringResource(R.string.cd_full_fill_up_yes)
                        )
                    } else {
                        Icon(
                            imageVector = check_box_outline_blank,
                            contentDescription = stringResource(R.string.cd_full_fill_up_no)
                        )
                    }
                }
                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded.value) 180f else 0f,
                    animationSpec = tween(durationMillis = 250)
                )
                // Expand button for showing additional fuel consumption info
                FilledTonalIconButton(
                    onClick = { isExpanded.value = !isExpanded.value },
                    modifier = Modifier.rotate(rotation)
                ) {
                    Icon(
                        imageVector = expand_circle_down,
                        contentDescription = stringResource(
                            if (isExpanded.value) R.string.cd_collapse_details
                            else R.string.cd_expand_details
                        )
                    )
                }
            }

            if (isExpanded.value) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.common_extra_details),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(
                        Modifier.fillMaxWidth()
                    ) {
                        MediumLabelText(stringResource(R.string.common_payment))

                        if (refuelEvent.paymentMethod != null)
                            ExtraDataText(refuelEvent.paymentMethod.label())
                        else
                            NoDataText(stringResource(R.string.common_none_selected))
                    }

                    Column(
                        Modifier.fillMaxWidth()
                    ) {
                        var location: SavedLocation? by remember { mutableStateOf(null) }
                        LaunchedEffect(refuelEvent) {
                            location = refuelEvent.base.savedLocationId?.let {
                                getSavedLocation(it)
                            }
                        }
                        MediumLabelText(stringResource(R.string.common_location))

                        if (location == null)
                            NoDataText(stringResource(R.string.common_no_location_set))
                        else {
                            ExtraDataText(location!!.name)
                        }

                    }

                    Column(
                        Modifier.fillMaxWidth()
                    ) {
                        MediumLabelText(stringResource(R.string.common_notes))
                        refuelEvent.base.notes.let { notes ->
                            if (notes.isNotEmpty()) {
                                ExtraDataText(notes)
                            } else {
                                NoDataText(stringResource(R.string.common_no_notes))
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
                            onNavigateToEdit(refuelEvent.id)
                        }) {
                            Icon(
                                imageVector = edit,
                                contentDescription = stringResource(R.string.cd_edit_event)
                            )
                        }
                    }

                    if (showDeleteDialog)
                        DeleteDialog(
                            {
                                showDeleteDialog = false
                            },
                            onConfirmRequest = {
                                onDeleteEvent(refuelEvent)
                            })
                }
            }
        }
    }
}

@Preview(wallpaper = Wallpapers.NONE)
@Composable
fun RefuelCardPreview() {
    RefuelEventRow(
        refuelEvent = RefuelEvent(
            base = BaseColumns(
                date = LocalDate.of(2024, 1, 15),
                mileage = 123543,
            ),
            amountLiters = BigDecimal.TEN,
            pricePerLiter = BigDecimal("1.599"),
            totalCost = BigDecimal("12.22"),
            paymentMethod = PaymentMethod.CARD,
            fullFillUp = true
        ),
        onDeleteEvent = {},
        onNavigateToEdit = {},
        getSavedLocation = { return@RefuelEventRow SavedLocation(name = "name") }
    )
}