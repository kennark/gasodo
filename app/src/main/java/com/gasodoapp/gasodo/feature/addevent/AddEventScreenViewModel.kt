package com.gasodoapp.gasodo.feature.addevent

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gasodoapp.gasodo.core.database.BaseColumns
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.database.repository.EventRepository
import com.gasodoapp.gasodo.core.database.repository.InspectionRepository
import com.gasodoapp.gasodo.core.database.repository.MaintenanceRepository
import com.gasodoapp.gasodo.core.database.repository.RefuelRepository
import com.gasodoapp.gasodo.core.enums.EventType
import com.gasodoapp.gasodo.core.enums.InspectionStatus
import com.gasodoapp.gasodo.core.enums.PaymentMethod
import com.gasodoapp.gasodo.core.utils.BigDecimalUtils
import com.gasodoapp.gasodo.feature.navigation.ADD_EVENT_TYPE_ARG
import com.gasodoapp.gasodo.feature.navigation.EDIT_EVENT_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEventScreenViewModel @Inject constructor(
    private val refuelRepository: RefuelRepository,
    private val inspectionRepository: InspectionRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val type: EventType = checkNotNull(
        savedStateHandle.get<EventType>(ADD_EVENT_TYPE_ARG)
    )

    val id: UUID? = savedStateHandle.get<String?>(EDIT_EVENT_ID_ARG)?.let {
        UUID.fromString(it)
    }

    private val _dismissDialog = MutableStateFlow(false)
    private val _hasError = MutableStateFlow(false)
    private val _baseUiState = MutableStateFlow(AddEventTypeFormState(type = type))
    private val _refuelUiState = MutableStateFlow(RefuelEventFormState())
    private val _maintenanceUiState = MutableStateFlow(MaintenanceEventFormState())
    private val _inspectionUiState = MutableStateFlow(InspectionEventFormState())

    val dismissDialog: StateFlow<Boolean> = _dismissDialog.asStateFlow()
    val hasError: StateFlow<Boolean> = _hasError.asStateFlow()
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

    init {
        if (id != null) {
            viewModelScope.launch {
                if (type == EventType.REFUEL) {
                    val event: RefuelEvent? = refuelRepository.getById(id)

                    if (event != null) {
                        _baseUiState.value = AddEventTypeFormState(
                            type = EventType.REFUEL,
                            mileage = event.base.mileage,
                            date = event.base.date,
                            notes = event.base.notes
                        )
                        _refuelUiState.value = RefuelEventFormState(
                            amount = event.amountLiters,
                            cost = event.totalCost,
                            pricePerLiter = event.pricePerLiter,
                            paymentMethod = event.paymentMethod,
                            fullFillUp = event.fullFillUp
                        )

                        mileageField.setTextAndPlaceCursorAtEnd(
                            event.base.mileage?.toString() ?: ""
                        )
                        costTextField.setTextAndPlaceCursorAtEnd(event.totalCost?.toString() ?: "")
                        notesField.setTextAndPlaceCursorAtEnd(event.base.notes)
                        amountTextField.setTextAndPlaceCursorAtEnd(
                            event.amountLiters?.toString() ?: ""
                        )
                        pricePerLiterTextField.setTextAndPlaceCursorAtEnd(
                            event.pricePerLiter?.toString() ?: ""
                        )

                        datePickerState.selectedDateMillis = event.base.date
                    }
                }
            }
        }
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

        viewModelScope.launch {
            if (validateBaseValues(updatedBaseState)) {

                if (updatedBaseState.type == EventType.REFUEL) {

                    val updatedRefuelState = calculateRefuelEventData()

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
            } else {
                _hasError.value = true
            }
        }
    }

    internal fun calculateRefuelEventData(): RefuelEventFormState {
        val state = _refuelUiState.value
        // Extract refuel text values from TextFieldStates
        val amountValue = amountTextField.text.toString().toBigDecimalOrNull()
        var costValue: BigDecimal? = null
        var pricePerLiterValue: BigDecimal? = null

        if (state.calculateCost) {
            // Calculate total cost
            pricePerLiterValue =
                pricePerLiterTextField.text.toString().toBigDecimalOrNull()
            if (pricePerLiterValue != null)
                costValue = amountValue?.multiply(pricePerLiterValue)
        } else {
            // Calculate price per liter
            costValue =
                costTextField.text.toString().toBigDecimalOrNull()
            if (amountValue != null)
                pricePerLiterValue = costValue?.divide(
                    amountValue,
                    BigDecimalUtils.SCALE,
                    BigDecimalUtils.ROUNDING_MODE
                )
        }

        val updatedRefuelState = state.copy(
            amount = amountValue,
            cost = costValue,
            pricePerLiter = pricePerLiterValue
        )
        return updatedRefuelState
    }

    internal suspend fun validateBaseValues(baseState: AddEventTypeFormState): Boolean {

        // Validate, that there does not exist any event with higher mileage, but past date (give error on it)
        if (baseState.date != null && baseState.mileage != null) {
            val higherEvent = eventRepository.getDateWithHigherMileage(baseState.mileage)
            if (higherEvent != null) {
                if (higherEvent.date < baseState.date)
                    return false
            }
            val lowerEvent = eventRepository.getDateWithLowerMileage(baseState.mileage)
            if (lowerEvent != null) {
                if (lowerEvent.date > baseState.date)
                    return false
            }
        }

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

    internal suspend fun storeRefuelEvent(
        refuelState: RefuelEventFormState,
        baseState: AddEventTypeFormState
    ) {
        val baseColumns = BaseColumns(
            date = baseState.date,
            mileage = baseState.mileage,
            savedLocationId = baseState.location?.id,
            notes = baseState.notes

        )

        val event = RefuelEvent(
            base = baseColumns,
            amountLiters = refuelState.amount,
            pricePerLiter = refuelState.pricePerLiter,
            totalCost = refuelState.cost,
            paymentMethod = refuelState.paymentMethod,
            fullFillUp = refuelState.fullFillUp,
        )

        // Set id of existing event in case of edit
        if (id != null)
            event.id = id

        refuelRepository.upsert(event)

        dismissDialog()
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

    private fun dismissDialog() {
        _dismissDialog.value = true
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
    val amount: BigDecimal? = null,
    // Cost if false, price per liter if true
    val calculateCost: Boolean = false,
    val cost: BigDecimal? = null,
    val pricePerLiter: BigDecimal? = null,
    val paymentMethod: PaymentMethod? = null,
    val fullFillUp: Boolean = true
)

data class MaintenanceEventFormState(
    val providerName: String = ""
)

data class InspectionEventFormState(
    val status: InspectionStatus? = null
)
