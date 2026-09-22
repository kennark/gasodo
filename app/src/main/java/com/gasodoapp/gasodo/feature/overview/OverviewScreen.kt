package com.gasodoapp.gasodo.feature.overview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import com.gasodoapp.gasodo.core.database.junctions.InspectionEventWithParts
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import com.gasodoapp.gasodo.core.utils.toDisplayString
import com.gasodoapp.gasodo.ui.components.NoDataCard
import com.gasodoapp.gasodo.ui.components.TopBarScaffold
import com.gasodoapp.gasodo.ui.icons.build
import com.gasodoapp.gasodo.ui.icons.content_paste_search
import com.gasodoapp.gasodo.ui.icons.error
import com.gasodoapp.gasodo.ui.icons.local_gas_station
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

    val totalRefuelCost by viewModel.totalRefuelCost.collectAsState()
    val averageCost by viewModel.averagePricePerLiter.collectAsState()
    val totalLiters by viewModel.totalLiters.collectAsState()
    val totalMileage by viewModel.totalMileage.collectAsState()
    val fuelConsumption by viewModel.fuelConsumption.collectAsState()
    val fuelCost by viewModel.fuelCost.collectAsState()

    val maintenanceData by viewModel.maintenanceData.collectAsState()
    val totalMaintenanceCost by viewModel.totalMaintenanceCost.collectAsState()
    val maintenanceActions by viewModel.maintenanceActions.collectAsState()
    val topMaintenanceActions by viewModel.topMaintenanceActions.collectAsState()

    val inspectionData by viewModel.inspectionData.collectAsState()
    val inspectionCount by viewModel.inspectionCount.collectAsState()
    val passCount by viewModel.passCount.collectAsState()
    val failCount by viewModel.failCount.collectAsState()
    val conditionalPassCount by viewModel.conditionalPassCount.collectAsState()
    val passPercentage by viewModel.passPercentage.collectAsState()
    val failPercentage by viewModel.failPercentage.collectAsState()
    val conditionalPassPercentage by viewModel.conditionalPassPercentage.collectAsState()

    TopBarScaffold(
        title = "Overview"
    ) { paddingValues ->
        MainContent(
            Modifier.padding(paddingValues),
            viewModel.dateRangePickerState,
            viewModel::onDateSelected,
            isLoading,
            refuelData,
            totalRefuelCost,
            averageCost,
            totalLiters,
            totalMileage,
            fuelConsumption,
            fuelCost,
            maintenanceData,
            totalMaintenanceCost,
            maintenanceActions,
            topMaintenanceActions,
            inspectionData,
            inspectionCount,
            passCount,
            failCount,
            conditionalPassCount,
            passPercentage,
            failPercentage,
            conditionalPassPercentage
        )
    }
}

@Composable
private fun MainContent(
    modifier: Modifier = Modifier,
    dateRangePickerState: DateRangePickerState,
    onDateSelected: () -> Unit,
    isLoading: Boolean,
    refuelData: List<RefuelEvent>,
    totalRefuelCost: BigDecimal,
    averageCost: BigDecimal?,
    totalLiters: BigDecimal,
    totalMileage: Long?,
    fuelConsumption: BigDecimal?,
    fuelCost: BigDecimal?,
    maintenanceData: List<MaintenanceEventWithServices>,
    totalMaintenanceCost: BigDecimal,
    maintenanceActions: List<MaintenanceServiceType>,
    topMaintenanceActions: List<TopMaintenanceAction>,
    inspectionData: List<InspectionEventWithParts>,
    inspectionCount: Int,
    passCount: Int,
    failCount: Int,
    conditionalPassCount: Int,
    passPercentage: BigDecimal,
    failPercentage: BigDecimal,
    conditionalPassPercentage: BigDecimal
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Year and Month Selector Card
        YearMonthSelectorCard(
            dateRangePickerState = dateRangePickerState,
            onDateSelected = onDateSelected
        )
        HorizontalDivider()

        if (dateRangePickerState.selectedEndDateMillis != null) {
            // Loading State
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp), color = MaterialTheme.colorScheme.primary
                )
            } else {
                if (refuelData.isNotEmpty())
                    RefuelStatsCard(
                        totalCost = totalRefuelCost,
                        averageCost = averageCost,
                        totalLiters = totalLiters,
                        totalMileage = totalMileage,
                        fuelConsumption = fuelConsumption,
                        fuelCost = fuelCost
                    )
                else
                    NoStatsCard(local_gas_station, "refuel")

                if (maintenanceData.isNotEmpty())
                    MaintenanceStatsCard(
                        totalCost = totalMaintenanceCost,
                        maintenanceActions = maintenanceActions,
                        topMaintenanceActions = topMaintenanceActions
                    )
                else
                    NoStatsCard(build, "maintenance")

                if (inspectionData.isNotEmpty())
                    InspectionStatsCard(
                        inspectionCount = inspectionCount,
                        passCount = passCount,
                        failCount = failCount,
                        conditionalPassCount = conditionalPassCount,
                        passPercentage = passPercentage,
                        failPercentage = failPercentage,
                        conditionalPassPercentage = conditionalPassPercentage
                    )
                else
                    NoStatsCard(content_paste_search, "inspection")
            }
        } else {
            NoDataCard(
                "Time period not set",
                "Select an end date to see statistics"
            )
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


@Composable
fun NoStatsCard(icon: ImageVector, type: String) {
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
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No ${type}s found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Select a different period to see your $type history",
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
    StatisticsCardFrame(
        "Refuel Statistics",
        local_gas_station
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LabelAndValueSpacing {
                    LabelText("Total Refuel Cost")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ValueText(totalCost.toDisplayString(2))
                        ValueText("€")
                    }
                }
                LabelAndValueSpacing {
                    LabelText("Total Refuelled")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ValueText(totalLiters.toDisplayString(2))
                        ValueText("L")
                    }
                }
                LabelAndValueSpacing {
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
                LabelAndValueSpacing {
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

                LabelAndValueSpacing {
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
                LabelAndValueSpacing {
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


@Composable
fun MaintenanceStatsCard(
    totalCost: BigDecimal,
    maintenanceActions: List<MaintenanceServiceType>,
    topMaintenanceActions: List<TopMaintenanceAction>
) {

    StatisticsCardFrame(
        "Maintenance Statistics",
        build
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LabelAndValueSpacing {
                    LabelText("Total Maintenance Cost")
                    ValueText("${totalCost.toDisplayString(2)} €")
                }

                LabelAndValueSpacing {
                    LabelText("Maintenance Action Count")
                    if (maintenanceActions.isNotEmpty())
                        ValueText("${maintenanceActions.size} actions")
                    else
                        NoDataText()
                }
            }
            if (topMaintenanceActions.isNotEmpty()) {
                VerticalDivider()
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    LabelText("Top Maintenance Actions")
                    for (action in topMaintenanceActions) {
                        ValueText("${action.count}x • ${action.serviceType.serviceName}")
                    }
                }
            }
        }
    }
}

@Composable
fun InspectionStatsCard(
    inspectionCount: Int,
    passCount: Int,
    failCount: Int,
    conditionalPassCount: Int,
    passPercentage: BigDecimal,
    failPercentage: BigDecimal,
    conditionalPassPercentage: BigDecimal
) {
    StatisticsCardFrame("Inspection Statistics", content_paste_search) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InspectionDonutChart(
                totalCount = inspectionCount,
                passCount = passCount,
                failCount = failCount,
                conditionalPassCount = conditionalPassCount
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LabelAndValueSpacing {
                    LabelText("Total Inspections")
                    ValueText("$inspectionCount inspections")
                }
                if (passCount > 0)
                    InspectionLegendRow(
                        label = "Pass",
                        count = passCount,
                        percentage = passPercentage,
                        color = MaterialTheme.colorScheme.primary
                    )
                if (failCount > 0)
                    InspectionLegendRow(
                        label = "Fail",
                        count = failCount,
                        percentage = failPercentage,
                        color = MaterialTheme.colorScheme.error
                    )
                if (conditionalPassCount > 0)
                    InspectionLegendRow(
                        label = "Conditional Pass",
                        count = conditionalPassCount,
                        percentage = conditionalPassPercentage,
                        color = MaterialTheme.colorScheme.tertiary
                    )
            }
        }
    }
}

@Composable
private fun InspectionDonutChart(
    totalCount: Int,
    passCount: Int,
    failCount: Int,
    conditionalPassCount: Int
) {
    val total = totalCount.coerceAtLeast(1).toFloat()
    val passFraction = passCount / total
    val failFraction = failCount / total
    val conditionalPassFraction = conditionalPassCount / total
    val remainingFraction = (total - passCount - failCount - conditionalPassCount) / total

    val passColor = MaterialTheme.colorScheme.primary
    val failColor = MaterialTheme.colorScheme.error
    val conditionalPassColor = MaterialTheme.colorScheme.tertiary
    val remainingColor = MaterialTheme.colorScheme.outlineVariant

    val strokeWidth = 10.dp
    val diameter = 84.dp

    Canvas(
        modifier = Modifier
            .size(diameter)
            .padding(strokeWidth / 2)
    ) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
        val startAngle = -90f
        val passSweep = passFraction * 360f
        val failSweep = failFraction * 360f
        val conditionalPassSweep = conditionalPassFraction * 360f
        val remainingSweep = remainingFraction * 360f

        // Pass
        drawArc(
            color = passColor,
            startAngle = startAngle,
            sweepAngle = passSweep,
            useCenter = false,
            style = stroke
        )
        // Fail
        drawArc(
            color = failColor,
            startAngle = startAngle + passSweep,
            sweepAngle = failSweep,
            useCenter = false,
            style = stroke
        )
        // Conditional Pass
        drawArc(
            color = conditionalPassColor,
            startAngle = startAngle + passSweep + failSweep,
            sweepAngle = conditionalPassSweep,
            useCenter = false,
            style = stroke
        )
        // No status
        drawArc(
            color = remainingColor,
            startAngle = startAngle + passSweep + failSweep + conditionalPassSweep,
            sweepAngle = remainingSweep,
            useCenter = false,
            style = stroke
        )
    }
}

@Composable
private fun InspectionLegendRow(
    label: String,
    count: Int,
    percentage: BigDecimal,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = CircleShape)
        )
        Text(
            text = "$label ${percentage.toDisplayString(0)}% • $count",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StatisticsCardFrame(
    title: String,
    icon: ImageVector,
    content: @Composable (() -> Unit)
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
                        imageVector = icon,
                        contentDescription = icon.name
                    )
                }
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            content()
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

@Composable
fun LabelAndValueSpacing(
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        content()
    }
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

