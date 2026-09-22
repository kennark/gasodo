@file:Suppress("UnusedFlow")

package com.gasodoapp.gasodo.feature.overview

import androidx.compose.material3.ExperimentalMaterial3Api
import com.gasodoapp.gasodo.core.database.BaseColumns
import com.gasodoapp.gasodo.core.database.entity.InspectionEvent
import com.gasodoapp.gasodo.core.database.entity.MaintenanceEvent
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import com.gasodoapp.gasodo.core.database.junctions.InspectionEventWithParts
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import com.gasodoapp.gasodo.core.database.repository.InspectionRepository
import com.gasodoapp.gasodo.core.database.repository.MaintenanceRepository
import com.gasodoapp.gasodo.core.database.repository.RefuelRepository
import com.gasodoapp.gasodo.core.enums.InspectionStatus
import com.gasodoapp.gasodo.core.enums.PaymentMethod
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.ZoneOffset

/**
 * Unit tests for OverviewScreenViewModel.
 * Tests cover refuel/maintenance data loading, statistics calculation, and date range handling.
 */
class OverviewScreenViewModelTest {

    private lateinit var viewModel: OverviewScreenViewModel
    private lateinit var refuelRepository: RefuelRepository
    private lateinit var maintenanceRepository: MaintenanceRepository
    private lateinit var inspectionRepository: InspectionRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        // The ViewModel uses viewModelScope (Dispatchers.Main) for its loading coroutines,
        // so route Main to a test dispatcher to make collection deterministic.
        Dispatchers.setMain(UnconfinedTestDispatcher())

        refuelRepository = mockk()
        maintenanceRepository = mockk()
        inspectionRepository = mockk()

        // Default to empty flows so the ViewModel's init block can run without stubbing.
        every { refuelRepository.getAllWithinTime(any(), any()) } returns flowOf(emptyList())
        every { maintenanceRepository.getAllWithinTime(any(), any()) } returns flowOf(emptyList())
        every { inspectionRepository.getAllWithinTime(any(), any()) } returns flowOf(emptyList())

        viewModel =
            OverviewScreenViewModel(refuelRepository, maintenanceRepository, inspectionRepository)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Helpers

    private fun refuelEvent(
        mileage: Long?,
        amountLiters: BigDecimal?,
        pricePerLiter: BigDecimal?,
        totalCost: BigDecimal?,
        fullFillUp: Boolean = false
    ): RefuelEvent = RefuelEvent(
        base = BaseColumns(mileage = mileage),
        amountLiters = amountLiters,
        pricePerLiter = pricePerLiter,
        totalCost = totalCost,
        paymentMethod = PaymentMethod.CARD,
        fullFillUp = fullFillUp
    )

    private fun maintenanceEvent(
        totalCost: BigDecimal?,
        vararg services: MaintenanceServiceType
    ): MaintenanceEventWithServices = MaintenanceEventWithServices(
        event = MaintenanceEvent(base = BaseColumns(), totalCost = totalCost),
        services = services.toList()
    )

    private fun inspectionEvent(status: InspectionStatus?): InspectionEventWithParts =
        InspectionEventWithParts(
            event = InspectionEvent(base = BaseColumns(), status = status),
            parts = emptyList()
        )

    // endregion

    // region Initial state

    @Test
    fun `initial state has empty data and zero totals`() {
        assertThat(viewModel.refuelData.value).isEmpty()
        assertThat(viewModel.maintenanceData.value).isEmpty()
        assertThat(viewModel.inspectionData.value).isEmpty()
        assertThat(viewModel.totalRefuelCost.value).isEqualTo(BigDecimal.ZERO)
        assertThat(viewModel.totalLiters.value).isEqualTo(BigDecimal.ZERO)
        assertThat(viewModel.totalMileage.value).isNull()
        assertThat(viewModel.fuelConsumption.value).isNull()
        assertThat(viewModel.fuelCost.value).isNull()
        assertThat(viewModel.averagePricePerLiter.value).isNull()
        assertThat(viewModel.totalMaintenanceCost.value).isEqualTo(BigDecimal.ZERO)
        assertThat(viewModel.maintenanceActions.value).isEmpty()
        assertThat(viewModel.topMaintenanceActions.value).isEmpty()
        assertThat(viewModel.inspectionCount.value).isEqualTo(0)
        assertThat(viewModel.passCount.value).isEqualTo(0)
        assertThat(viewModel.failCount.value).isEqualTo(0)
        assertThat(viewModel.conditionalPassCount.value).isEqualTo(0)
        assertThat(viewModel.passPercentage.value).isEqualTo(BigDecimal.ZERO)
        assertThat(viewModel.failPercentage.value).isEqualTo(BigDecimal.ZERO)
        assertThat(viewModel.conditionalPassPercentage.value).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun `initial date range defaults to current month`() {
        val now = java.time.LocalDate.now()
        assertThat(viewModel.dateRangePickerState.selectedStartDateMillis).isEqualTo(
            java.time.LocalDate.of(now.year, now.monthValue, 1).atStartOfDay(ZoneOffset.UTC)
                .toInstant().toEpochMilli()
        )
        assertThat(viewModel.dateRangePickerState.selectedEndDateMillis).isEqualTo(
            now.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
    }

    // endregion

    // region Refuel data loading

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadRefuelData collects data and clears loading state`() = runTest {
        val events = listOf(
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            ),
            refuelEvent(
                1100L,
                BigDecimal("12"),
                BigDecimal("2"),
                BigDecimal("24"),
                fullFillUp = true
            )
        )
        every { refuelRepository.getAllWithinTime(any(), any()) } returns flowOf(events)

        viewModel.loadRefuelData(0L, 1000L)

        advanceUntilIdle()

        assertThat(viewModel.refuelData.value).isEqualTo(events)
        assertThat(viewModel.isLoading.value).isFalse()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadRefuelData does nothing when end is null`() = runTest {
        // The init block already triggered one call; a null-bound call must not add another.
        viewModel.loadRefuelData(0L, null)

        advanceUntilIdle()

        coVerify(exactly = 1) { refuelRepository.getAllWithinTime(any(), any()) }
        assertThat(viewModel.refuelData.value).isEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadRefuelData does nothing when start is null`() = runTest {
        // The init block already triggered one call; a null-bound call must not add another.
        viewModel.loadRefuelData(null, 0L)

        advanceUntilIdle()

        coVerify(exactly = 1) { refuelRepository.getAllWithinTime(any(), any()) }
        assertThat(viewModel.refuelData.value).isEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadRefuelData handles flow error by resetting data`() = runTest {
        every { refuelRepository.getAllWithinTime(any(), any()) } returns flow {
            throw RuntimeException("boom")
        }

        viewModel.loadRefuelData(0L, 1000L)

        advanceUntilIdle()

        assertThat(viewModel.refuelData.value).isEmpty()
    }

    // endregion

    // region Refuel statistics

    @Test
    fun `calculateRefuelStatistics sums cost and liters`() {
        val data = listOf(
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            ),
            refuelEvent(
                1100L,
                BigDecimal("12"),
                BigDecimal("2"),
                BigDecimal("24"),
                fullFillUp = true
            )
        )

        viewModel.calculateRefuelStatistics(data)

        assertThat(viewModel.totalRefuelCost.value).isEqualTo(BigDecimal("44"))
        assertThat(viewModel.totalLiters.value).isEqualTo(BigDecimal("22"))
    }

    @Test
    fun `calculateRefuelStatistics treats null values as zero`() {
        val data = listOf(
            refuelEvent(1000L, null, null, null, fullFillUp = true),
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            )
        )

        viewModel.calculateRefuelStatistics(data)

        assertThat(viewModel.totalRefuelCost.value).isEqualTo(BigDecimal("20"))
        assertThat(viewModel.totalLiters.value).isEqualTo(BigDecimal("10"))
    }

    @Test
    fun `calculateRefuelStatistics computes average price per liter`() {
        val data = listOf(
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2.00"),
                BigDecimal("20"),
                fullFillUp = true
            ),
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("3.00"),
                BigDecimal("30"),
                fullFillUp = true
            )
        )

        viewModel.calculateRefuelStatistics(data)

        // (2.00 + 3.00) / 2 = 2.50
        assertThat(viewModel.averagePricePerLiter.value).isEqualTo(BigDecimal("2.50"))
    }

    @Test
    fun `calculateRefuelStatistics leaves average price null when no prices exist`() {
        val data = listOf(
            refuelEvent(1000L, BigDecimal("10"), null, BigDecimal("20"), fullFillUp = true),
            refuelEvent(1100L, BigDecimal("10"), null, BigDecimal("30"), fullFillUp = true)
        )

        viewModel.calculateRefuelStatistics(data)

        assertThat(viewModel.averagePricePerLiter.value).isNull()
    }

    @Test
    fun `calculateRefuelStatistics computes total mileage from first to last event`() {
        // Database query sorts the data by mileage descending
        val data = listOf(
            refuelEvent(
                1200L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            ),
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            ),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            )
        )

        viewModel.calculateRefuelStatistics(data)

        assertThat(viewModel.totalMileage.value).isEqualTo(200L)
    }

    @Test
    fun `calculateRefuelStatistics skips mileage and consumption for single event`() {
        val data = listOf(
            refuelEvent(1000L, BigDecimal("10"), BigDecimal("2"), BigDecimal("20"))
        )

        viewModel.calculateRefuelStatistics(data)

        assertThat(viewModel.totalMileage.value).isNull()
        assertThat(viewModel.fuelConsumption.value).isNull()
        assertThat(viewModel.fuelCost.value).isNull()
    }

    @Test
    fun `calculateRefuelStatistics computes fuel consumption between full fill-ups`() {
        val data = listOf(
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            ),
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            ),
            refuelEvent(
                1200L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            )
        )

        viewModel.calculateRefuelStatistics(data)

        // Liters after the first full fill = 10 + 10 = 20, mileage = 200
        // 20 / 200 * 100 = 10.00
        assertThat(viewModel.fuelConsumption.value).isEqualTo(BigDecimal("10.00"))
    }

    @Test
    fun `calculateRefuelStatistics computes fuel cost between full fill-ups`() {
        val data = listOf(
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            ),
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            ),
            refuelEvent(
                1200L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            )
        )

        viewModel.calculateRefuelStatistics(data)

        // Cost after the first full fill = 20 + 20 = 40, mileage = 200
        // 40 / 200 * 100 = 20.00
        assertThat(viewModel.fuelCost.value).isEqualTo(BigDecimal("20.00"))
    }

    @Test
    fun `calculateRefuelStatistics does nothing for empty data`() {
        viewModel.calculateRefuelStatistics(emptyList())

        assertThat(viewModel.totalRefuelCost.value).isEqualTo(BigDecimal.ZERO)
        assertThat(viewModel.totalLiters.value).isEqualTo(BigDecimal.ZERO)
        assertThat(viewModel.averagePricePerLiter.value).isNull()
    }

    // endregion

    // region Maintenance data loading

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadMaintenanceData collects data and clears loading state`() = runTest {
        val service = MaintenanceServiceType(id = 1, serviceName = "Oil change")
        val data = listOf(maintenanceEvent(BigDecimal("50"), service))
        every { maintenanceRepository.getAllWithinTime(any(), any()) } returns flowOf(data)

        viewModel.loadMaintenanceData(0L, 1000L)

        advanceUntilIdle()

        assertThat(viewModel.maintenanceData.value).isEqualTo(data)
        assertThat(viewModel.isLoading.value).isFalse()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadMaintenanceData does nothing when end is null`() = runTest {
        // The init block already triggered one call; a null-bound call must not add another.
        viewModel.loadMaintenanceData(0L, null)

        advanceUntilIdle()

        coVerify(exactly = 1) { maintenanceRepository.getAllWithinTime(any(), any()) }
        assertThat(viewModel.maintenanceData.value).isEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadMaintenanceData does nothing when start is null`() = runTest {
        // The init block already triggered one call; a null-bound call must not add another.
        viewModel.loadMaintenanceData(null, 0L)

        advanceUntilIdle()

        coVerify(exactly = 1) { maintenanceRepository.getAllWithinTime(any(), any()) }
        assertThat(viewModel.maintenanceData.value).isEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadMaintenanceData handles flow error by resetting data`() = runTest {
        every { maintenanceRepository.getAllWithinTime(any(), any()) } returns flow {
            throw RuntimeException("boom")
        }

        viewModel.loadMaintenanceData(0L, 1000L)

        advanceUntilIdle()

        assertThat(viewModel.maintenanceData.value).isEmpty()
    }

    // endregion

    // region Inspection data loading

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadInspectionData collects data and clears loading state`() = runTest {
        val data = listOf(
            inspectionEvent(InspectionStatus.PASS),
            inspectionEvent(InspectionStatus.FAIL)
        )
        every { inspectionRepository.getAllWithinTime(any(), any()) } returns flowOf(data)

        viewModel.loadInspectionData(0L, 1000L)

        advanceUntilIdle()

        assertThat(viewModel.inspectionData.value).isEqualTo(data)
        assertThat(viewModel.inspectionCount.value).isEqualTo(2)
        assertThat(viewModel.isLoading.value).isFalse()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadInspectionData does nothing when end is null`() = runTest {
        // The init block already triggered one call; a null-bound call must not add another.
        viewModel.loadInspectionData(0L, null)

        advanceUntilIdle()

        coVerify(exactly = 1) { inspectionRepository.getAllWithinTime(any(), any()) }
        assertThat(viewModel.inspectionData.value).isEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadInspectionData does nothing when start is null`() = runTest {
        // The init block already triggered one call; a null-bound call must not add another.
        viewModel.loadInspectionData(null, 0L)

        advanceUntilIdle()

        coVerify(exactly = 1) { inspectionRepository.getAllWithinTime(any(), any()) }
        assertThat(viewModel.inspectionData.value).isEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadInspectionData handles flow error by resetting data`() = runTest {
        every { inspectionRepository.getAllWithinTime(any(), any()) } returns flow {
            throw RuntimeException("boom")
        }

        viewModel.loadInspectionData(0L, 1000L)

        advanceUntilIdle()

        assertThat(viewModel.inspectionData.value).isEmpty()
    }

    // endregion

    // region Maintenance statistics

    @Test
    fun `calculateMaintenanceStatistics sums cost and flattens services`() {
        val serviceA = MaintenanceServiceType(id = 1, serviceName = "Oil change")
        val serviceB = MaintenanceServiceType(id = 2, serviceName = "Tire rotation")
        val data = listOf(
            maintenanceEvent(BigDecimal("50"), serviceA, serviceB),
            maintenanceEvent(BigDecimal("30"), serviceA)
        )

        viewModel.calculateMaintenanceStatistics(data)

        assertThat(viewModel.totalMaintenanceCost.value).isEqualTo(BigDecimal("80"))
        assertThat(viewModel.maintenanceActions.value).containsExactly(serviceA, serviceB, serviceA)
    }

    @Test
    fun `calculateMaintenanceStatistics treats null cost as zero`() {
        val data = listOf(
            maintenanceEvent(null),
            maintenanceEvent(BigDecimal("30"))
        )

        viewModel.calculateMaintenanceStatistics(data)

        assertThat(viewModel.totalMaintenanceCost.value).isEqualTo(BigDecimal("30"))
    }

    @Test
    fun `calculateMaintenanceStatistics computes top actions sorted by count`() {
        val oil = MaintenanceServiceType(id = 1, serviceName = "Oil change")
        val tire = MaintenanceServiceType(id = 2, serviceName = "Tire rotation")
        val brake = MaintenanceServiceType(id = 3, serviceName = "Brake service")
        val data = listOf(
            maintenanceEvent(BigDecimal("10"), oil, tire),
            maintenanceEvent(BigDecimal("10"), oil, brake),
            maintenanceEvent(BigDecimal("10"), oil, tire)
        )

        viewModel.calculateMaintenanceStatistics(data)

        val top = viewModel.topMaintenanceActions.value
        assertThat(top).hasSize(3)
        assertThat(top[0].serviceType.serviceName).isEqualTo("Oil change")
        assertThat(top[0].count).isEqualTo(3)
        assertThat(top[1].count).isEqualTo(2)
        assertThat(top[2].count).isEqualTo(1)
    }

    @Test
    fun `calculateMaintenanceStatistics limits top actions to three`() {
        val services =
            (1..5).map { MaintenanceServiceType(id = it.toLong(), serviceName = "Service $it") }
        val data = services.map { maintenanceEvent(BigDecimal("10"), it) }

        viewModel.calculateMaintenanceStatistics(data)

        assertThat(viewModel.topMaintenanceActions.value).hasSize(3)
    }

    @Test
    fun `calculateMaintenanceStatistics does nothing for empty data`() {
        viewModel.calculateMaintenanceStatistics(emptyList())

        assertThat(viewModel.totalMaintenanceCost.value).isEqualTo(BigDecimal.ZERO)
        assertThat(viewModel.maintenanceActions.value).isEmpty()
        assertThat(viewModel.topMaintenanceActions.value).isEmpty()
    }

    // endregion

    // region Inspection statistics

    @Test
    fun `calculateInspectionStatistics counts events by status`() {
        val data = listOf(
            inspectionEvent(InspectionStatus.PASS),
            inspectionEvent(InspectionStatus.PASS),
            inspectionEvent(InspectionStatus.FAIL),
            inspectionEvent(InspectionStatus.CONDITIONAL_PASS)
        )

        viewModel.calculateInspectionStatistics(data)

        assertThat(viewModel.inspectionCount.value).isEqualTo(4)
        assertThat(viewModel.passCount.value).isEqualTo(2)
        assertThat(viewModel.failCount.value).isEqualTo(1)
        assertThat(viewModel.conditionalPassCount.value).isEqualTo(1)
    }

    @Test
    fun `calculateInspectionStatistics computes percentages`() {
        // 2 of 4 = 50.00%, 1 of 4 = 25.00%
        val data = listOf(
            inspectionEvent(InspectionStatus.PASS),
            inspectionEvent(InspectionStatus.PASS),
            inspectionEvent(InspectionStatus.FAIL),
            inspectionEvent(InspectionStatus.CONDITIONAL_PASS)
        )

        viewModel.calculateInspectionStatistics(data)

        assertThat(viewModel.passPercentage.value).isEqualTo(BigDecimal("50.00"))
        assertThat(viewModel.failPercentage.value).isEqualTo(BigDecimal("25.00"))
        assertThat(viewModel.conditionalPassPercentage.value).isEqualTo(BigDecimal("25.00"))
    }

    @Test
    fun `calculateInspectionStatistics handles all pass`() {
        val data = listOf(
            inspectionEvent(InspectionStatus.PASS),
            inspectionEvent(InspectionStatus.PASS),
            inspectionEvent(InspectionStatus.PASS)
        )

        viewModel.calculateInspectionStatistics(data)

        assertThat(viewModel.inspectionCount.value).isEqualTo(3)
        assertThat(viewModel.passCount.value).isEqualTo(3)
        assertThat(viewModel.failCount.value).isEqualTo(0)
        assertThat(viewModel.conditionalPassCount.value).isEqualTo(0)
        assertThat(viewModel.passPercentage.value).isEqualTo(BigDecimal("100.00"))
        assertThat(viewModel.failPercentage.value).isEqualTo(BigDecimal("0.00"))
        assertThat(viewModel.conditionalPassPercentage.value).isEqualTo(BigDecimal("0.00"))
    }

    @Test
    fun `calculateInspectionStatistics calculates with null events`() {
        val data = listOf(
            inspectionEvent(InspectionStatus.PASS),
            inspectionEvent(null),
            inspectionEvent(InspectionStatus.FAIL)
        )

        viewModel.calculateInspectionStatistics(data)

        assertThat(viewModel.inspectionCount.value).isEqualTo(3)
        assertThat(viewModel.passCount.value).isEqualTo(1)
        assertThat(viewModel.failCount.value).isEqualTo(1)
        assertThat(viewModel.conditionalPassCount.value).isEqualTo(0)
    }

    @Test
    fun `calculateInspectionStatistics handles empty data`() {
        viewModel.calculateInspectionStatistics(emptyList())

        assertThat(viewModel.inspectionCount.value).isEqualTo(0)
        assertThat(viewModel.passCount.value).isEqualTo(0)
        assertThat(viewModel.failCount.value).isEqualTo(0)
        assertThat(viewModel.conditionalPassCount.value).isEqualTo(0)
        assertThat(viewModel.passPercentage.value).isEqualTo(BigDecimal.ZERO)
        assertThat(viewModel.failPercentage.value).isEqualTo(BigDecimal.ZERO)
        assertThat(viewModel.conditionalPassPercentage.value).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun `calculateInspectionStatistics handles all events with null`() {
        val data = listOf(
            inspectionEvent(null),
            inspectionEvent(null)
        )

        viewModel.calculateInspectionStatistics(data)

        assertThat(viewModel.inspectionCount.value).isEqualTo(2)
        assertThat(viewModel.passCount.value).isEqualTo(0)
        assertThat(viewModel.failCount.value).isEqualTo(0)
        assertThat(viewModel.conditionalPassCount.value).isEqualTo(0)
        assertThat(viewModel.passPercentage.value).isEqualToIgnoringScale(BigDecimal.ZERO)
        assertThat(viewModel.failPercentage.value).isEqualToIgnoringScale(BigDecimal.ZERO)
        assertThat(viewModel.conditionalPassPercentage.value).isEqualToIgnoringScale(BigDecimal.ZERO)
    }

    // endregion

    // region Date selection

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class)
    @Test
    fun `onDateSelected reloads refuel, maintenance and inspection data`() = runTest {
        val refuelEvents = listOf(
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            )
        )
        val service = MaintenanceServiceType(id = 1, serviceName = "Oil change")
        val maintenanceEvents = listOf(maintenanceEvent(BigDecimal("50"), service))
        val inspectionEvents = listOf(inspectionEvent(InspectionStatus.PASS))

        every { refuelRepository.getAllWithinTime(any(), any()) } returns flowOf(refuelEvents)
        every { maintenanceRepository.getAllWithinTime(any(), any()) } returns flowOf(
            maintenanceEvents
        )
        every { inspectionRepository.getAllWithinTime(any(), any()) } returns flowOf(
            inspectionEvents
        )

        viewModel.onDateSelected()

        advanceUntilIdle()

        // init block + onDateSelected = 2 calls each
        coVerify(exactly = 2) { refuelRepository.getAllWithinTime(any(), any()) }
        coVerify(exactly = 2) { maintenanceRepository.getAllWithinTime(any(), any()) }
        coVerify(exactly = 2) { inspectionRepository.getAllWithinTime(any(), any()) }
        assertThat(viewModel.refuelData.value).isEqualTo(refuelEvents)
        assertThat(viewModel.maintenanceData.value).isEqualTo(maintenanceEvents)
        assertThat(viewModel.inspectionData.value).isEqualTo(inspectionEvents)
    }

    // endregion
}

