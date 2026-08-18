package com.example.carrefueltracker.feature.overview

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.getSelectedEndDate
import androidx.compose.material3.getSelectedStartDate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.carrefueltracker.core.utils.toDisplayString
import com.example.carrefueltracker.ui.icons.error
import com.example.carrefueltracker.ui.icons.local_gas_station
import java.math.BigDecimal
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    viewModel: OverviewScreenViewModel = hiltViewModel<OverviewScreenViewModel>()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val refuelData by viewModel.refuelData.collectAsState()

    val totalCost by viewModel.totalCost.collectAsState()
    val averageCost by viewModel.averagePricePerLiter.collectAsState()
    val totalLiters by viewModel.totalLiters.collectAsState()
    val totalMileage by viewModel.totalMileage.collectAsState()
    val fuelConsumption by viewModel.fuelConsumption.collectAsState()
    val fuelCost by viewModel.fuelCost.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Year and Month Selector Card
        YearMonthSelectorCard(
            dateRangePickerState = viewModel.dateRangePickerState,
            onDateSelected = viewModel::onDateSelected
        )
        HorizontalDivider()

        if (viewModel.dateRangePickerState.selectedEndDateMillis != null) {
            // Loading State
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp), color = MaterialTheme.colorScheme.primary
                )
            } else if (refuelData.isEmpty()) {
                // Empty State
                EmptyRefuelState()
            } else {
                // Statistics Cards
                RefuelStatsCard(
                    totalCost = totalCost,
                    averageCost = averageCost,
                    totalLiters = totalLiters,
                    totalMileage = totalMileage,
                    fuelConsumption = fuelConsumption,
                    fuelCost = fuelCost
                )

            }
        } else {
            MissingEndDateCard()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearMonthSelectorCard(
    dateRangePickerState: DateRangePickerState, onDateSelected: () -> Unit
) {
    var showModal by remember { mutableStateOf(false) }
    TextField(
        value = dateRangePickerState.getSelectedStartDate()
            ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                + " - " + (dateRangePickerState.getSelectedEndDate()
            ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "?"),
        onValueChange = {},
        label = { Text("Time Period") },
        readOnly = true,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(dateRangePickerState) {
                awaitEachGesture {
                    // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                    // in the Initial pass to observe events before the text field consumes them
                    // in the Main pass.
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        showModal = true
                    }
                }
            },
    )
    if (showModal) {
        DatePickerDialog(
            onDismissRequest = { showModal = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected()
                    showModal = false
                }) {
                    Text("OK")
                }
            }, dismissButton = {
                TextButton(onClick = { showModal = false }) {
                    Text("Cancel")
                }
            }) {
            DateRangePicker(
                state = dateRangePickerState,
                showModeToggle = false
            )
        }
    }
}


@Preview
@Composable
fun EmptyRefuelState() {
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
                imageVector = local_gas_station,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No refuels found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Select a different period to see your refuel history",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview
@Composable
fun MissingEndDateCard() {
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
                text = "Time period not set",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Select an end date to see statistics",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun RefuelStatsCard(
    totalCost: BigDecimal,
    averageCost: BigDecimal?,
    totalLiters: BigDecimal,
    totalMileage: Long?,
    fuelConsumption: BigDecimal?,
    fuelCost: BigDecimal?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.padding(2.dp)) {
                    Icon(
                        imageVector = local_gas_station,
                        contentDescription = local_gas_station.name
                    )
                }
                Text(
                    text = "Refuel Statistics",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        LabelText("Total Refuel Cost")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ValueText(totalCost.toDisplayString(2))
                            ValueText("€")
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        LabelText("Total Refuelled")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ValueText(totalLiters.toDisplayString(2))
                            ValueText("L")
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        LabelText("Average Fuel Price")
                        if (averageCost != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ValueText(averageCost.toDisplayString(2))
                                ValueText("€/L")
                            }
                        } else {
                            NoDataText()
                        }
                    }
                }
                VerticalDivider()
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        LabelText("Total Distance Travelled")
                        if (totalMileage != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ValueText(totalMileage.toString())
                                ValueText("km")
                            }
                        } else {
                            NoDataText()
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        LabelText("Average Fuel Consumption")
                        if (fuelConsumption != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ValueText(fuelConsumption.toDisplayString(2))
                                ValueText("L/100 km")
                            }
                        } else {
                            NoDataText()
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        LabelText("Average Fuel Cost")
                        if (fuelCost != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                ValueText(fuelCost.toDisplayString(2))
                                ValueText("€/100 km")
                            }
                        } else {
                            NoDataText()
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun NoDataText() {
    ValueText("-")
}

@Composable
fun ValueText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun LabelText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall
    )
}


@Preview
@Composable
fun RefuelStatsCardPreview() {
    RefuelStatsCard(
        totalCost = BigDecimal("10.00"),
        averageCost = BigDecimal("1.23"),
        totalLiters = BigDecimal("123.45"),
        totalMileage = 934085L,
        fuelConsumption = BigDecimal("5.46"),
        fuelCost = BigDecimal("9.99")
    )
}

@Preview
@Composable
fun RefuelStatsCardEmptyPreview() {
    RefuelStatsCard(
        totalCost = BigDecimal("10.00"),
        averageCost = null,
        totalLiters = BigDecimal("123.45"),
        totalMileage = null,
        fuelConsumption = null,
        fuelCost = null
    )
}

