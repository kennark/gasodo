package com.example.carrefueltracker.feature.overview

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.carrefueltracker.ui.icons.local_gas_station

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    viewModel: OverviewScreenViewModel = hiltViewModel<OverviewScreenViewModel>()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val refuelData by viewModel.refuelData.collectAsState()

    val totalCost by viewModel.totalCost.collectAsState()
    val totalLiters by viewModel.totalLiters.collectAsState()
    val showMileageStatistics by viewModel.mileageStatisticsCanBeCalculated.collectAsState()
    val totalMileage by viewModel.totalMileage.collectAsState()
    val fuelConsumption by viewModel.fuelConsumption.collectAsState()
    val fuelCost by viewModel.fuelCost.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Year and Month Selector Card
        YearMonthSelectorCard(
            dateRangePickerState = viewModel.dateRangePickerState,
            onDateSelected = viewModel::onDateSelected
        )

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
                totalLiters = totalLiters,
                showMileageStatistics = showMileageStatistics,
                totalMileage = totalMileage,
                fuelConsumption = fuelConsumption,
                fuelCost = fuelCost
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearMonthSelectorCard(
    dateRangePickerState: DateRangePickerState, onDateSelected: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ), shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            var showModal by remember { mutableStateOf(false) }
            TextField(
                value = dateRangePickerState.getSelectedStartDate()
                    .toString() + " - " + dateRangePickerState.getSelectedEndDate().toString(),
                onValueChange = {},
                label = { Text("Date range") },
                readOnly = true,
                singleLine = true,
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
                    DateRangePicker(state = dateRangePickerState)
                }
            }
        }
    }
}

@Composable
fun EmptyRefuelState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp), colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            ), shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Select a different month to see your refuel history",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun RefuelStatsCard(
    totalCost: Double,
    totalLiters: Double,
    showMileageStatistics: Boolean,
    totalMileage: Long,
    fuelConsumption: Double,
    fuelCost: Double

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.padding(2.dp)) {
                    Icon(
                        imageVector = local_gas_station,
                        contentDescription = local_gas_station.name
                    )
                }
                Text(
                    text = "Refuel Statistics",
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Total Refuel Cost", style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = totalCost.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Total Refuelled", style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = totalLiters.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (showMileageStatistics) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Total Mileage Travelled",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = totalMileage.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Fuel Consumption", style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = fuelConsumption.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Fuel cost per 100", style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = fuelCost.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
