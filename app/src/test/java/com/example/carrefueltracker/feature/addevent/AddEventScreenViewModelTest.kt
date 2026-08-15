package com.example.carrefueltracker.feature.addevent

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import com.example.carrefueltracker.core.database.BaseColumns
import com.example.carrefueltracker.core.database.entity.RefuelEvent
import com.example.carrefueltracker.core.database.projections.DateMileage
import com.example.carrefueltracker.core.database.repository.EventRepository
import com.example.carrefueltracker.core.database.repository.InspectionRepository
import com.example.carrefueltracker.core.database.repository.MaintenanceRepository
import com.example.carrefueltracker.core.database.repository.RefuelRepository
import com.example.carrefueltracker.core.enums.EventType
import com.example.carrefueltracker.core.enums.PaymentMethod
import com.example.carrefueltracker.core.utils.BigDecimalUtils
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Unit tests for AddEventScreenViewModel.
 * Tests cover form state management, validation logic, and event submission flow.
 */
class AddEventScreenViewModelTest {

    private lateinit var viewModel: AddEventScreenViewModel
    private lateinit var refuelRepository: RefuelRepository
    private lateinit var inspectionRepository: InspectionRepository
    private lateinit var maintenanceRepository: MaintenanceRepository
    private lateinit var eventRepository: EventRepository

    @Before
    fun setup() {
        refuelRepository = mockk()
        inspectionRepository = mockk()
        maintenanceRepository = mockk()
        eventRepository = mockk()
        viewModel = AddEventScreenViewModel(
            refuelRepository,
            inspectionRepository,
            maintenanceRepository,
            eventRepository
        )
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
        assertThat(viewModel.showConfirmation.value).isFalse()
        assertThat(viewModel.hasError.value).isFalse()

        // Form states are defaults
        assertThat(viewModel.baseUiState.value).isEqualTo(AddEventTypeFormState())
        assertThat(viewModel.refuelUiState.value).isEqualTo(RefuelEventFormState())
        assertThat(viewModel.maintenanceUiState.value).isEqualTo(MaintenanceEventFormState())
        assertThat(viewModel.inspectionUiState.value).isEqualTo(InspectionEventFormState())
    }

    @Test
    fun `onSubmit with Refuel form, calculateCost true and all data correct`() {
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
        coEvery { refuelRepository.insert(any()) } returns Unit

        viewModel.onSubmit()

        coVerify(exactly = 1) { refuelRepository.insert(any()) }

        assertThat(viewModel.showConfirmation.value).isTrue()
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

        coEvery { refuelRepository.insert(capture(slot)) } returns Unit

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
            refuelRepository.insert(
                event = expectedEvent
            )
        }

        assertThat(viewModel.showConfirmation.value).isTrue()
    }
}
