package com.gasodoapp.gasodo.feature.maintenance

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
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import com.gasodoapp.gasodo.core.utils.toDisplayString
import com.gasodoapp.gasodo.feature.navigation.TopBarScaffold
import com.gasodoapp.gasodo.feature.refuel.DeleteDialog
import com.gasodoapp.gasodo.feature.refuel.ExtraDataText
import com.gasodoapp.gasodo.feature.refuel.MediumLabelText
import com.gasodoapp.gasodo.feature.refuel.NoDataText
import com.gasodoapp.gasodo.ui.icons.delete
import com.gasodoapp.gasodo.ui.icons.edit
import com.gasodoapp.gasodo.ui.icons.error
import com.gasodoapp.gasodo.ui.icons.expand_circle_down
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@Composable
fun MaintenanceScreen(
    viewModel: MaintenanceScreenViewModel = hiltViewModel<MaintenanceScreenViewModel>()
) {
    val pagedItems = viewModel.pagedItems.collectAsLazyPagingItems()

    TopBarScaffold(title = "Recent Maintenance Entries") { paddingValues ->
        MainContent(
            Modifier.padding(paddingValues),
            pagedItems,
            viewModel::onDeleteMaintenanceEvent
        )
    }
}

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    pagedItems: LazyPagingItems<MaintenanceEventWithServices>,
    onDeleteMaintenanceEvent: (MaintenanceEventWithServices) -> Unit
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
                MaintenanceEventRow(
                    eventWithServices = event,
                    onDeleteMaintenanceEvent = onDeleteMaintenanceEvent
                )
            }
        }

        item {
            NoDataCard()
        }
    }
}


@Composable
fun MaintenanceEventRow(
    eventWithServices: MaintenanceEventWithServices,
    onDeleteMaintenanceEvent: (MaintenanceEventWithServices) -> Unit
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
                        LocalDate.ofEpochDay(eventWithServices.event.base.date?.div(86400000) ?: 0)
                            .format(
                                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            )
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (eventWithServices.event.base.mileage != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = eventWithServices.event.base.mileage.toString(),
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
                Column(Modifier.weight(0.5f)) {
                    MediumLabelText("Cost")

                    val total = eventWithServices.event.totalCost ?: BigDecimal.ZERO
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
                            text = "€",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = eventWithServices.services.size.let {
                            "$it service action" +
                                    if (it > 1) "s" else ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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

                    Column(
                        Modifier.fillMaxWidth()
                    ) {
                        MediumLabelText("Done Work")

                        if (eventWithServices.services.size > 3) {
                            for (serviceType in eventWithServices.services.subList(0, 3)) {
                                ExtraDataText(serviceType.serviceName)
                            }

                            var showDoneWorkDialog by remember { mutableStateOf(false) }

                            TextButton(onClick = { showDoneWorkDialog = true }) {
                                Text("Show All (${eventWithServices.services.size} actions)")
                            }
                            if (showDoneWorkDialog)
                                DoneTasksBottomSheet(
                                    doneWork = eventWithServices.services,
                                    onDismissRequest = { showDoneWorkDialog = false }
                                )
                        } else {
                            for (serviceType in eventWithServices.services) {
                                ExtraDataText(serviceType.serviceName)
                            }
                        }

                    }

                    Column(
                        Modifier.fillMaxWidth()
                    ) {
                        MediumLabelText("Payment")

                        NoDataText("Placeholder")
                    }

                    Column(
                        Modifier.fillMaxWidth()
                    ) {
                        var location: SavedLocation? by remember { mutableStateOf(null) }

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
                        eventWithServices.event.base.notes.let { notes ->
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
                            // to be added
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
                                onDeleteMaintenanceEvent(eventWithServices)
                            })
                }
            }
        }
    }
}


@Composable
fun NoDataCard() {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Maintenance ends here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Register new maintenance events to show up here",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoneTasksBottomSheet(
    doneWork: List<MaintenanceServiceType>,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Text(
            text = "Done services (${doneWork.size})",
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
            items(doneWork) { service ->

                Text(
                    service.serviceName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}