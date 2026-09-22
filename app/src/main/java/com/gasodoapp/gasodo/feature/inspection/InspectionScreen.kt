package com.gasodoapp.gasodo.feature.inspection

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.gasodoapp.gasodo.core.database.entity.InspectablePart
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.database.junctions.InspectionEventWithParts
import com.gasodoapp.gasodo.ui.components.DeleteDialog
import com.gasodoapp.gasodo.ui.components.ExtraDataText
import com.gasodoapp.gasodo.ui.components.MediumLabelText
import com.gasodoapp.gasodo.ui.components.NoDataCard
import com.gasodoapp.gasodo.ui.components.NoDataText
import com.gasodoapp.gasodo.ui.components.TopBarScaffold
import com.gasodoapp.gasodo.ui.icons.delete
import com.gasodoapp.gasodo.ui.icons.edit
import com.gasodoapp.gasodo.ui.icons.expand_circle_down
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

@Composable
fun InspectionScreen(
    viewModel: InspectionScreenViewModel = hiltViewModel<InspectionScreenViewModel>(),
    onNavigateToEdit: (id: UUID) -> Unit
) {

    val pagedItems = viewModel.pagedItems.collectAsLazyPagingItems()

    TopBarScaffold("Recent Inspections") { paddingValues ->
        MainContent(
            Modifier.padding(paddingValues),
            pagedItems,
            onDeleteEvent = viewModel::onDeleteInspectionEvent,
            onNavigateToEdit,
            viewModel::getSavedLocation
        )
    }
}

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    pagedItems: LazyPagingItems<InspectionEventWithParts>,
    onDeleteEvent: (InspectionEventWithParts) -> Unit,
    onNavigateToEdit: (id: UUID) -> Unit,
    getSavedLocation: suspend (id: UUID) -> SavedLocation?
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
                    event,
                    onDeleteEvent,
                    onNavigateToEdit,
                    getSavedLocation
                )
            }
        }

        item {
            NoDataCard("Inspection ends here", "Register new inspection events to show up here")
        }
    }
}

@Composable
fun InspectionEventCard(
    eventWithParts: InspectionEventWithParts,
    onDeleteEvent: (InspectionEventWithParts) -> Unit,
    onNavigateToEdit: (id: UUID) -> Unit,
    getSavedLocation: suspend (id: UUID) -> SavedLocation?
) {
    val isExpanded = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
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
                    MediumLabelText("Date")
                    // Convert the date from epoch milliseconds to epoch days
                    val dateString =
                        LocalDate.ofEpochDay(eventWithParts.event.base.date?.div(86400000) ?: 0)
                            .format(
                                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            )
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (eventWithParts.event.base.mileage != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = eventWithParts.event.base.mileage.toString(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "km",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        // Spacer for displaying same size cards
                        Spacer(Modifier.height(MaterialTheme.typography.bodyMedium.lineHeight.value.dp))
                    }
                }

                Column(Modifier.weight(0.8f)) {

                    MediumLabelText("Result")

                    Text(
                        text = eventWithParts.event.status?.toString() ?: "-",
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
                        text = eventWithParts.parts.size.let {
                            "$it inspected area" +
                                    if (it > 1) "s" else ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                else
                    Spacer(Modifier)

                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded.value) 180f else 0f,
                    animationSpec = tween(durationMillis = 250)
                )
                // Expand button for showing additional info
                FilledTonalIconButton(
                    onClick = { isExpanded.value = !isExpanded.value },
                    modifier = Modifier.rotate(rotation)
                ) {
                    Icon(
                        imageVector = expand_circle_down,
                        contentDescription = if (isExpanded.value) "Collapse details" else "Expand details"
                    )
                }
            }
            if (isExpanded.value) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Extra Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (eventWithParts.parts.isNotEmpty()) {
                        Column(
                            Modifier.fillMaxWidth()
                        ) {
                            MediumLabelText("Inspected Areas")

                            if (eventWithParts.parts.size > 3) {
                                for (part in eventWithParts.parts.subList(0, 3)) {
                                    ExtraDataText(part.partName)
                                }

                                var showAllPartsDialog by remember { mutableStateOf(false) }

                                TextButton(onClick = { showAllPartsDialog = true }) {
                                    Text("Show All (${eventWithParts.parts.size} actions)")
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

                    Column(
                        Modifier.fillMaxWidth()
                    ) {
                        var location: SavedLocation? by remember { mutableStateOf(null) }
                        LaunchedEffect(eventWithParts) {
                            location = eventWithParts.event.base.savedLocationId?.let {
                                getSavedLocation(it)
                            }
                        }
                        MediumLabelText("Location")

                        if (location == null)
                            NoDataText("No location set")
                        else {
                            ExtraDataText(location!!.name)
                        }

                    }

                    Column(
                        Modifier.fillMaxWidth()
                    ) {
                        MediumLabelText("Notes")
                        eventWithParts.event.base.notes.let { notes ->
                            if (notes.isNotEmpty()) {
                                ExtraDataText(notes)
                            } else {
                                NoDataText("No notes")
                            }
                        }
                    }

                    var showDeleteDialog by remember { mutableStateOf(false) }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilledIconButton(
                            onClick = {
                                showDeleteDialog = true
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(imageVector = delete, contentDescription = delete.name)
                        }
                        FilledIconButton(onClick = {
                            onNavigateToEdit(eventWithParts.event.id)
                        }) {
                            Icon(imageVector = edit, contentDescription = edit.name)
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
            text = "Inspected areas/parts (${parts.size})",
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