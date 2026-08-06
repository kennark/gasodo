package com.example.carrefueltracker.feature.addevent

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carrefueltracker.core.database.BaseColumns
import com.example.carrefueltracker.core.database.entity.RefuelEvent
import com.example.carrefueltracker.core.database.entity.SavedLocation
import com.example.carrefueltracker.core.database.repository.InspectionRepository
import com.example.carrefueltracker.core.database.repository.MaintenanceRepository
import com.example.carrefueltracker.core.database.repository.RefuelRepository
import com.example.carrefueltracker.core.enums.EventType
import com.example.carrefueltracker.core.enums.InspectionStatus
import com.example.carrefueltracker.core.enums.PaymentMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AddEventScreenViewModel @Inject constructor(
    private val refuelRepository: RefuelRepository,
    private val inspectionRepository: InspectionRepository,
    private val maintenanceRepository: MaintenanceRepository
) : ViewModel() {

    private val _showConfirmation = MutableStateFlow(false)
    private val _baseUiState = MutableStateFlow(AddEventTypeFormState())
    private val _refuelUiState = MutableStateFlow(RefuelEventFormState())
    private val _maintenanceUiState = MutableStateFlow(MaintenanceEventFormState())
    private val _inspectionUiState = MutableStateFlow(InspectionEventFormState())

    val showConfirmation: StateFlow<Boolean> = _showConfirmation.asStateFlow()
    val baseUiState: StateFlow<AddEventTypeFormState> = _baseUiState.asStateFlow()
    val refuelUiState: StateFlow<RefuelEventFormState> = _refuelUiState.asStateFlow()
    val maintenanceUiState: StateFlow<MaintenanceEventFormState> = _maintenanceUiState.asStateFlow()
    val inspectionUiState: StateFlow<InspectionEventFormState> = _inspectionUiState.asStateFlow()

    // TextFieldStates for user-editable text fields (state-based API)
    val mileageField = TextFieldState()
    val notesField = TextFieldState()
    val amountTextField = TextFieldState()
    val costTextField = TextFieldState()
    val pricePerLiterTextField = TextFieldState()
    val providerNameField = TextFieldState()

    @OptIn(ExperimentalMaterial3Api::class)
    val datePickerState = DatePickerState(
        locale = CalendarLocale.getDefault(),
        initialSelectedDate = LocalDate.now(),
    )

    // onChange functions for all other types of edits (boolean switch, date, etc.)
    fun onFormTypeChange(value: EventType) {
        _baseUiState.update { it.copy(type = value) }
    }

    fun onLocationChange(value: SavedLocation) {
        _baseUiState.update { it.copy(location = value) }
    }

    fun onCalculateCostChange(value: Boolean) {
        _refuelUiState.update { it.copy(calculateCost = value) }
    }

    fun onPaymentMethodChange(value: PaymentMethod) {
        _refuelUiState.update { it.copy(paymentMethod = value) }
    }

    fun onFullFillUpChange(value: Boolean) {
        _refuelUiState.update { it.copy(fullFillUp = value) }
    }

    fun onStatusChange(value: InspectionStatus) {
        _inspectionUiState.update { it.copy(status = value) }
    }

    /**
     * Validate all logic and if everything passes, automatically save.
     */
    fun onSubmit() {
        val baseState = _baseUiState.value
        // Extract text values from TextFieldStates for saving
        val mileageValue = mileageField.text.toString().trim().toLongOrNull()
        val notesValue = notesField.text.toString().ifEmpty { baseState.notes }
        val dateValue = datePickerState.selectedDateMillis

        val updatedBaseState = baseState.copy(
            mileage = mileageValue,
            notes = notesValue,
            date = dateValue
        )

        if (validateBaseValues(updatedBaseState)) {

            if (updatedBaseState.type == EventType.REFUEL) {
                val state = _refuelUiState.value
                // Extract refuel text values from TextFieldStates
                val amountValue = amountTextField.text.toString().toDoubleOrNull()
                var costValue: Double?
                var pricePerLiterValue: Double?

                if (state.calculateCost) {
                    pricePerLiterValue = pricePerLiterTextField.text.toString().toDoubleOrNull()
                    costValue = amountValue?.times(pricePerLiterValue ?: 0.00)
                } else {
                    costValue = costTextField.text.toString().toDoubleOrNull()
                    pricePerLiterValue = amountValue?.div(costValue ?: 0.00) // todo: 0.00 division
                }

                val updatedRefuelState = state.copy(
                    amount = amountValue,
                    cost = costValue,
                    pricePerLiter = pricePerLiterValue
                )
                if (validateRefuelValues(updatedRefuelState)) {
                    storeRefuelEvent(updatedRefuelState, updatedBaseState)
                }
            } else if (updatedBaseState.type == EventType.INSPECTION) {
                val state = _inspectionUiState.value
                if (validateInspectionValues(state)) {
                    storeInspectionEvent(state, updatedBaseState)
                }
            } else if (updatedBaseState.type == EventType.MAINTENANCE) {
                val state = _maintenanceUiState.value
                val providerNameValue =
                    providerNameField.text.toString().ifEmpty { state.providerName }
                val updatedMaintenanceState = state.copy(
                    providerName = providerNameValue
                )
                if (validateMaintenanceValues(updatedMaintenanceState)) {
                    storeMaintenanceEvent(updatedMaintenanceState, updatedBaseState)
                }
            }
        }
    }

    private fun validateBaseValues(baseState: AddEventTypeFormState): Boolean {
        // Example: Validate, that event date and mileage are in order, so future dates don't have a smaller mileage than previous events
        return true
    }

    private fun validateRefuelValues(refuelState: RefuelEventFormState): Boolean {
        return true
    }

    private fun validateInspectionValues(inspectionState: InspectionEventFormState): Boolean {
        return true
    }

    private fun validateMaintenanceValues(maintenanceState: MaintenanceEventFormState): Boolean {
        return true
    }

    private fun storeRefuelEvent(
        refuelState: RefuelEventFormState,
        baseState: AddEventTypeFormState
    ) {
        val baseColumns = BaseColumns(
            date = baseState.date,
            mileage = baseState.mileage,
            savedLocationId = baseState.location?.id,
            notes = baseState.notes

        )
//        var pricePerLiter: Double? = null
//        var totalCost: Double? = null
//        if (refuelState.calculateCost && refuelState.pricePerLiter != null) {
//            pricePerLiter = refuelState.pricePerLiter
//            totalCost = refuelState.amount?.times(pricePerLiter)
//        } else if (refuelState.amount != null) {
//            totalCost = refuelState.cost
//            pricePerLiter = totalCost?.div(refuelState.amount)
//        }
        val event = RefuelEvent(
            base = baseColumns,
            amountLiters = refuelState.amount,
            pricePerLiter = refuelState.pricePerLiter,
            totalCost = refuelState.cost,
            paymentMethod = refuelState.paymentMethod,
            fullFillUp = refuelState.fullFillUp,
        )
        viewModelScope.launch {
            refuelRepository.insert(event)

            showConfirmation()
        }
    }

    private fun storeInspectionEvent(
        inspectionState: InspectionEventFormState,
        baseState: AddEventTypeFormState
    ) {
        // TODO: Implement inspection event storage
    }

    private fun storeMaintenanceEvent(
        maintenanceState: MaintenanceEventFormState,
        baseState: AddEventTypeFormState
    ) {
        // TODO: Implement maintenance event storage
    }

    private fun showConfirmation() {
        _showConfirmation.value = true
    }
}


data class AddEventTypeFormState(
    val type: EventType = EventType.REFUEL,
    val mileage: Long? = null,
    val date: Long? = LocalDate.now().toEpochDay(),
    val location: SavedLocation? = null,
    val notes: String = ""
)

data class RefuelEventFormState(
    val amount: Double? = null,
    // Cost if false, price per liter if true
    val calculateCost: Boolean = false,
    val cost: Double? = null,
    val pricePerLiter: Double? = null,
    val paymentMethod: PaymentMethod? = null,
    val fullFillUp: Boolean = true
)

data class MaintenanceEventFormState(
    val providerName: String = ""
)

data class InspectionEventFormState(
    val status: InspectionStatus? = null
)
