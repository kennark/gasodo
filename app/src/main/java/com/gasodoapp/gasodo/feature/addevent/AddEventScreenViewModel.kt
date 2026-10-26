package com.gasodoapp.gasodo.feature.addevent

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.setSelectedDate
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gasodoapp.gasodo.core.database.BaseColumns
import com.gasodoapp.gasodo.core.database.entity.InspectablePart
import com.gasodoapp.gasodo.core.database.entity.InspectionEvent
import com.gasodoapp.gasodo.core.database.entity.MaintenanceEvent
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.database.junctions.InspectionEventWithParts
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import com.gasodoapp.gasodo.core.database.repository.EventRepository
import com.gasodoapp.gasodo.core.database.repository.InspectablePartRepository
import com.gasodoapp.gasodo.core.database.repository.InspectionRepository
import com.gasodoapp.gasodo.core.database.repository.MaintenanceRepository
import com.gasodoapp.gasodo.core.database.repository.MaintenanceServiceTypeRepository
import com.gasodoapp.gasodo.core.database.repository.RefuelRepository
import com.gasodoapp.gasodo.core.database.repository.SavedLocationRepository
import com.gasodoapp.gasodo.core.enums.EventType
import com.gasodoapp.gasodo.core.enums.InspectionStatus
import com.gasodoapp.gasodo.core.enums.PaymentMethod
import com.gasodoapp.gasodo.core.utils.BigDecimalUtils
import com.gasodoapp.gasodo.feature.navigation.ADD_EVENT_TYPE_ARG
import com.gasodoapp.gasodo.feature.navigation.EDIT_EVENT_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    private val inspectablePartRepository: InspectablePartRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val eventRepository: EventRepository,
    private val locationRepository: SavedLocationRepository,
    private val maintenanceServiceTypeRepository: MaintenanceServiceTypeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val type: EventType = checkNotNull(
        savedStateHandle.get<EventType>(ADD_EVENT_TYPE_ARG)
    )

    val id: UUID? = savedStateHandle.get<String?>(EDIT_EVENT_ID_ARG)?.let {
        UUID.fromString(it)
    }

    private val _dismissDialog = MutableStateFlow(false)
    private val _formErrors = MutableStateFlow(AddEventFormErrors())
    private val _baseUiState = MutableStateFlow(AddEventTypeFormState(type = type))
    private val _refuelUiState = MutableStateFlow(RefuelEventFormState())
    private val _maintenanceUiState = MutableStateFlow(MaintenanceEventFormState())
    private val _inspectionUiState = MutableStateFlow(InspectionEventFormState())

    val dismissDialog: StateFlow<Boolean> = _dismissDialog.asStateFlow()
    val formErrors: StateFlow<AddEventFormErrors> = _formErrors.asStateFlow()

    /**
     * True while at least one field is invalid. Kept as a derived flow so that existing
     * consumers (and tests) of a single error flag keep working.
     */
    val hasError: StateFlow<Boolean> =
        _formErrors
            .map { it.hasAnyError }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val baseUiState: StateFlow<AddEventTypeFormState> = _baseUiState.asStateFlow()
    val refuelUiState: StateFlow<RefuelEventFormState> = _refuelUiState.asStateFlow()
    val maintenanceUiState: StateFlow<MaintenanceEventFormState> = _maintenanceUiState.asStateFlow()
    val inspectionUiState: StateFlow<InspectionEventFormState> = _inspectionUiState.asStateFlow()

    val locations = locationRepository.getAll()
    private val _draftServiceTypes = MutableStateFlow<List<MaintenanceServiceType>>(emptyList())
    private val _dbServiceTypes = maintenanceServiceTypeRepository.getAll()
        .map { list -> list.sortedBy { it !in _maintenanceUiState.value.doneWork } }
    val serviceTypes =
        combine(_dbServiceTypes, _draftServiceTypes) { dbItems, drafts ->
            drafts + dbItems
        }

    private val _draftInspectableParts = MutableStateFlow<List<InspectablePart>>(emptyList())
    private val _dbInspectableParts = inspectablePartRepository.getAll()
        .map { list -> list.sortedBy { it !in _inspectionUiState.value.inspectedParts } }
    val inspectableParts =
        combine(_dbInspectableParts, _draftInspectableParts) { dbItems, drafts ->
            drafts + dbItems
        }

    // TextFieldStates for user-editable text fields (state-based API)
    val mileageField = TextFieldState()
    val notesField = TextFieldState()
    val amountTextField = TextFieldState()
    val costTextField = TextFieldState()
    val pricePerLiterTextField = TextFieldState()
    val providerNameField = TextFieldState()

    // region Baseline snapshots used to determine whether the user has made edits.
    // Captured after the form is (possibly) populated with existing data so that
    // autopopulated values, such as mileage, do not make the form appear dirty.
    private var baselineMileage: String = ""
    private var baselineNotes: String = ""
    private var baselineAmount: String = ""
    private var baselineCost: String = ""
    private var baselinePricePerLiter: String = ""
    private var baselineProviderName: String = ""
    private var baselineDate: LocalDate? = null
    private var baselineBaseUiState: AddEventTypeFormState = AddEventTypeFormState()
    private var baselineRefuelUiState: RefuelEventFormState = RefuelEventFormState()
    private var baselineMaintenanceUiState = MaintenanceEventFormState()
    private var baselineInspectionUiState = InspectionEventFormState()

    /**
     * Snapshot the current form contents so that [isFormDirty] reports false
     * until the user makes a change.
     */
    private fun captureDirtyBaseline() {
        baselineMileage = mileageField.text.toString()
        baselineNotes = notesField.text.toString()
        baselineAmount = amountTextField.text.toString()
        baselineCost = costTextField.text.toString()
        baselinePricePerLiter = pricePerLiterTextField.text.toString()
        baselineProviderName = providerNameField.text.toString()
        baselineDate = datePickerState.getSelectedDate()
        baselineBaseUiState = _baseUiState.value
        baselineRefuelUiState = _refuelUiState.value
        baselineMaintenanceUiState = _maintenanceUiState.value
        baselineInspectionUiState = _inspectionUiState.value
    }

    // endregion

    /**
     * Returns true if any of the form's text fields or UI states have been
     * changed by the user relative to the captured baseline, otherwise false.
     */
    val isFormDirty: Boolean
        get() {
            val textFieldsEdited =
                mileageField.text.toString() != baselineMileage ||
                        notesField.text.toString() != baselineNotes ||
                        amountTextField.text.toString() != baselineAmount ||
                        costTextField.text.toString() != baselineCost ||
                        pricePerLiterTextField.text.toString() != baselinePricePerLiter ||
                        providerNameField.text.toString() != baselineProviderName ||
                        datePickerState.getSelectedDate() != baselineDate

            val baseUiStateEdited =
                _baseUiState.value != baselineBaseUiState

            val refuelUiStateEdited =
                _refuelUiState.value != baselineRefuelUiState

            val maintenanceUiStateEdited =
                _maintenanceUiState.value != baselineMaintenanceUiState

            val inspectionUiStateEdited =
                _inspectionUiState.value != baselineInspectionUiState

            return textFieldsEdited ||
                    baseUiStateEdited ||
                    refuelUiStateEdited ||
                    maintenanceUiStateEdited ||
                    inspectionUiStateEdited
        }

    val datePickerState = DatePickerState(
        locale = CalendarLocale.getDefault(),
        initialSelectedDate = LocalDate.now(),
    )

    // This is currently only used in tests
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

    fun onServiceTypeChange(value: MaintenanceServiceType) {
        _maintenanceUiState.update { current ->
            current.copy(
                doneWork = if (value in current.doneWork) {
                    current.doneWork.minus(value)
                } else {
                    current.doneWork.plus(value)
                }
            )
        }
    }

    fun onCreateNewServiceType(value: MaintenanceServiceType) {
        _draftServiceTypes.update { it.plus(value) }
        onServiceTypeChange(value)
    }

    fun onInspectedPartChange(value: InspectablePart) {
        _inspectionUiState.update { current ->
            current.copy(
                inspectedParts = if (value in current.inspectedParts) {
                    current.inspectedParts.minus(value)
                } else {
                    current.inspectedParts.plus(value)
                }
            )
        }
    }

    fun onCreateNewInspectablePart(value: InspectablePart) {
        _draftInspectableParts.update { it.plus(value) }
        onInspectedPartChange(value)
    }

    init {
        viewModelScope.launch {
            if (id != null) {
                when (type) {
                    EventType.REFUEL -> {
                        loadRefuelEvent(id)
                    }

                    EventType.MAINTENANCE -> {
                        loadMaintenanceEvent(id)
                    }

                    EventType.INSPECTION -> {
                        loadInspectionEvent(id)
                    }
                }
            } else {
                mileageField.setTextAndPlaceCursorAtEnd(
                    eventRepository.getHighestMileage()?.toString() ?: ""
                )
            }
            captureDirtyBaseline()
        }

        observeFieldEditsToClearErrors()
    }

    /**
     * The text fields use TextFieldState, there is no onValueChange
     * to clear an error from. Watch the states instead and remove the field's error when user edits it.
     */
    private fun observeFieldEditsToClearErrors() {
        launchClearingOnEdit(mileageField) {
            it.copy(mileage = null, date = null)
        }
        launchClearingOnEdit(amountTextField) {
            it.copy(amount = null)
        }
        launchClearingOnEdit(costTextField) {
            it.copy(cost = null, pricePerLiter = null)
        }
        launchClearingOnEdit(pricePerLiterTextField) {
            it.copy(pricePerLiter = null, cost = null)
        }
    }

    private fun launchClearingOnEdit(
        field: TextFieldState,
        clear: (AddEventFormErrors) -> AddEventFormErrors
    ) {
        viewModelScope.launch {
            snapshotFlow { field.text.toString() }
                .drop(1)
                .collect { _formErrors.update(clear) }
        }
    }

    private suspend fun loadMaintenanceEvent(id: UUID) {
        val eventWithServices: MaintenanceEventWithServices? =
            maintenanceRepository.getByIdWithServiceTypes(id)

        if (eventWithServices != null) {
            _baseUiState.value = AddEventTypeFormState(
                type = EventType.MAINTENANCE,
                mileage = eventWithServices.event.base.mileage,
                date = eventWithServices.event.base.date,
                notes = eventWithServices.event.base.notes
            )

            _maintenanceUiState.value = MaintenanceEventFormState(
                doneWork = eventWithServices.services.toSet(),
                cost = eventWithServices.event.totalCost
            )

            mileageField.setTextAndPlaceCursorAtEnd(
                eventWithServices.event.base.mileage?.toString() ?: ""
            )

            costTextField.setTextAndPlaceCursorAtEnd(
                eventWithServices.event.totalCost?.toString() ?: ""
            )

            notesField.setTextAndPlaceCursorAtEnd(
                eventWithServices.event.base.notes
            )

            datePickerState.setSelectedDate(eventWithServices.event.base.date)

            eventWithServices.event.base.savedLocationId?.let { id ->
                locationRepository.getById(id)?.let {
                    onLocationChange(it)
                }
            }
        }
    }

    private suspend fun loadInspectionEvent(id: UUID) {
        val eventWithParts: InspectionEventWithParts? =
            inspectionRepository.getByIdWithParts(id)

        if (eventWithParts != null) {
            _baseUiState.value = AddEventTypeFormState(
                type = EventType.INSPECTION,
                mileage = eventWithParts.event.base.mileage,
                date = eventWithParts.event.base.date,
                notes = eventWithParts.event.base.notes
            )

            _inspectionUiState.value = InspectionEventFormState(
                inspectedParts = eventWithParts.parts.toSet()
            )

            mileageField.setTextAndPlaceCursorAtEnd(
                eventWithParts.event.base.mileage?.toString() ?: ""
            )

            notesField.setTextAndPlaceCursorAtEnd(
                eventWithParts.event.base.notes
            )

            datePickerState.setSelectedDate(eventWithParts.event.base.date)

            eventWithParts.event.base.savedLocationId?.let { locationId ->
                locationRepository.getById(locationId)?.let {
                    onLocationChange(it)
                }
            }
        }
    }

    private suspend fun loadRefuelEvent(id: UUID) {
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

            datePickerState.setSelectedDate(event.base.date)

            event.base.savedLocationId?.let { id ->
                locationRepository.getById(id)?.let {
                    onLocationChange(it)
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
        val dateValue = datePickerState.getSelectedDate()

        val updatedBaseState = baseState.copy(
            mileage = mileageValue,
            notes = notesValue,
            date = dateValue
        )

        viewModelScope.launch {
            val errors = validate(updatedBaseState)
            _formErrors.value = errors
            if (errors.hasAnyError) return@launch

            baseState.location?.let { savedLocation -> storeLocationIfNotExist(savedLocation) }

            when (updatedBaseState.type) {
                EventType.REFUEL -> {
                    val updatedRefuelState = calculateRefuelEventData()

                    storeRefuelEvent(updatedRefuelState, updatedBaseState)
                }

                EventType.INSPECTION -> {
                    val state = _inspectionUiState.value

                    storeInspectionEvent(state, updatedBaseState)
                }

                EventType.MAINTENANCE -> {
                    val state = _maintenanceUiState.value.copy(
                        cost = costTextField.text.toString().toBigDecimalOrNull()
                    )

                    storeMaintenanceEvent(state, updatedBaseState)
                }
            }
        }
    }

    /**
     * Validates the values of the form and returns the problems found, keyed by field.
     *
     * Empty fields are valid by design; only values that cannot be stored as entered are
     * reported. Purely computational apart from [validateBaseValues], which queries the
     * database for the date/mileage consistency rule.
     */
    internal suspend fun validate(baseState: AddEventTypeFormState): AddEventFormErrors =
        baseState.type.let { type ->
            when (type) {
                EventType.REFUEL -> validateRefuel()
                EventType.MAINTENANCE -> validateMaintenance()
                EventType.INSPECTION -> AddEventFormErrors()
            }
        }.let { errors ->
            when {
                // Mileage is optional, but something that does not fit in a Long
                // would otherwise be silently dropped on save.
                mileageField.text.isNotBlank() && baseState.mileage == null ->
                    errors.copy(mileage = FieldError.MILEAGE_INVALID)

                !validateBaseValues(baseState) ->
                    errors.copy(
                        mileage = FieldError.DATE_MILEAGE_CONFLICT,
                        date = FieldError.DATE_MILEAGE_CONFLICT
                    )

                else -> errors
            }
        }

    private fun validateRefuel(): AddEventFormErrors {
        val state = _refuelUiState.value
        val amountRaw = amountTextField.text
        val costRaw = costTextField.text
        val pricePerLiterRaw = pricePerLiterTextField.text
        val amount = amountRaw.toString().toBigDecimalOrNull()
        val cost = costRaw.toString().toBigDecimalOrNull()
        val pricePerLiter = pricePerLiterRaw.toString().toBigDecimalOrNull()

        return AddEventFormErrors(
            amount = when {
                amountRaw.isBlank() -> null
                amount == null -> FieldError.AMOUNT_INVALID

                // a 0 would throw instead of saving
                amount <= BigDecimal.ZERO -> FieldError.AMOUNT_NOT_POSITIVE
                else -> null
            },
            // Only one field is validated, the other is calculated based on calculateCost
            cost = if (state.calculateCost) null else when {
                costRaw.isBlank() -> null
                cost == null -> FieldError.COST_INVALID
                cost < BigDecimal.ZERO -> FieldError.COST_NEGATIVE
                else -> null
            },
            pricePerLiter = if (!state.calculateCost) null else when {
                pricePerLiterRaw.isBlank() -> null
                pricePerLiter == null -> FieldError.PRICE_PER_LITER_INVALID
                // a 0 price is meaningless
                pricePerLiter <= BigDecimal.ZERO -> FieldError.PRICE_PER_LITER_NOT_POSITIVE
                else -> null
            }
        )
    }

    private fun validateMaintenance(): AddEventFormErrors {
        val costRaw = costTextField.text
        val cost = costRaw.toString().toBigDecimalOrNull()

        return AddEventFormErrors(
            cost = when {
                costRaw.isBlank() -> null
                cost == null -> FieldError.COST_INVALID
                cost < BigDecimal.ZERO -> FieldError.COST_NEGATIVE
                else -> null
            }
        )
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

    internal suspend fun storeInspectionEvent(
        inspectionState: InspectionEventFormState,
        baseState: AddEventTypeFormState
    ) {
        // Persist any draft parts that are newly created in the UI
        for (part in _draftInspectableParts.value) {
            if (part in inspectionState.inspectedParts)
                storeInspectablePartIfNotExist(part)
        }

        val baseColumns = BaseColumns(
            date = baseState.date,
            mileage = baseState.mileage,
            savedLocationId = baseState.location?.id,
            notes = baseState.notes
        )

        if (id != null) {
            val event = InspectionEvent(
                id = id,
                base = baseColumns,
                status = inspectionState.status
            )
            inspectionRepository.update(event, inspectionState.inspectedParts)
        } else {
            val event = InspectionEvent(
                base = baseColumns,
                status = inspectionState.status
            )
            inspectionRepository.insertWithUsedParts(event, inspectionState.inspectedParts)
        }

        dismissDialog()
    }

    internal suspend fun storeMaintenanceEvent(
        maintenanceState: MaintenanceEventFormState,
        baseState: AddEventTypeFormState
    ) {
        for (serviceType in _draftServiceTypes.value) {
            if (serviceType in maintenanceState.doneWork)
                storeMaintenanceServiceTypeIfNotExist(serviceType)
        }

        val baseColumns = BaseColumns(
            date = baseState.date,
            mileage = baseState.mileage,
            savedLocationId = baseState.location?.id,
            notes = baseState.notes

        )

        val event = MaintenanceEvent(
            base = baseColumns,
            totalCost = maintenanceState.cost
        )

        if (id != null) {
            event.id = id
            maintenanceRepository.update(event, maintenanceState.doneWork)
        } else
            maintenanceRepository.insertWithUsedServices(event, maintenanceState.doneWork)

        dismissDialog()
    }

    internal suspend fun storeLocationIfNotExist(
        location: SavedLocation
    ) {
        if (locationRepository.getById(location.id) == null)
            locationRepository.insert(location)
    }

    internal suspend fun storeMaintenanceServiceTypeIfNotExist(
        type: MaintenanceServiceType
    ) {
        if (type.id == 0L)
        // Set id to newly created id in DB
            type.id = maintenanceServiceTypeRepository.insert(type)
    }

    internal suspend fun storeInspectablePartIfNotExist(
        part: InspectablePart
    ) {
        if (part.id == 0L)
        // Set id to newly created id in DB
            part.id = inspectablePartRepository.insert(part)
    }

    private fun dismissDialog() {
        _dismissDialog.value = true
    }
}


data class AddEventTypeFormState(
    val type: EventType = EventType.REFUEL,
    val mileage: Long? = null,
    val date: LocalDate? = LocalDate.now(),
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
    val doneWork: Set<MaintenanceServiceType> = emptySet(),
    val cost: BigDecimal? = null
)

data class InspectionEventFormState(
    val inspectedParts: Set<InspectablePart> = emptySet(),
    val status: InspectionStatus? = null
)
