package com.gasodoapp.gasodo.feature.addevent

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.SavedStateHandle
import com.gasodoapp.gasodo.core.database.BaseColumns
import com.gasodoapp.gasodo.core.database.entity.InspectablePart
import com.gasodoapp.gasodo.core.database.entity.InspectionEvent
import com.gasodoapp.gasodo.core.database.entity.MaintenanceEvent
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.database.junctions.InspectionEventWithParts
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import com.gasodoapp.gasodo.core.database.projections.DateMileage
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
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Unit tests for AddEventScreenViewModel.
 * Tests cover form state management, validation logic, and event submission flow.
 */
class AddEventScreenViewModelTest {

    private lateinit var viewModel: AddEventScreenViewModel
    private lateinit var refuelRepository: RefuelRepository
    private lateinit var inspectionRepository: InspectionRepository
    private lateinit var inspectablePartRepository: InspectablePartRepository
    private lateinit var maintenanceRepository: MaintenanceRepository
    private lateinit var eventRepository: EventRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var locationRepository: SavedLocationRepository
    private lateinit var maintenanceServiceTypeRepository: MaintenanceServiceTypeRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        refuelRepository = mockk()
        inspectionRepository = mockk()
        inspectablePartRepository = mockk()
        maintenanceRepository = mockk()
        eventRepository = mockk()
        savedStateHandle = mockk()
        locationRepository = mockk()
        maintenanceServiceTypeRepository = mockk()

        every { savedStateHandle.get<EventType>(ADD_EVENT_TYPE_ARG) } returns EventType.REFUEL
        every { savedStateHandle.get<String?>(EDIT_EVENT_ID_ARG) } returns null
        every { locationRepository.getAll() } returns emptyFlow()
        every { maintenanceServiceTypeRepository.getAll() } returns emptyFlow()
        every { inspectablePartRepository.getAll() } returns emptyFlow()
        coEvery { eventRepository.getHighestMileage() } returns null

        viewModel = AddEventScreenViewModel(
            refuelRepository,
            inspectionRepository,
            inspectablePartRepository,
            maintenanceRepository,
            eventRepository,
            locationRepository,
            maintenanceServiceTypeRepository,
            savedStateHandle
        )

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default fields are set on ViewModel initialisation`() {
        // Text fields are empty
        assertThat(viewModel.notesField.text.length).isEqualTo(0)
        assertThat(viewModel.mileageField.text.length).isEqualTo(0)
        assertThat(viewModel.costTextField.text.length).isEqualTo(0)
        assertThat(viewModel.amountTextField.text.length).isEqualTo(0)
        assertThat(viewModel.pricePerLiterTextField.text.length).isEqualTo(0)
        assertThat(viewModel.providerNameField.text.length).isEqualTo(0)

        // Selected date is today
        assertThat(viewModel.datePickerState.selectedDateMillis).isEqualTo(
            LocalDate.now().toEpochDay().times(86400000L)
        )

        // No error or confirmation yet
        assertThat(viewModel.dismissDialog.value).isFalse()
        assertThat(viewModel.hasError.value).isFalse()

        // Form states are defaults
        assertThat(viewModel.baseUiState.value).isEqualTo(AddEventTypeFormState())
        assertThat(viewModel.refuelUiState.value).isEqualTo(RefuelEventFormState())
        assertThat(viewModel.maintenanceUiState.value).isEqualTo(MaintenanceEventFormState())
        assertThat(viewModel.inspectionUiState.value).isEqualTo(InspectionEventFormState())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `mileage field is set on ViewModel initialisation`() = runTest {
        coEvery { eventRepository.getHighestMileage() } returns 10L

        val testViewModel = AddEventScreenViewModel(
            refuelRepository,
            inspectionRepository,
            inspectablePartRepository,
            maintenanceRepository,
            eventRepository,
            locationRepository,
            maintenanceServiceTypeRepository,
            savedStateHandle
        )

        advanceUntilIdle()

        // 2 for the overwritten mock & setup
        coVerify(exactly = 2) { eventRepository.getHighestMileage() }

        assertThat(testViewModel.mileageField.text.toString()).isEqualTo("10")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `onSubmit with Refuel form, calculateCost true and all data correct`() = runTest {
        val higherEvent =
            DateMileage(LocalDate.now().plusDays(1).toEpochDay().times(86400000L), 1235)
        val lowerEvent =
            DateMileage(LocalDate.now().minusDays(1).toEpochDay().times(86400000L), 1233)
        val mileage = 1234L
        val notes = "notes"
        val amount = "10.00"
        val pricePerLiter = "2.00"
        viewModel.mileageField.setTextAndPlaceCursorAtEnd(mileage.toString())
        viewModel.onFormTypeChange(EventType.REFUEL)
        viewModel.amountTextField.setTextAndPlaceCursorAtEnd(amount)
        viewModel.pricePerLiterTextField.setTextAndPlaceCursorAtEnd(pricePerLiter)
        viewModel.onCalculateCostChange(true)
        viewModel.notesField.setTextAndPlaceCursorAtEnd(notes)

        coEvery { eventRepository.getDateWithHigherMileage(mileage) } returns higherEvent
        coEvery { eventRepository.getDateWithLowerMileage(mileage) } returns lowerEvent
        coEvery { refuelRepository.upsert(any()) } returns Unit

        viewModel.onSubmit()

        advanceUntilIdle()

        coVerify(exactly = 1) { refuelRepository.upsert(any()) }

        assertThat(viewModel.dismissDialog.value).isTrue()
    }

    @Test
    fun `calculateRefuelEventData calculates cost when calculateCost is true`() {
        // Given
        viewModel.amountTextField.setTextAndPlaceCursorAtEnd("15.0")
        viewModel.pricePerLiterTextField.setTextAndPlaceCursorAtEnd("2.50")
        viewModel.onCalculateCostChange(true)

        // When
        val result = viewModel.calculateRefuelEventData()

        // Then - amount is extracted correctly
        assertThat(result.amount).isEqualTo(BigDecimal("15.0"))

        // And cost is calculated (amount × pricePerLiter)
        assertThat(result.cost).isEqualTo(BigDecimal("37.500"))

        // And price per liter is preserved from input
        assertThat(result.pricePerLiter).isEqualTo(BigDecimal("2.50"))

        // And other fields remain unchanged
        assertThat(result.calculateCost).isTrue()
    }

    @Test
    fun `calculateRefuelEventData calculates pricePerLiter when calculateCost is false`() {
        // Given
        viewModel.amountTextField.setTextAndPlaceCursorAtEnd("10.0")
        viewModel.costTextField.setTextAndPlaceCursorAtEnd("25.00")
        viewModel.onCalculateCostChange(false)

        // When
        val result = viewModel.calculateRefuelEventData()

        // Then - amount is extracted correctly
        assertThat(result.amount).isEqualTo(BigDecimal("10.0"))

        // And cost is extracted from text field
        assertThat(result.cost).isEqualTo(BigDecimal("25.00"))

        // And pricePerLiter is calculated (cost / amount)
        assertThat(result.pricePerLiter).isEqualToIgnoringScale(BigDecimal("2.5"))

        // And calculateCost flag remains false
        assertThat(result.calculateCost).isFalse()
    }

    @Test
    fun `calculateRefuelEventData handles empty amount gracefully`() {
        // Given - manually set the text field to empty string
        viewModel.amountTextField.setTextAndPlaceCursorAtEnd("")
        viewModel.pricePerLiterTextField.setTextAndPlaceCursorAtEnd("2.50")
        viewModel.onCalculateCostChange(true)

        // When
        val result = viewModel.calculateRefuelEventData()

        // Then - amount is null when empty
        assertThat(result.amount).isNull()

        // And cost is also null (cannot calculate without amount)
        assertThat(result.cost).isNull()

        // And pricePerLiter remains from input
        assertThat(result.pricePerLiter).isEqualTo(BigDecimal("2.50"))
    }

    @Test
    fun `calculateRefuelEventData handles empty cost gracefully`() {
        // Given - manually set the text field to empty string
        viewModel.amountTextField.setTextAndPlaceCursorAtEnd("10.00")
        viewModel.costTextField.setTextAndPlaceCursorAtEnd("")
        viewModel.onCalculateCostChange(false)

        // When
        val result = viewModel.calculateRefuelEventData()

        // Then - amount is null when empty
        assertThat(result.amount).isEqualTo(BigDecimal("10.00"))

        // And cost is also null (cannot calculate without amount)
        assertThat(result.cost).isNull()

        // And pricePerLiter remains from input
        assertThat(result.pricePerLiter).isNull()
    }

    @Test
    fun `calculateRefuelEventData handles empty pricePerLiter gracefully`() {
        // Given - manually set the text field to empty string
        viewModel.amountTextField.setTextAndPlaceCursorAtEnd("10.00")
        viewModel.pricePerLiterTextField.setTextAndPlaceCursorAtEnd("")
        viewModel.onCalculateCostChange(true)

        // When
        val result = viewModel.calculateRefuelEventData()

        // Then - amount is null when empty
        assertThat(result.amount).isEqualTo(BigDecimal("10.00"))

        // And cost is also null (cannot calculate without amount)
        assertThat(result.cost).isNull()

        // And pricePerLiter remains from input
        assertThat(result.pricePerLiter).isNull()
    }

    @Test
    fun `calculateRefuelEventData handles invalid decimal values`() {
        // Given - empty or invalid text in fields
        viewModel.amountTextField.setTextAndPlaceCursorAtEnd("234.432d")
        viewModel.costTextField.setTextAndPlaceCursorAtEnd("invalid")
        viewModel.onCalculateCostChange(false)

        // When
        val result = viewModel.calculateRefuelEventData()

        // Then - all numeric fields are null when input is invalid/empty
        assertThat(result.amount).isNull()
        assertThat(result.cost).isNull()
        assertThat(result.pricePerLiter).isNull()
    }

    @Test
    fun `calculateRefuelEventData preserves all other fields in RefuelFormState`() {
        // Given
        viewModel.onCalculateCostChange(true)
        viewModel.onPaymentMethodChange(PaymentMethod.MOBILE_PAYMENT)
        viewModel.onFullFillUpChange(true)

        // Set some initial values (will be reset by calculateRefuelEventData)
        viewModel.amountTextField.setTextAndPlaceCursorAtEnd("10.0")
        viewModel.pricePerLiterTextField.setTextAndPlaceCursorAtEnd("2.00")

        // When - get the default state which has defaults for paymentMethod and fullFillUp
        val result = viewModel.calculateRefuelEventData()

        // Then - calculateCost flag is preserved
        assertThat(result.calculateCost).isTrue()

        // And other values are not touched
        assertThat(result.paymentMethod).isEqualTo(PaymentMethod.MOBILE_PAYMENT)
        assertThat(result.fullFillUp).isTrue()

    }

    @Test
    fun `calculateRefuelEventData handles null TextField text`() {
        // Given - manually clear the state by setting empty strings
        viewModel.amountTextField.setTextAndPlaceCursorAtEnd("")
        viewModel.costTextField.setTextAndPlaceCursorAtEnd("")
        viewModel.pricePerLiterTextField.setTextAndPlaceCursorAtEnd("")

        // When
        val result = viewModel.calculateRefuelEventData()

        // Then - all extracted values are null
        assertThat(result.amount).isNull()
        assertThat(result.cost).isNull()
        assertThat(result.pricePerLiter).isNull()
    }

    @Test
    fun `calculateRefuelEventData calculates cost with multiple decimal places`() {
        // Given
        viewModel.amountTextField.setTextAndPlaceCursorAtEnd("12.345")
        viewModel.pricePerLiterTextField.setTextAndPlaceCursorAtEnd("3.14159")
        viewModel.onCalculateCostChange(true)

        // When
        val result = viewModel.calculateRefuelEventData()

        // Then - amount is preserved with full precision
        assertThat(result.amount).isEqualTo(BigDecimal("12.345"))

        // And pricePerLiter is preserved with full precision
        assertThat(result.pricePerLiter).isEqualTo(BigDecimal("3.14159"))

        // And cost is calculated using BigDecimal math context
        val expectedCost = BigDecimal("12.345").multiply(BigDecimal("3.14159"))
        assertThat(result.cost).isEqualTo(expectedCost)
    }

    @Test
    fun `calculateRefuelEventData calculates pricePerLiter with multiple decimal places`() {
        // Given
        viewModel.amountTextField.setTextAndPlaceCursorAtEnd("8.765")
        viewModel.costTextField.setTextAndPlaceCursorAtEnd("20.12345")
        viewModel.onCalculateCostChange(false)

        val expectedAmount = BigDecimal("8.765")
        val expectedCost = BigDecimal("20.12345")

        // When
        val result = viewModel.calculateRefuelEventData()

        // Then - amount is preserved with full precision
        assertThat(result.amount).isEqualTo(expectedAmount)

        // And cost is preserved with full precision
        assertThat(result.cost).isEqualTo(expectedCost)

        // And pricePerLiter is calculated using BigDecimal math context
        val expectedPricePerLiter =
            expectedCost.divide(
                expectedAmount,
                BigDecimalUtils.SCALE,
                BigDecimalUtils.ROUNDING_MODE
            )
        assertThat(result.pricePerLiter).isEqualToIgnoringScale(expectedPricePerLiter)
    }

    @Test
    fun `validateBaseValues returns true when no higher or lower events exist`() = runTest {
        // Given
        val mileage = 1000L
        val date = LocalDate.now().toEpochDay()
        val state = AddEventTypeFormState(mileage = mileage, date = date)

        coEvery { eventRepository.getDateWithHigherMileage(any()) } returns null
        coEvery { eventRepository.getDateWithLowerMileage(any()) } returns null

        // When & Then
        assertThat(viewModel.validateBaseValues(state)).isTrue()
    }

    @Test
    fun `validateBaseValues returns false when higher mileage event has earlier date`() = runTest {
        // Given - an event with higher mileage that occurred in the past
        val currentMileage = 1000L
        val currentDate = LocalDate.now().toEpochDay()
        val state = AddEventTypeFormState(mileage = currentMileage, date = currentDate)

        val earlierDate = currentDate - 86400000 // one day earlier
        val higherEvent = DateMileage(earlierDate, 1500L) // higher mileage, earlier date

        coEvery { eventRepository.getDateWithHigherMileage(any()) } returns higherEvent
        coEvery { eventRepository.getDateWithLowerMileage(any()) } returns null

        // When & Then
        assertThat(viewModel.validateBaseValues(state)).isFalse()
    }

    @Test
    fun `validateBaseValues returns true when higher mileage event has equal or later date`() =
        runTest {
            // Given - an event with higher mileage that occurred on or after the current date
            val currentMileage = 1000L
            val currentDate = LocalDate.now().toEpochDay()
            val state = AddEventTypeFormState(mileage = currentMileage, date = currentDate)

            val laterDate = currentDate + 86400000 // one day later
            val higherEvent = DateMileage(laterDate, 1500L) // higher mileage, later date

            coEvery { eventRepository.getDateWithHigherMileage(any()) } returns higherEvent
            coEvery { eventRepository.getDateWithLowerMileage(any()) } returns null

            // When & Then
            assertThat(viewModel.validateBaseValues(state)).isTrue()
        }

    @Test
    fun `validateBaseValues returns false when lower mileage event has later date`() = runTest {
        // Given - an event with lower mileage that occurred in the future
        val currentMileage = 1000L
        val currentDate = LocalDate.now().toEpochDay()
        val state = AddEventTypeFormState(mileage = currentMileage, date = currentDate)

        val laterDate = currentDate + 86400000 // one day later
        val lowerEvent = DateMileage(laterDate, 500L) // lower mileage, later date

        coEvery { eventRepository.getDateWithHigherMileage(any()) } returns null
        coEvery { eventRepository.getDateWithLowerMileage(any()) } returns lowerEvent

        // When & Then
        assertThat(viewModel.validateBaseValues(state)).isFalse()
    }

    @Test
    fun `validateBaseValues returns true when lower mileage event has equal or earlier date`() =
        runTest {
            // Given - an event with lower mileage that occurred on or before the current date
            val currentMileage = 1000L
            val currentDate = LocalDate.now().toEpochDay()
            val state = AddEventTypeFormState(mileage = currentMileage, date = currentDate)

            val earlierDate = currentDate - 86400000 // one day earlier
            val lowerEvent = DateMileage(earlierDate, 500L) // lower mileage, earlier date

            coEvery { eventRepository.getDateWithHigherMileage(any()) } returns null
            coEvery { eventRepository.getDateWithLowerMileage(any()) } returns lowerEvent

            // When & Then
            assertThat(viewModel.validateBaseValues(state)).isTrue()
        }

    @Test
    fun `validateBaseValues returns true when both higher and lower events have valid dates`() =
        runTest {
            // Given - both higher and lower mileage events exist with correct date ordering
            val currentMileage = 1000L
            val currentDate = LocalDate.now().toEpochDay()
            val state = AddEventTypeFormState(mileage = currentMileage, date = currentDate)

            // Higher mileage event is after the current date (valid)
            val higherEvent = DateMileage(currentDate + 86400000, 1500L)

            coEvery { eventRepository.getDateWithHigherMileage(any()) } returns higherEvent
            coEvery { eventRepository.getDateWithLowerMileage(any()) } returns null

            // When & Then
            assertThat(viewModel.validateBaseValues(state)).isTrue()
        }

    @Test
    fun `validateBaseValues returns true when date is null`() = runTest {
        // Given - state with no date set
        val state = AddEventTypeFormState(mileage = 1000L, date = null)

        // When & Then - validation passes through because date is null
        assertThat(viewModel.validateBaseValues(state)).isTrue()
    }

    @Test
    fun `validateBaseValues returns true when mileage is null`() = runTest {
        // Given - state with no milestone set
        val currentDate = LocalDate.now().toEpochDay()
        val state = AddEventTypeFormState(mileage = null, date = currentDate)

        // When & Then - validation passes through because milestone is null
        assertThat(viewModel.validateBaseValues(state)).isTrue()
    }

    @Test
    fun `storeRefuelEvent creates correct RefuelEvent and shows confirmation`() = runTest {
        val baseState = AddEventTypeFormState(
            date = LocalDate.now().toEpochDay().times(86400000),
            mileage = 1234,
            location = null,
            notes = "notes"
        )
        val refuelState = RefuelEventFormState(
            amount = BigDecimal("10.00"),
            calculateCost = true,
            cost = BigDecimal("20.00"),
            pricePerLiter = BigDecimal("1.23"),
            paymentMethod = PaymentMethod.MOBILE_PAYMENT,
            fullFillUp = true
        )
        val slot = slot<RefuelEvent>()

        coEvery { refuelRepository.upsert(capture(slot)) } returns Unit

        viewModel.storeRefuelEvent(refuelState, baseState)

        val expectedEvent = RefuelEvent(
            id = slot.captured.id,
            base = BaseColumns(
                date = LocalDate.now().toEpochDay().times(86400000),
                mileage = 1234,
                savedLocationId = null,
                notes = "notes"
            ),
            amountLiters = BigDecimal("10.00"),
            pricePerLiter = BigDecimal("1.23"),
            totalCost = BigDecimal("20.00"),
            paymentMethod = PaymentMethod.MOBILE_PAYMENT,
            fullFillUp = true,
        )

        coVerify(exactly = 1) {
            refuelRepository.upsert(
                event = expectedEvent
            )
        }

        assertThat(viewModel.dismissDialog.value).isTrue()
    }

    @Test
    fun `onSubmit with validation failure sets hasError`() = runTest {
        // Given - state that will fail validation
        val currentMileage = 1000L
        val currentDate = LocalDate.now().toEpochDay()
        val state = AddEventTypeFormState(mileage = currentMileage, date = currentDate)

        // Mock a higher mileage event with earlier date to trigger failure
        val earlierDate = currentDate - 86400000
        val higherEvent = DateMileage(earlierDate, 1500L)

        coEvery { eventRepository.getDateWithHigherMileage(any()) } returns higherEvent
        coEvery { eventRepository.getDateWithLowerMileage(any()) } returns null

        // When & Then - validation fails, so hasError should be set to true
        assertThat(viewModel.validateBaseValues(state)).isFalse()
    }

    @Test
    fun `storeLocationIfNotExist inserts location when it does not exist`() = runTest {
        // Given
        val location = SavedLocation(name = "Test Station")

        coEvery { locationRepository.getById(any()) } returns null
        coEvery { locationRepository.insert(location) } returns Unit

        // When & Then - verify insert is called
        viewModel.storeLocationIfNotExist(location)

        coVerify(exactly = 1) { locationRepository.insert(location) }
    }

    @Test
    fun `storeLocationIfNotExist does not insert when location already exists`() = runTest {
        // Given
        val location = SavedLocation(name = "Existing Station")

        coEvery { locationRepository.getById(any()) } returns location

        // When & Then - verify insert is NOT called because getById returned the existing location
        viewModel.storeLocationIfNotExist(location)

        coVerify(exactly = 0) { locationRepository.insert(any()) }
    }

    @Test
    fun `onInspectedPartChange adds part when not already selected`() {
        // Given
        val part = InspectablePart(partName = "Brakes")

        // When
        viewModel.onInspectedPartChange(part)

        // Then - part is added to inspectedParts
        assertThat(viewModel.inspectionUiState.value.inspectedParts).contains(part)
    }

    @Test
    fun `onInspectedPartChange removes part when already selected`() {
        // Given
        val part = InspectablePart(partName = "Brakes")
        viewModel.onInspectedPartChange(part)

        // When
        viewModel.onInspectedPartChange(part)

        // Then - part is removed from inspectedParts
        assertThat(viewModel.inspectionUiState.value.inspectedParts).doesNotContain(part)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `onCreateNewInspectablePart adds draft part and selects it`() = runTest {
        // Given
        val part = InspectablePart(partName = "Tires")

        // When
        viewModel.onCreateNewInspectablePart(part)

        // Then - the part is selected in inspectedParts
        assertThat(viewModel.inspectionUiState.value.inspectedParts).contains(part)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `onSubmit with Inspection form stores inspection event`() = runTest {
        // Given
        val higherEvent =
            DateMileage(LocalDate.now().plusDays(1).toEpochDay().times(86400000L), 1235)
        val lowerEvent =
            DateMileage(LocalDate.now().minusDays(1).toEpochDay().times(86400000L), 1233)
        val mileage = 1234L
        val notes = "inspection notes"
        val part = InspectablePart(partName = "Brakes")

        viewModel.onFormTypeChange(EventType.INSPECTION)
        viewModel.mileageField.setTextAndPlaceCursorAtEnd(mileage.toString())
        viewModel.onInspectedPartChange(part)
        viewModel.onStatusChange(InspectionStatus.PASS)
        viewModel.notesField.setTextAndPlaceCursorAtEnd(notes)

        coEvery { eventRepository.getDateWithHigherMileage(mileage) } returns higherEvent
        coEvery { eventRepository.getDateWithLowerMileage(mileage) } returns lowerEvent
        coEvery { inspectionRepository.insertWithUsedParts(any(), any()) } returns Unit

        viewModel.onSubmit()

        advanceUntilIdle()

        coVerify(exactly = 1) {
            inspectionRepository.insertWithUsedParts(
                event = match {
                    it.status == InspectionStatus.PASS &&
                            it.base.mileage == mileage &&
                            it.base.notes == notes
                },
                parts = setOf(part)
            )
        }

        assertThat(viewModel.dismissDialog.value).isTrue()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `onSubmit with existing Inspection id calls update instead of insert`() = runTest {
        // Given
        val existingId = UUID.randomUUID()
        coEvery { savedStateHandle.get<String?>(EDIT_EVENT_ID_ARG) } returns existingId.toString()
        coEvery { savedStateHandle.get<EventType>(ADD_EVENT_TYPE_ARG) } returns EventType.INSPECTION
        coEvery { inspectionRepository.getByIdWithParts(existingId) } returns null

        val editViewModel = AddEventScreenViewModel(
            refuelRepository,
            inspectionRepository,
            inspectablePartRepository,
            maintenanceRepository,
            eventRepository,
            locationRepository,
            maintenanceServiceTypeRepository,
            savedStateHandle
        )

        val higherEvent =
            DateMileage(LocalDate.now().plusDays(1).toEpochDay().times(86400000L), 1001)
        val lowerEvent =
            DateMileage(LocalDate.now().minusDays(1).toEpochDay().times(86400000L), 999)
        val mileage = 1000L
        val part = InspectablePart(partName = "Engine")

        editViewModel.onFormTypeChange(EventType.INSPECTION)
        editViewModel.mileageField.setTextAndPlaceCursorAtEnd(mileage.toString())
        editViewModel.onInspectedPartChange(part)
        editViewModel.onStatusChange(InspectionStatus.CONDITIONAL_PASS)

        coEvery { eventRepository.getDateWithHigherMileage(mileage) } returns higherEvent
        coEvery { eventRepository.getDateWithLowerMileage(mileage) } returns lowerEvent
        coEvery { inspectionRepository.update(any(), any()) } returns Unit

        editViewModel.onSubmit()

        advanceUntilIdle()

        coVerify(exactly = 1) {
            inspectionRepository.update(
                event = match {
                    it.id == existingId && it.status == InspectionStatus.CONDITIONAL_PASS
                },
                parts = setOf(part)
            )
        }

        coVerify(exactly = 0) { inspectionRepository.insertWithUsedParts(any(), any()) }
        assertThat(editViewModel.dismissDialog.value).isTrue()
    }

    @Test
    fun `storeInspectionEvent inserts new InspectionEvent and shows confirmation`() = runTest {
        // Given
        val baseState = AddEventTypeFormState(
            date = LocalDate.now().toEpochDay().times(86400000),
            mileage = 1234,
            location = null,
            notes = "inspection notes"
        )
        val part = InspectablePart(partName = "Brakes")
        val inspectionState = InspectionEventFormState(
            inspectedParts = setOf(part),
            status = InspectionStatus.PASS
        )

        coEvery { inspectionRepository.insertWithUsedParts(any(), any()) } returns Unit

        viewModel.storeInspectionEvent(inspectionState, baseState)

        coVerify(exactly = 1) {
            inspectionRepository.insertWithUsedParts(
                event = match {
                    it.status == InspectionStatus.PASS &&
                            it.base.mileage == 1234L &&
                            it.base.notes == "inspection notes"
                },
                parts = setOf(part)
            )
        }

        assertThat(viewModel.dismissDialog.value).isTrue()
    }

    @Test
    fun `storeInspectionEvent with existing id calls update`() = runTest {
        // Given
        val existingId = UUID.randomUUID()
        coEvery { savedStateHandle.get<String?>(EDIT_EVENT_ID_ARG) } returns existingId.toString()
        coEvery { savedStateHandle.get<EventType>(ADD_EVENT_TYPE_ARG) } returns EventType.INSPECTION
        coEvery { inspectionRepository.getByIdWithParts(existingId) } returns null

        val editViewModel = AddEventScreenViewModel(
            refuelRepository,
            inspectionRepository,
            inspectablePartRepository,
            maintenanceRepository,
            eventRepository,
            locationRepository,
            maintenanceServiceTypeRepository,
            savedStateHandle
        )

        val baseState = AddEventTypeFormState(
            date = LocalDate.now().toEpochDay().times(86400000),
            mileage = 1000,
            location = null,
            notes = "edit notes"
        )
        val part = InspectablePart(partName = "Engine")
        val inspectionState = InspectionEventFormState(
            inspectedParts = setOf(part),
            status = InspectionStatus.CONDITIONAL_PASS
        )

        coEvery { inspectionRepository.update(any(), any()) } returns Unit

        editViewModel.storeInspectionEvent(inspectionState, baseState)

        coVerify(exactly = 1) {
            inspectionRepository.update(
                event = match {
                    it.id == existingId && it.status == InspectionStatus.CONDITIONAL_PASS
                },
                parts = setOf(part)
            )
        }

        coVerify(exactly = 0) { inspectionRepository.insertWithUsedParts(any(), any()) }
        assertThat(editViewModel.dismissDialog.value).isTrue()
    }

    @Test
    fun `storeInspectablePartIfNotExist inserts part when it has no id`() = runTest {
        // Given
        val part = InspectablePart(partName = "Tires")
        coEvery { inspectablePartRepository.insert(part) } returns 5L

        // When
        viewModel.storeInspectablePartIfNotExist(part)

        // Then - part id is set from the DB insert
        assertThat(part.id).isEqualTo(5L)
        coVerify(exactly = 1) { inspectablePartRepository.insert(part) }
    }

    @Test
    fun `storeInspectablePartIfNotExist skips insert when part already has an id`() = runTest {
        // Given
        val part = InspectablePart(id = 10L, partName = "Brakes")

        // When
        viewModel.storeInspectablePartIfNotExist(part)

        // Then - no insert is performed
        coVerify(exactly = 0) { inspectablePartRepository.insert(any()) }
    }

    @Test
    fun `storeMaintenanceEvent inserts MaintenanceEvent with used services`() = runTest {
        // Given
        val baseState = AddEventTypeFormState(
            date = LocalDate.now().toEpochDay().times(86400000),
            mileage = 1500,
            location = null,
            notes = "oil change"
        )
        val serviceType = MaintenanceServiceType(serviceName = "Oil Change")
        val maintenanceState = MaintenanceEventFormState(
            doneWork = setOf(serviceType),
            cost = BigDecimal("49.99")
        )

        coEvery { maintenanceRepository.insertWithUsedServices(any(), any()) } returns Unit

        viewModel.storeMaintenanceEvent(maintenanceState, baseState)

        coVerify(exactly = 1) {
            maintenanceRepository.insertWithUsedServices(
                event = match {
                    it.totalCost == BigDecimal("49.99") &&
                            it.base.mileage == 1500L &&
                            it.base.notes == "oil change"
                },
                services = setOf(serviceType)
            )
        }

        assertThat(viewModel.dismissDialog.value).isTrue()
    }

    @Test
    fun `storeMaintenanceEvent with existing id calls update`() = runTest {
        // Given
        val existingId = UUID.randomUUID()
        coEvery { savedStateHandle.get<String?>(EDIT_EVENT_ID_ARG) } returns existingId.toString()
        coEvery { savedStateHandle.get<EventType>(ADD_EVENT_TYPE_ARG) } returns EventType.MAINTENANCE
        coEvery { maintenanceRepository.getByIdWithServiceTypes(existingId) } returns null

        val editViewModel = AddEventScreenViewModel(
            refuelRepository,
            inspectionRepository,
            inspectablePartRepository,
            maintenanceRepository,
            eventRepository,
            locationRepository,
            maintenanceServiceTypeRepository,
            savedStateHandle
        )

        val baseState = AddEventTypeFormState(
            date = LocalDate.now().toEpochDay().times(86400000),
            mileage = 2000,
            location = null,
            notes = "edit work"
        )
        val serviceType = MaintenanceServiceType(serviceName = "Brake Pads")
        val maintenanceState = MaintenanceEventFormState(
            doneWork = setOf(serviceType),
            cost = BigDecimal("120.00")
        )

        coEvery { maintenanceRepository.update(any(), any()) } returns Unit

        editViewModel.storeMaintenanceEvent(maintenanceState, baseState)

        coVerify(exactly = 1) {
            maintenanceRepository.update(
                event = match {
                    it.id == existingId && it.totalCost == BigDecimal("120.00")
                },
                services = setOf(serviceType)
            )
        }

        coVerify(exactly = 0) { maintenanceRepository.insertWithUsedServices(any(), any()) }
        assertThat(editViewModel.dismissDialog.value).isTrue()
    }

    @Test
    fun `storeMaintenanceEvent stores draft service types before insert`() = runTest {
        // Given - a draft service type that is selected in doneWork
        val draftType = MaintenanceServiceType(serviceName = "New Service")
        viewModel.onCreateNewServiceType(draftType)
        val baseState = AddEventTypeFormState(
            date = LocalDate.now().toEpochDay().times(86400000),
            mileage = 1800,
            location = null,
            notes = ""
        )
        val maintenanceState = MaintenanceEventFormState(
            doneWork = setOf(draftType),
            cost = BigDecimal("75.00")
        )

        coEvery { maintenanceServiceTypeRepository.insert(draftType) } returns 7L
        coEvery { maintenanceRepository.insertWithUsedServices(any(), any()) } returns Unit

        viewModel.storeMaintenanceEvent(maintenanceState, baseState)

        // Then - the draft service type id is set from the DB
        assertThat(draftType.id).isEqualTo(7L)
        coVerify(exactly = 1) { maintenanceServiceTypeRepository.insert(draftType) }
        coVerify(exactly = 1) { maintenanceRepository.insertWithUsedServices(any(), any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadInspectionEvent populates parts and base state when editing`() = runTest {
        // Given
        val existingId = UUID.randomUUID()
        coEvery { savedStateHandle.get<String?>(EDIT_EVENT_ID_ARG) } returns existingId.toString()
        coEvery { savedStateHandle.get<EventType>(ADD_EVENT_TYPE_ARG) } returns EventType.INSPECTION

        val part = InspectablePart(id = 3L, partName = "Brakes")
        val inspectionEvent = InspectionEvent(
            id = existingId,
            base = BaseColumns(
                date = LocalDate.now().toEpochDay().times(86400000),
                mileage = 2000,
                savedLocationId = null,
                notes = "Inspection notes"
            ),
            status = InspectionStatus.PASS
        )
        coEvery { inspectionRepository.getByIdWithParts(existingId) } returns
                InspectionEventWithParts(event = inspectionEvent, parts = listOf(part))
        coEvery { eventRepository.getHighestMileage() } returns null

        val editViewModel = AddEventScreenViewModel(
            refuelRepository,
            inspectionRepository,
            inspectablePartRepository,
            maintenanceRepository,
            eventRepository,
            locationRepository,
            maintenanceServiceTypeRepository,
            savedStateHandle
        )

        advanceUntilIdle()

        // Then - base state reflects the loaded event
        assertThat(editViewModel.baseUiState.value.type).isEqualTo(EventType.INSPECTION)
        assertThat(editViewModel.baseUiState.value.mileage).isEqualTo(2000L)
        assertThat(editViewModel.baseUiState.value.notes).isEqualTo("Inspection notes")

        // And - inspected parts are populated
        assertThat(editViewModel.inspectionUiState.value.inspectedParts).contains(part)
    }

    @Test
    fun `storeMaintenanceServiceTypeIfNotExist inserts type when it has no id`() = runTest {
        // Given
        val type = MaintenanceServiceType(serviceName = "New Service")
        coEvery { maintenanceServiceTypeRepository.insert(type) } returns 5L

        // When
        viewModel.storeMaintenanceServiceTypeIfNotExist(type)

        // Then - type id is set from the DB insert
        assertThat(type.id).isEqualTo(5L)
        coVerify(exactly = 1) { maintenanceServiceTypeRepository.insert(type) }
    }

    @Test
    fun `storeMaintenanceServiceTypeIfNotExist skips insert when type already has an id`() =
        runTest {
            // Given
            val type = MaintenanceServiceType(id = 10L, serviceName = "Existing Service")

            // When
            viewModel.storeMaintenanceServiceTypeIfNotExist(type)

            // Then - no insert is performed
            coVerify(exactly = 0) { maintenanceServiceTypeRepository.insert(any()) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadRefuelEvent populates fields when editing`() = runTest {
        // Given
        val existingId = UUID.randomUUID()
        coEvery { savedStateHandle.get<String?>(EDIT_EVENT_ID_ARG) } returns existingId.toString()
        coEvery { savedStateHandle.get<EventType>(ADD_EVENT_TYPE_ARG) } returns EventType.REFUEL

        val refuelEvent = RefuelEvent(
            id = existingId,
            base = BaseColumns(
                date = LocalDate.now().toEpochDay().times(86400000),
                mileage = 3000,
                savedLocationId = null,
                notes = "refuel notes"
            ),
            amountLiters = BigDecimal("40.00"),
            pricePerLiter = BigDecimal("1.80"),
            totalCost = BigDecimal("72.00"),
            paymentMethod = PaymentMethod.MOBILE_PAYMENT,
            fullFillUp = true
        )
        coEvery { refuelRepository.getById(existingId) } returns refuelEvent
        coEvery { eventRepository.getHighestMileage() } returns null

        val editViewModel = AddEventScreenViewModel(
            refuelRepository,
            inspectionRepository,
            inspectablePartRepository,
            maintenanceRepository,
            eventRepository,
            locationRepository,
            maintenanceServiceTypeRepository,
            savedStateHandle
        )

        advanceUntilIdle()

        // Then - base state reflects the loaded event
        assertThat(editViewModel.baseUiState.value.type).isEqualTo(EventType.REFUEL)
        assertThat(editViewModel.baseUiState.value.mileage).isEqualTo(3000L)
        assertThat(editViewModel.baseUiState.value.notes).isEqualTo("refuel notes")

        // And - refuel state is populated
        assertThat(editViewModel.refuelUiState.value.amount).isEqualTo(BigDecimal("40.00"))
        assertThat(editViewModel.refuelUiState.value.pricePerLiter).isEqualTo(BigDecimal("1.80"))
        assertThat(editViewModel.refuelUiState.value.cost).isEqualTo(BigDecimal("72.00"))
        assertThat(editViewModel.refuelUiState.value.paymentMethod)
            .isEqualTo(PaymentMethod.MOBILE_PAYMENT)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadMaintenanceEvent populates fields when editing`() = runTest {
        // Given
        val existingId = UUID.randomUUID()
        coEvery { savedStateHandle.get<String?>(EDIT_EVENT_ID_ARG) } returns existingId.toString()
        coEvery { savedStateHandle.get<EventType>(ADD_EVENT_TYPE_ARG) } returns EventType.MAINTENANCE

        val serviceType = MaintenanceServiceType(id = 3L, serviceName = "Oil Change")
        val maintenanceEvent = MaintenanceEvent(
            id = existingId,
            base = BaseColumns(
                date = LocalDate.now().toEpochDay().times(86400000),
                mileage = 4000,
                savedLocationId = null,
                notes = "maintenance notes"
            ),
            totalCost = BigDecimal("95.50")
        )
        coEvery { maintenanceRepository.getByIdWithServiceTypes(existingId) } returns
                MaintenanceEventWithServices(
                    event = maintenanceEvent,
                    services = listOf(serviceType)
                )
        coEvery { eventRepository.getHighestMileage() } returns null

        val editViewModel = AddEventScreenViewModel(
            refuelRepository,
            inspectionRepository,
            inspectablePartRepository,
            maintenanceRepository,
            eventRepository,
            locationRepository,
            maintenanceServiceTypeRepository,
            savedStateHandle
        )

        advanceUntilIdle()

        // Then - base state reflects the loaded event
        assertThat(editViewModel.baseUiState.value.type).isEqualTo(EventType.MAINTENANCE)
        assertThat(editViewModel.baseUiState.value.mileage).isEqualTo(4000L)
        assertThat(editViewModel.baseUiState.value.notes).isEqualTo("maintenance notes")

        // And - maintenance state is populated
        assertThat(editViewModel.maintenanceUiState.value.cost).isEqualTo(BigDecimal("95.50"))
        assertThat(editViewModel.maintenanceUiState.value.doneWork).contains(serviceType)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `onSubmit with Maintenance validation failure sets hasError`() = runTest {
        // Given - set form type to MAINTENANCE
        viewModel.onFormTypeChange(EventType.MAINTENANCE)
        viewModel.mileageField.setTextAndPlaceCursorAtEnd("1000")

        // Mock a higher mileage event with earlier date to trigger validation failure
        val earlierDate = LocalDate.now().minusDays(1).toEpochDay().times(86400000)
        val higherEvent = DateMileage(earlierDate, 1500L)
        coEvery { eventRepository.getDateWithHigherMileage(1000L) } returns higherEvent
        coEvery { eventRepository.getDateWithLowerMileage(1000L) } returns null

        viewModel.onSubmit()

        advanceUntilIdle()

        coVerify(exactly = 1) { eventRepository.getDateWithHigherMileage(1000L) }

        // Then - validation failed, so hasError is set and nothing is stored
        assertThat(viewModel.hasError.value).isTrue()
        coVerify(exactly = 0) { maintenanceRepository.insertWithUsedServices(any(), any()) }
        coVerify(exactly = 0) { maintenanceRepository.update(any(), any()) }
    }
}
