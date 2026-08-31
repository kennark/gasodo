package com.gasodoapp.gasodo.feature.addevent

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.byValue
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.getSelectedDate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
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
    onSnackbarMessage: () -> Unit,
    viewModel: AddEventScreenViewModel = hiltViewModel<AddEventScreenViewModel>()
) {

    val baseState by viewModel.baseUiState.collectAsStateWithLifecycle()
    val refuelState by viewModel.refuelUiState.collectAsStateWithLifecycle()
    val inspectionState by viewModel.inspectionUiState.collectAsStateWithLifecycle()
    val maintenanceState by viewModel.maintenanceUiState.collectAsStateWithLifecycle()
    val dismissDialog by viewModel.dismissDialog.collectAsStateWithLifecycle()
    val hasError by viewModel.hasError.collectAsStateWithLifecycle()
    val formType = viewModel.type
    val locations by viewModel.locations.collectAsStateWithLifecycle(initialValue = emptyList())

    val view = LocalView.current
    val darkIcons = !isSystemInDarkTheme()

    // Fix white status bar icons on white mode dialog
    SideEffect {
        val dialogWindow = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect

        // Match icon appearance
        val controller = WindowCompat.getInsetsController(dialogWindow, view)
        controller.isAppearanceLightStatusBars = darkIcons
        controller.isAppearanceLightNavigationBars = darkIcons
    }

    LaunchedEffect(dismissDialog) {
        if (dismissDialog) {
            onSnackbarMessage()
            onDismiss()
        }
    }

    TopBarScaffold(
        if (viewModel.id == null) "New Event" else "Edit Event",
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
            locations,
            hasError
        )
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
    locations: List<SavedLocation>,
    hasError: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BaseForm(
            state = baseState,
            mileageState = viewModel.mileageField,
            onLocationChange = viewModel::onLocationChange,
            notesState = viewModel.notesField,
            datePickerState = viewModel.datePickerState,
            locations = locations
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
    datePickerState: DatePickerState,
    locations: List<SavedLocation>,
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
        var showDateModal by remember { mutableStateOf(false) }
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
                            showDateModal = true
                        }
                    }
                },
        )
        if (showDateModal) {
            DatePickerDialog(
                onDismissRequest = { showDateModal = false },
                confirmButton = {
                    TextButton(onClick = {
                        showDateModal = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDateModal = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Location Field
        var showLocationDialog by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = state.location?.name ?: "",
            onValueChange = {},
            label = { Text("Location") },
            readOnly = true,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(state.location?.name) {
                    awaitEachGesture {
                        // Modifier.clickable doesn't work for text fields, so we use Modifier.pointerInput
                        // in the Initial pass to observe events before the text field consumes them
                        // in the Main pass.
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        if (upEvent != null) {
                            showLocationDialog = true
                        }
                    }
                },
        )
        val selectedLocation = remember { mutableStateOf(state.location) }

        if (showLocationDialog) {
            LocationSelectDialog(
                locations = locations,
                onLocationChange = onLocationChange,
                onDismissDialog = {
                    showLocationDialog = false
                },
                selectedLocation = selectedLocation
            )
        }

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
                if (option is SavedLocation) {
                    Text(option.name)
                } else {
                    Text(option.toString())
                }
            }
        }
    }
}


@Composable
fun LocationSelectDialog(
    locations: List<SavedLocation>,
    onLocationChange: (SavedLocation) -> Unit,
    onDismissDialog: () -> Unit,
    selectedLocation: MutableState<SavedLocation?>
) {
    val searchTextFieldState = rememberTextFieldState()
    Dialog(
        onDismissRequest = {
            onDismissDialog()
        }
    ) {
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                Text(
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp),
                    text = "Select Location",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()

                Column(
                    Modifier
                        .height(240.dp)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedTextField(
                        label = { Text("Search or enter new name") },
                        state = searchTextFieldState,
                    )

                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLocation.value == null,
                            onClick = { selectedLocation.value = null }
                        )
                        Text("Create new")
                    }
                    locations.filter { location ->
                        location.name.contains(
                            searchTextFieldState.text.toString(),
                            ignoreCase = true
                        )
                    }.let { filteredLocations ->
                        if (filteredLocations.isEmpty())
                        // If user enters a new name, auto-select "Create new"
                            selectedLocation.value = null
                        RadioGroup<SavedLocation>(
                            options = filteredLocations,
                            selectedOption = selectedLocation.value,
                            onSelectionChanged = { option -> selectedLocation.value = option }
                        )
                    }


                }
                HorizontalDivider()

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onDismissDialog() }
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            selectedLocation.value?.let { onLocationChange(it) }
                            if (selectedLocation.value == null && searchTextFieldState.text.isNotEmpty()) {
                                onLocationChange(SavedLocation(name = searchTextFieldState.text.toString()))
                            }
                            onDismissDialog()
                        },
                        enabled = selectedLocation.value != null || searchTextFieldState.text.isNotEmpty()
                    ) {
                        Text("Select")
                    }
                }
            }
        }
    }
}