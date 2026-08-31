package com.gasodoapp.gasodo.feature.refuel

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.gasodoapp.gasodo.core.database.BaseColumns
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.enums.PaymentMethod
import com.gasodoapp.gasodo.core.utils.toDisplayString
import com.gasodoapp.gasodo.feature.navigation.TopBarScaffold
import com.gasodoapp.gasodo.ui.icons.check_box
import com.gasodoapp.gasodo.ui.icons.check_box_outline_blank
import com.gasodoapp.gasodo.ui.icons.delete
import com.gasodoapp.gasodo.ui.icons.edit
import com.gasodoapp.gasodo.ui.icons.error
import com.gasodoapp.gasodo.ui.icons.expand_circle_down
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

@Composable
fun RefuelScreen(
    viewModel: RefuelScreenViewModel = hiltViewModel<RefuelScreenViewModel>(),
    onNavigateToEdit: (id: UUID) -> Unit
) {
    val pagedItems = viewModel.pagedItems.collectAsLazyPagingItems()

    TopBarScaffold("Recent Refuels", navigationIcon = {}, actions = {}) { paddingValues ->
        MainContent(
            Modifier.padding(paddingValues),
            pagedItems,
            viewModel::onDeleteEvent,
            onNavigateToEdit,
            getSavedLocation = viewModel::getSavedLocation
        )
    }
}

@Composable
private fun MainContent(
    modifier: Modifier = Modifier,
    pagedItems: LazyPagingItems<RefuelEvent>,
    onDeleteEvent: (RefuelEvent) -> Unit,
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
            key = pagedItems.itemKey { it.id }
        ) { index ->
            val refuel = pagedItems[index]
            if (refuel != null) {
                RefuelEventRow(
                    refuelEvent = refuel,
                    onDeleteEvent = onDeleteEvent,
                    onNavigateToEdit = onNavigateToEdit,
                    getSavedLocation = getSavedLocation
                )
            } else {
                // Show placeholder for empty slots while paging
                RefuelScreenLoadingPlaceholder()
            }
        }
        item {
            NoDataCard()
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
    getSavedLocation: suspend (id: UUID) -> SavedLocation?
) {
    val isExpanded = remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth(),
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
                        LocalDate.ofEpochDay(refuelEvent.base.date?.div(86400000) ?: 0).format(
                            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        )
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
                                text = "km",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Column(Modifier.weight(0.7f)) {
                    MediumLabelText("Amount")
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
                            text = "L",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Column(Modifier.weight(0.5f)) {
                    MediumLabelText("Cost")

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
                            text = "€",
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
                            text = "€/L",
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
                        text = "Full Fill-Up",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (refuelEvent.fullFillUp) {
                        Icon(imageVector = check_box, contentDescription = check_box.name)
                    } else {
                        Icon(
                            imageVector = check_box_outline_blank,
                            contentDescription = check_box_outline_blank.name
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
                        MediumLabelText("Payment")

                        if (refuelEvent.paymentMethod != null)
                            ExtraDataText(refuelEvent.paymentMethod.toString())
                        else
                            NoDataText("None selected")
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
                        refuelEvent.base.notes.let { notes ->
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
                            onNavigateToEdit(refuelEvent.id)
                        }) {
                            Icon(imageVector = edit, contentDescription = edit.name)
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
                text = "Refuels end here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Register new refuels to show up here",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * Placeholder for loading state.
 */
@Composable
fun RefuelScreenLoadingPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
fun DeleteDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) {
    BasicAlertDialog(
        { onDismissRequest() }
    ) {
        Card(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text =
                        "Do you want to delete this event?"
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            onDismissRequest()
                        },
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = {
                            onDismissRequest()
                            onConfirmRequest()
                        },
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
fun MediumLabelText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun ExtraDataText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun NoDataText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}


@Preview
@Composable
fun RefuelCardPreview() {
    RefuelEventRow(
        refuelEvent = RefuelEvent(
            base = BaseColumns(
                date = 1000000000000,
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