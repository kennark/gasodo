package com.example.carrefueltracker.feature.addevent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.getSelectedDate
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carrefueltracker.core.database.entity.SavedLocation
import com.example.carrefueltracker.core.enums.EventType
import com.example.carrefueltracker.core.enums.InspectionStatus
import com.example.carrefueltracker.core.enums.PaymentMethod


@Composable
fun AddEventScreen(
    onNavigationBack: () -> Unit,
    viewModel: AddEventScreenViewModel = hiltViewModel<AddEventScreenViewModel>()
) {
    val baseState by viewModel.baseUiState.collectAsStateWithLifecycle()
    val refuelState by viewModel.refuelUiState.collectAsStateWithLifecycle()
    val inspectionState by viewModel.inspectionUiState.collectAsStateWithLifecycle()
    val maintenanceState by viewModel.maintenanceUiState.collectAsStateWithLifecycle()
    val showConfirmation by viewModel.showConfirmation.collectAsState()


    AnimatedVisibility(
        visible = showConfirmation,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = fadeOut(animationSpec = tween(durationMillis = 300)),
        modifier = Modifier.zIndex(1f)
    ) {
        AddEventConfirmationOverlay(baseState.type.toString(), onNavigationBack)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BaseForm(
                state = baseState,
                onTypeChange = viewModel::onFormTypeChange,
                mileageState = viewModel.mileageField,
                onLocationChange = viewModel::onLocationChange,
                notesState = viewModel.notesField,
                datePickerState = viewModel.datePickerState
            )
        }

        when (baseState.type) {
            EventType.REFUEL -> {
                item {
                    RefuelForm(
                        state = refuelState,
                        amountState = viewModel.amountTextField,
                        onCalculateCostChange = viewModel::onCalculateCostChange,
                        costState = viewModel.costTextField,
                        pricePerLiterState = viewModel.pricePerLiterTextField,
                        onPaymentMethodChange = viewModel::onPaymentMethodChange,
                        onFullFillUpChange = viewModel::onFullFillUpChange
                    )
                }
            }

            EventType.MAINTENANCE -> {
                item {
                    MaintenanceForm(
                        state = maintenanceState,
                        providerState = viewModel.providerNameField
                    )
                }
            }

            EventType.INSPECTION -> {
                item {
                    InspectionForm(
                        state = inspectionState,
                        onStatusChange = viewModel::onStatusChange
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = viewModel::onSubmit,
                ) {
                    Text("Save Event")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseForm(
    state: AddEventTypeFormState,
    onTypeChange: (EventType) -> Unit,
    mileageState: TextFieldState,
    onLocationChange: (SavedLocation) -> Unit,
    notesState: TextFieldState,
    datePickerState: DatePickerState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Event Type Selector
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                value = state.type.displayName,
                onValueChange = {},
                label = { Text("Event Type") },
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            // Example https://developer.android.com/reference/kotlin/androidx/compose/material3/ExposedDropdownMenuBox.composable
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                EventType.entries.forEach { eventType ->
                    DropdownMenuItem(
                        text = { Text(eventType.toString()) },
                        onClick = {
                            onTypeChange(eventType)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Mileage Field
        OutlinedTextField(
            state = mileageState,
            label = { Text("Mileage") },
            lineLimits = TextFieldLineLimits.SingleLine,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Text("km") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Date Picker
        var showModal by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = datePickerState.getSelectedDate().toString(), // todo display format
            onValueChange = {},
            label = { Text("Date") },
            readOnly = true,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(datePickerState) {
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
                        showModal = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showModal = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Location Field (placeholder - needs location picker)
        OutlinedTextField(
            value = state.location?.name ?: "",
            onValueChange = {},
            label = { Text("Location") },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Notes Field
        OutlinedTextField(
            state = notesState,
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefuelForm(
    state: RefuelEventFormState,
    amountState: TextFieldState,
    onCalculateCostChange: (Boolean) -> Unit,
    costState: TextFieldState,
    pricePerLiterState: TextFieldState,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onFullFillUpChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Refuel Details",
            style = MaterialTheme.typography.titleMedium
        )

        // Amount Field (Liters)
        OutlinedTextField(
            state = amountState,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Amount (Liters)") },
            lineLimits = TextFieldLineLimits.SingleLine,
            modifier = Modifier.fillMaxWidth(),
        )

        // Full Fill-Up Toggle
        RowOfTextAndSwitch(
            text = "Full Fill-Up",
            checked = state.fullFillUp,
            onCheckedChange = onFullFillUpChange
        )

        // Cost Calculation Mode Toggle
        RowOfTextAndSwitch(
            text = "Calculate from Price/Liter",
            checked = state.calculateCost,
            onCheckedChange = onCalculateCostChange
        )

        if (state.calculateCost) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Price per Liter Field
                OutlinedTextField(
                    state = pricePerLiterState,
                    label = { Text("Price per Liter") },
                    lineLimits = TextFieldLineLimits.SingleLine,
                    modifier = Modifier.weight(2f),
                )

                // Display calculated total cost (read-only)
                OutlinedTextField(
                    value = amountState.text.toString().toDoubleOrNull()?.times(
                        pricePerLiterState.text.toString().toDoubleOrNull() ?: 0.0
                    )?.toString()
                        ?: "0.0",
                    onValueChange = {},
                    label = { Text("Total Cost") },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Cost Field (manual entry)
                OutlinedTextField(
                    state = costState,
                    label = { Text("Total Cost") },
                    lineLimits = TextFieldLineLimits.SingleLine,
                    modifier = Modifier.weight(2f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                // Display calculated price per liter (read-only)
                OutlinedTextField(
                    value = costState.text.toString().toDoubleOrNull()?.div(
                        amountState.text.toString().toDoubleOrNull() ?: 1.0
                    )?.toString()
                        ?: "0.0",
                    onValueChange = {},
                    label = { Text("Price per Liter") },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Payment Method Selector
        var paymentExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = paymentExpanded,
            onExpandedChange = { paymentExpanded = !paymentExpanded }) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                value = state.paymentMethod?.displayName ?: "",
                onValueChange = {},
                label = { Text("Payment Method") },
                readOnly = true,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentExpanded) }
            )
            ExposedDropdownMenu(
                expanded = paymentExpanded,
                onDismissRequest = { paymentExpanded = false }
            ) {
                PaymentMethod.entries.forEach { method ->
                    DropdownMenuItem(
                        text = { Text(method.toString()) },
                        onClick = {
                            onPaymentMethodChange(method)
                            paymentExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InspectionForm(
    state: InspectionEventFormState,
    onStatusChange: (InspectionStatus) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Inspection Details",
            style = MaterialTheme.typography.titleMedium
        )

        // Status Selector (Radio buttons)
        state.status?.let { currentStatus ->
            RadioGroup(
                options = InspectionStatus.entries,
                selectedOption = currentStatus,
                onSelectionChanged = onStatusChange
            )
        } ?: run {
            RadioGroup(
                options = InspectionStatus.entries,
                selectedOption = null,
                onSelectionChanged = onStatusChange
            )
        }
    }
}

@Composable
fun MaintenanceForm(
    state: MaintenanceEventFormState,
    providerState: TextFieldState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Maintenance Details",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            state = providerState,
            label = { Text("Service Provider") },
            lineLimits = TextFieldLineLimits.SingleLine,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun RowOfTextAndSwitch(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun <T> RadioGroup(
    options: List<T>,
    selectedOption: T?,
    onSelectionChanged: (T) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedOption == option,
                    onClick = { onSelectionChanged(option) }
                )
                Text(option.toString())
            }
        }
    }
}
