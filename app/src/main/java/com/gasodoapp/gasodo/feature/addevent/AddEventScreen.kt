package com.gasodoapp.gasodo.feature.addevent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.byValue
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.getSelectedDate
import androidx.compose.runtime.Composable
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
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.enums.EventType
import com.gasodoapp.gasodo.core.enums.InspectionStatus
import com.gasodoapp.gasodo.core.enums.PaymentMethod
import com.gasodoapp.gasodo.core.utils.BigDecimalUtils
import com.gasodoapp.gasodo.core.utils.toDisplayString
import com.gasodoapp.gasodo.feature.navigation.TopBarScaffold
import com.gasodoapp.gasodo.ui.icons.arrow_back
import com.gasodoapp.gasodo.ui.icons.check
import com.gasodoapp.gasodo.ui.icons.error
import java.math.BigDecimal
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@Composable
fun AddEventScreen(
    onDismiss: () -> Unit,
    viewModel: AddEventScreenViewModel = hiltViewModel<AddEventScreenViewModel>()
) {

    val baseState by viewModel.baseUiState.collectAsStateWithLifecycle()
    val refuelState by viewModel.refuelUiState.collectAsStateWithLifecycle()
    val inspectionState by viewModel.inspectionUiState.collectAsStateWithLifecycle()
    val maintenanceState by viewModel.maintenanceUiState.collectAsStateWithLifecycle()
    val showConfirmation by viewModel.showConfirmation.collectAsStateWithLifecycle()
    val hasError by viewModel.hasError.collectAsStateWithLifecycle()
    val formType = viewModel.type

    AnimatedVisibility(
        visible = showConfirmation,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = fadeOut(animationSpec = tween(durationMillis = 300)),
        modifier = Modifier.zIndex(1f)
    ) {
        AddEventConfirmationOverlay(baseState.type.toString(), onDismiss)
    }
    Surface(modifier = Modifier.fillMaxSize()) {
        TopBarScaffold(
            "New Event",
            subtitle = formType.toString(),
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = arrow_back, contentDescription = arrow_back.name)
                }
            },
            actions = {
                Row(
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    FilledIconButton(
                        modifier = Modifier.size(
                            IconButtonDefaults.smallContainerSize(
                                IconButtonDefaults.IconButtonWidthOption.Wide
                            )
                        ),
                        onClick = viewModel::onSubmit
                    ) {
                        Icon(imageVector = check, contentDescription = check.name)
                    }
                }
            }
        ) { innerPadding ->
            FormContent(
                innerPadding,
                baseState,
                viewModel,
                refuelState,
                maintenanceState,
                inspectionState,
                hasError
            )
        }
    }

}

@Composable
private fun FormContent(
    paddingValues: PaddingValues,
    baseState: AddEventTypeFormState,
    viewModel: AddEventScreenViewModel,
    refuelState: RefuelEventFormState,
    maintenanceState: MaintenanceEventFormState,
    inspectionState: InspectionEventFormState,
    hasError: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BaseForm(
            state = baseState,
            mileageState = viewModel.mileageField,
            onLocationChange = viewModel::onLocationChange,
            notesState = viewModel.notesField,
            datePickerState = viewModel.datePickerState
        )

        when (baseState.type) {
            EventType.REFUEL -> {
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

            EventType.MAINTENANCE -> {
                MaintenanceForm(
                    state = maintenanceState,
                    providerState = viewModel.providerNameField
                )
            }

            EventType.INSPECTION -> {
                InspectionForm(
                    state = inspectionState,
                    onStatusChange = viewModel::onStatusChange
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp, alignment = Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasError) {
                Icon(
                    imageVector = error,
                    contentDescription = error.name,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Validation error",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = 2.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseForm(
    state: AddEventTypeFormState,
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
        // Mileage Field
        OutlinedTextField(
            state = mileageState,
            label = { Text("Mileage") },
            lineLimits = TextFieldLineLimits.SingleLine,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Text("km") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            inputTransformation = InputTransformation.byValue { current, proposed ->
                if ("""\D""".toRegex() in proposed) current else proposed
            }

        )

        // Date Picker
        var showModal by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = datePickerState.getSelectedDate()?.format(
                DateTimeFormatter.ofLocalizedDate(
                    FormatStyle.MEDIUM
                )
            ) ?: "",
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
            label = { Text("Amount") },
            lineLimits = TextFieldLineLimits.SingleLine,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Text("L") },
            inputTransformation = InputTransformation.byValue { current, proposed ->
                if ("""[^0-9,.]""".toRegex() in proposed) current else proposed
            }
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
                    trailingIcon = { Text("€/L") },
                    inputTransformation = InputTransformation.byValue { current, proposed ->
                        if ("""[^0-9,.]""".toRegex() in proposed) current else proposed
                    }
                )

                // Display calculated total cost (read-only)
                OutlinedTextField(
                    value = amountState.text.toString().toBigDecimalOrNull()?.multiply(
                        pricePerLiterState.text.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
                    )?.toDisplayString(2)
                        ?: "0.00",
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
                    trailingIcon = { Text("€") },
                    inputTransformation = InputTransformation.byValue { current, proposed ->
                        if ("""[^0-9,.]""".toRegex() in proposed) current else proposed
                    }
                )

                // Display calculated price per liter (read-only)
                OutlinedTextField(
                    value = costState.text.toString().toBigDecimalOrNull()?.divide(
                        amountState.text.toString().toBigDecimalOrNull() ?: BigDecimal.ONE,
                        BigDecimalUtils.SCALE, BigDecimalUtils.ROUNDING_MODE
                    )?.toDisplayString(3)
                        ?: "0.000",
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
