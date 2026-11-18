@file:Suppress("UnusedFlow")

package com.gasodoapp.gasodo.feature.overview

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
import com.gasodoapp.gasodo.core.enums.units.FuelConsumption
import com.gasodoapp.gasodo.core.settings.SettingsRepository
import com.gasodoapp.gasodo.core.settings.Units
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import java.time.LocalDate
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
    private lateinit var settingsRepository: SettingsRepository

    /** Unit preferences the ViewModel observes; tests update it to simulate a settings change. */
    private lateinit var unitsFlow: MutableStateFlow<Units>

    private companion object {
        /** Arbitrary, non-null bounds used to drive the explicit loading functions. */
        val RANGE_START: LocalDate = LocalDate.of(2025, 1, 1)
        val RANGE_END: LocalDate = LocalDate.of(2025, 6, 30)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        // The ViewModel uses viewModelScope (Dispatchers.Main) for its loading coroutines,
        // so route Main to a test dispatcher to make collection deterministic.
        Dispatchers.setMain(UnconfinedTestDispatcher())

        refuelRepository = mockk()
        maintenanceRepository = mockk()
        inspectionRepository = mockk()
        settingsRepository = mockk()

        unitsFlow = MutableStateFlow(Units())
        every { settingsRepository.units } returns unitsFlow

        // Default to empty flows so the ViewModel's init block can run without stubbing.
        every { refuelRepository.getAllWithinTime(any(), any()) } returns flowOf(emptyList())
        every { maintenanceRepository.getAllWithinTime(any(), any()) } returns flowOf(emptyList())
        every { inspectionRepository.getAllWithinTime(any(), any()) } returns flowOf(emptyList())

        viewModel =
            OverviewScreenViewModel(
                refuelRepository,
                maintenanceRepository,
                inspectionRepository,
                settingsRepository
            )
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
        fullFillUp: Boolean = false,
        date: LocalDate? = null
    ): RefuelEvent = RefuelEvent(
        base = BaseColumns(date = date, mileage = mileage),
        amountLiters = amountLiters,
        pricePerLiter = pricePerLiter,
        totalCost = totalCost,
        paymentMethod = PaymentMethod.CARD,
        fullFillUp = fullFillUp
    )

    private fun maintenanceEvent(
        totalCost: BigDecimal?,
        vararg services: MaintenanceServiceType,
        paymentMethod: PaymentMethod? = PaymentMethod.CARD
    ): MaintenanceEventWithServices = MaintenanceEventWithServices(
        event = MaintenanceEvent(
            base = BaseColumns(),
            totalCost = totalCost,
            paymentMethod = paymentMethod
        ),
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
        val now = LocalDate.now()
        assertThat(viewModel.dateRangePickerState.selectedStartDateMillis).isEqualTo(
            LocalDate.of(now.year, now.monthValue, 1).atStartOfDay(ZoneOffset.UTC)
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

        viewModel.loadRefuelData(RANGE_START, RANGE_END)

        advanceUntilIdle()

        assertThat(viewModel.refuelData.value).isEqualTo(events)
        assertThat(viewModel.isLoading.value).isFalse()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadRefuelData does nothing when end is null`() = runTest {
        // The init block already triggered one call; a null-bound call must not add another.
        viewModel.loadRefuelData(RANGE_START, null)

        advanceUntilIdle()

        coVerify(exactly = 1) { refuelRepository.getAllWithinTime(any(), any()) }
        assertThat(viewModel.refuelData.value).isEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadRefuelData does nothing when start is null`() = runTest {
        // The init block already triggered one call; a null-bound call must not add another.
        viewModel.loadRefuelData(null, RANGE_END)

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

        viewModel.loadRefuelData(RANGE_START, RANGE_END)

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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `calculateRefuelStatistics computes distance per liquid when configured`() = runTest {
        unitsFlow.value = Units(fuelConsumption = FuelConsumption.DISTANCE_PER_LIQUID)
        advanceUntilIdle()

        val data = listOf(
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            ),
            refuelEvent(
                1050L,
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
            )
        )

        viewModel.calculateRefuelStatistics(data)

        // 20 liquid over 100 distance units = 5.00 distance units per liquid unit
        assertThat(viewModel.fuelConsumption.value).isEqualTo(BigDecimal("5.00"))
        // Fuel cost keeps its per hundred distance units meaning, it does not follow the toggle
        assertThat(viewModel.fuelCost.value).isEqualTo(BigDecimal("40.00"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `calculateRefuelStatistics leaves consumption null when an amount is missing`() = runTest {
        unitsFlow.value = Units(fuelConsumption = FuelConsumption.DISTANCE_PER_LIQUID)
        advanceUntilIdle()

        val data = listOf(
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            ),
            refuelEvent(1050L, null, BigDecimal("2"), BigDecimal("20")),
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            )
        )

        viewModel.calculateRefuelStatistics(data)

        assertThat(viewModel.fuelConsumption.value).isNull()
        assertThat(viewModel.fuelCost.value).isEqualTo(BigDecimal("40.00"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `changing the fuel consumption unit recalculates without reloading`() = runTest {
        val data = listOf(
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            ),
            refuelEvent(
                1050L,
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
            )
        )

        every { refuelRepository.getAllWithinTime(any(), any()) } returns flowOf(data)

        // init block + this explicit load
        viewModel.loadRefuelData(RANGE_START, RANGE_END)
        advanceUntilIdle()

        // 20 / 100 * 100 = 20.00 liquid per hundred distance units
        assertThat(viewModel.fuelConsumption.value).isEqualTo(BigDecimal("20.00"))

        unitsFlow.value = Units(fuelConsumption = FuelConsumption.DISTANCE_PER_LIQUID)
        advanceUntilIdle()

        // The same stretch read the other way round: 100 / 20 = 5.00
        assertThat(viewModel.fuelConsumption.value).isEqualTo(BigDecimal("5.00"))
        // The stored events did not change, so the repository must not have been queried again.
        coVerify(exactly = 2) { refuelRepository.getAllWithinTime(any(), any()) }
    }

    @Test
    fun `calculateRefuelStatistics leaves cost null when a cost is missing`() {
        val data = listOf(
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            ),
            refuelEvent(1100L, BigDecimal("10"), BigDecimal("2"), null),
            refuelEvent(
                1200L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            )
        )

        viewModel.calculateRefuelStatistics(data)

        // Every amount is present, so the consumption is still measurable
        assertThat(viewModel.fuelConsumption.value).isEqualTo(BigDecimal("10.00"))
        assertThat(viewModel.fuelCost.value).isNull()
    }

    /**
     * Two full fill-ups recorded on the same mileage give a zero distance. Dividing by it would
     * either throw or produce an infinite figure, so both values must stay unset instead.
     */
    @Test
    fun `calculateRefuelStatistics skips consumption when full fill-ups share a mileage`() {
        val data = listOf(
            refuelEvent(
                1000L,
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

        assertThat(viewModel.totalMileage.value).isEqualTo(0L)
        assertThat(viewModel.fuelConsumption.value).isNull()
        assertThat(viewModel.fuelCost.value).isNull()
    }

    /**
     * Consumption is measured from the first to the last full fill-up, so refuels outside that
     * window must not contribute to the figure even though they do count towards the totals.
     */
    @Test
    fun `calculateRefuelStatistics ignores refuels outside the full fill-up window`() {
        // Database query sorts the data by mileage descending
        val data = listOf(
            refuelEvent(1400L, BigDecimal("100"), BigDecimal("2"), BigDecimal("200")),
            refuelEvent(
                1200L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            ),
            refuelEvent(1100L, BigDecimal("10"), BigDecimal("2"), BigDecimal("20")),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true
            ),
            refuelEvent(900L, BigDecimal("50"), BigDecimal("2"), BigDecimal("100"))
        )

        viewModel.calculateRefuelStatistics(data)

        // Only the 1000 -> 1200 window is measured: 20 liquid and 40 cost over 200 distance units
        assertThat(viewModel.fuelConsumption.value).isEqualTo(BigDecimal("10.00"))
        assertThat(viewModel.fuelCost.value).isEqualTo(BigDecimal("20.00"))
        // The totals still cover every event of the period
        assertThat(viewModel.totalLiters.value).isEqualTo(BigDecimal("180"))
        assertThat(viewModel.totalMileage.value).isEqualTo(500L)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `changing the fuel consumption unit leaves the other statistics untouched`() = runTest {
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
            )
        )

        every { refuelRepository.getAllWithinTime(any(), any()) } returns flowOf(data)
        viewModel.loadRefuelData(RANGE_START, RANGE_END)
        advanceUntilIdle()

        unitsFlow.value = Units(fuelConsumption = FuelConsumption.DISTANCE_PER_LIQUID)
        advanceUntilIdle()

        assertThat(viewModel.totalRefuelCost.value).isEqualTo(BigDecimal("40"))
        assertThat(viewModel.totalLiters.value).isEqualTo(BigDecimal("20"))
        assertThat(viewModel.totalMileage.value).isEqualTo(100L)
        assertThat(viewModel.averagePricePerLiter.value).isEqualTo(BigDecimal("2.00"))
    }

    @Test
    fun `calculateRefuelStatistics does nothing for empty data`() {
        viewModel.calculateRefuelStatistics(emptyList())

        assertThat(viewModel.totalRefuelCost.value).isEqualTo(BigDecimal.ZERO)
        assertThat(viewModel.totalLiters.value).isEqualTo(BigDecimal.ZERO)
        assertThat(viewModel.averagePricePerLiter.value).isNull()
    }

    // endregion

    // region Detailed fuel consumption chart

    @Test
    fun `chart has one point per full fill interval`() {
        val january = LocalDate.of(2025, 1, 10)
        val february = LocalDate.of(2025, 2, 10)
        val march = LocalDate.of(2025, 3, 10)

        // Database query sorts the data by mileage descending
        val data = listOf(
            refuelEvent(
                1200L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = march
            ),
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = february
            ),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = january
            )
        )

        viewModel.calculateDetailedFuelConsumptionChart(data)

        val points = viewModel.fuelConsumptionChart.value

        // Three full fill-ups delimit two intervals, not three: the last one only closes
        assertThat(points).hasSize(2)
        assertThat(points.map { it.time })
            .containsExactly(february, march)
            .inOrder()
        // 10 liquid over 100 distance units = 10.00 liquid per hundred distance units
        assertThat(points.map { it.value })
            .containsExactly(BigDecimal("10.00"), BigDecimal("10.00"))
            .inOrder()
    }

    /**
     * A point describes the consumption of the interval that ENDS on it, which only becomes known
     * once the tank is filled up again. Dating it to the opening fill-up would shift the whole chart
     * to the left and make the most recent interval impossible to place on the timeline.
     */
    @Test
    fun `chart point is dated to the closing fill-up`() {
        val opening = LocalDate.of(2025, 1, 10)
        val closing = LocalDate.of(2025, 3, 10)

        val data = listOf(
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = closing
            ),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = opening
            )
        )

        viewModel.calculateDetailedFuelConsumptionChart(data)

        val points = viewModel.fuelConsumptionChart.value

        assertThat(points).hasSize(1)
        assertThat(points.single().time).isEqualTo(closing)
        assertThat(points.single().time).isNotEqualTo(opening)
    }

    @Test
    fun `chart points are ordered by ascending date`() {
        val january = LocalDate.of(2025, 1, 10)
        val february = LocalDate.of(2025, 2, 10)
        val march = LocalDate.of(2025, 3, 10)

        val data = listOf(
            refuelEvent(
                1200L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = march
            ),
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = february
            ),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = january
            )
        )

        viewModel.calculateDetailedFuelConsumptionChart(data)

        // The repository hands the events over by descending mileage, the chart must still be
        // readable left to right on a timeline.
        assertThat(viewModel.fuelConsumptionChart.value.map { it.time })
            .containsExactly(february, march)
            .inOrder()
    }

    /**
     * Intervals are measured in mileage order, but they are drawn on a timeline and joined in list
     * order. A user who back-dates an entry makes the two orders disagree, and the curve would then
     * run backwards on itself, so the emitted points must be sorted by date regardless of how they
     * were measured.
     */
    @Test
    fun `chart sorts points by date when entries are back dated`() {
        val backDated = LocalDate.of(2025, 3, 1)
        val later = LocalDate.of(2025, 1, 1)
        val latest = LocalDate.of(2025, 2, 1)

        // Mileage ascends 1000 -> 1100 -> 1200 while the dates are deliberately shuffled.
        val data = listOf(
            refuelEvent(
                1200L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = later
            ),
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = backDated
            ),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = latest
            )
        )

        viewModel.calculateDetailedFuelConsumptionChart(data)

        // Interval 1 closes at mileage 1100 (dated March) and interval 2 at 1200 (dated January),
        // so measuring order alone would emit March before January.
        assertThat(viewModel.fuelConsumptionChart.value.map { it.time })
            .containsExactly(later, backDated)
            .inOrder()
    }

    @Test
    fun `chart folds partial fills into the segment volume`() {
        val opening = LocalDate.of(2025, 1, 10)
        val closing = LocalDate.of(2025, 2, 10)

        val data = listOf(
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = closing
            ),
            refuelEvent(
                1050L,
                BigDecimal("20"),
                BigDecimal("2"),
                BigDecimal("40"),
                date = LocalDate.of(2025, 1, 20)
            ),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = opening
            )
        )

        viewModel.calculateDetailedFuelConsumptionChart(data)

        val points = viewModel.fuelConsumptionChart.value

        // The partial fill belongs to the interval it was bought in: 20 + 10 = 30 liquid over 100
        // distance units = 30.00 liquid per hundred distance units
        assertThat(points).hasSize(1)
        assertThat(points.single().value).isEqualTo(BigDecimal("30.00"))
        assertThat(points.single().time).isEqualTo(closing)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `chart computes distance per liquid when configured`() = runTest {
        unitsFlow.value = Units(fuelConsumption = FuelConsumption.DISTANCE_PER_LIQUID)
        advanceUntilIdle()

        val opening = LocalDate.of(2025, 1, 10)
        val closing = LocalDate.of(2025, 2, 10)

        val data = listOf(
            refuelEvent(
                1050L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = closing
            ),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = opening
            )
        )

        viewModel.calculateDetailedFuelConsumptionChart(data)

        // 10 liquid over 50 distance units = 5.00 distance units per liquid unit
        assertThat(viewModel.fuelConsumptionChart.value.single().value)
            .isEqualTo(BigDecimal("5.00"))
    }

    /**
     * A missing amount makes the interval's volume unknown. Zero-filling it would draw a point on
     * the chart that claims the car burnt nothing, so the interval is dropped instead while the
     * neighbouring ones keep their points.
     */
    @Test
    fun `chart skips segments with a null amount`() {
        val february = LocalDate.of(2025, 2, 10)
        val march = LocalDate.of(2025, 3, 10)

        val data = listOf(
            refuelEvent(
                1200L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = march
            ),
            refuelEvent(1150L, null, BigDecimal("2"), BigDecimal("20")),
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = february
            ),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = LocalDate.of(2025, 1, 10)
            )
        )

        viewModel.calculateDetailedFuelConsumptionChart(data)

        val points = viewModel.fuelConsumptionChart.value

        // The 1000 -> 1100 interval is measurable, the 1100 -> 1200 one is not
        assertThat(points).hasSize(1)
        assertThat(points.single().time).isEqualTo(february)
        assertThat(points.single().value).isEqualTo(BigDecimal("10.00"))
    }

    /**
     * Two fill-ups logged on the same mileage cover no distance, so the ratio would be a division by
     * zero. No point may be emitted for such an interval.
     */
    @Test
    fun `chart skips segments with no distance`() {
        val data = listOf(
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = LocalDate.of(2025, 2, 10)
            ),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = LocalDate.of(2025, 1, 10)
            )
        )

        viewModel.calculateDetailedFuelConsumptionChart(data)

        assertThat(viewModel.fuelConsumptionChart.value).isEmpty()
    }

    /**
     * A point cannot be placed on a timeline without a date. The interval is skipped rather than
     * pinned to an arbitrary `LocalDate`.
     */
    @Test
    fun `chart skips segments without a date`() {
        val data = listOf(
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = null
            ),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = LocalDate.of(2025, 1, 10)
            )
        )

        viewModel.calculateDetailedFuelConsumptionChart(data)

        assertThat(viewModel.fuelConsumptionChart.value).isEmpty()
    }

    @Test
    fun `chart ignores events without a mileage`() {
        val closing = LocalDate.of(2025, 2, 10)

        val data = listOf(
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = closing
            ),
            // Cannot be placed between two fill-ups, so it neither opens nor breaks an interval
            refuelEvent(
                null,
                BigDecimal("999"),
                BigDecimal("2"),
                BigDecimal("1998"),
                date = LocalDate.of(2025, 1, 20)
            ),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = LocalDate.of(2025, 1, 10)
            )
        )

        viewModel.calculateDetailedFuelConsumptionChart(data)

        val points = viewModel.fuelConsumptionChart.value

        // 10 liquid over 100 distance units; the 999 liter event takes no part in the volume
        assertThat(points).hasSize(1)
        assertThat(points.single().value).isEqualTo(BigDecimal("10.00"))
        assertThat(points.single().time).isEqualTo(closing)
    }

    @Test
    fun `chart is empty with fewer than two full fill ups`() {
        val data = listOf(
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                date = LocalDate.of(2025, 3, 10)
            ),
            refuelEvent(
                1050L,
                BigDecimal("20"),
                BigDecimal("2"),
                BigDecimal("40"),
                date = LocalDate.of(2025, 2, 10)
            ),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = LocalDate.of(2025, 1, 10)
            )
        )

        viewModel.calculateDetailedFuelConsumptionChart(data)

        // A single full fill-up opens an interval but nothing can close it
        assertThat(viewModel.fuelConsumptionChart.value).isEmpty()
    }

    /**
     * The chart belongs to the selected period. When the user narrows the range and the new events
     * no longer yield a point, the previous series must be dropped: keeping it would render old data
     * as if it belonged to the new range, which is worse than an empty chart.
     */
    @Test
    fun `chart is cleared when the period no longer yields points`() {
        val february = LocalDate.of(2025, 2, 10)
        val march = LocalDate.of(2025, 3, 10)

        val data = listOf(
            refuelEvent(
                1200L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = march
            ),
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = february
            ),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = LocalDate.of(2025, 1, 10)
            )
        )

        viewModel.calculateDetailedFuelConsumptionChart(data)
        assertThat(viewModel.fuelConsumptionChart.value).hasSize(2)

        viewModel.calculateDetailedFuelConsumptionChart(emptyList())
        assertThat(viewModel.fuelConsumptionChart.value).isEmpty()

        // Same for a period holding a single event, which cannot describe an interval either
        viewModel.calculateDetailedFuelConsumptionChart(data)
        assertThat(viewModel.fuelConsumptionChart.value).hasSize(2)

        viewModel.calculateDetailedFuelConsumptionChart(data.take(1))
        assertThat(viewModel.fuelConsumptionChart.value).isEmpty()
    }

    @Test
    fun `chart ignores refuels outside the full fill up window`() {
        val closing = LocalDate.of(2025, 2, 10)

        // Database query sorts the data by mileage descending
        val data = listOf(
            refuelEvent(
                1400L,
                BigDecimal("100"),
                BigDecimal("2"),
                BigDecimal("200"),
                date = LocalDate.of(2025, 3, 10)
            ),
            refuelEvent(
                1200L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = closing
            ),
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                date = LocalDate.of(2025, 1, 20)
            ),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = LocalDate.of(2025, 1, 10)
            ),
            refuelEvent(
                900L,
                BigDecimal("50"),
                BigDecimal("2"),
                BigDecimal("100"),
                date = LocalDate.of(2025, 1, 5)
            )
        )

        viewModel.calculateDetailedFuelConsumptionChart(data)

        val points = viewModel.fuelConsumptionChart.value

        // Only the 1000 -> 1200 window is measured: 20 liquid over 200 distance units
        assertThat(points).hasSize(1)
        assertThat(points.single().time).isEqualTo(closing)
        assertThat(points.single().value).isEqualTo(BigDecimal("10.00"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `chart is recomputed when the unit changes without reloading`() = runTest {
        val february = LocalDate.of(2025, 2, 10)
        val march = LocalDate.of(2025, 3, 10)

        val data = listOf(
            refuelEvent(
                1100L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = march
            ),
            refuelEvent(
                1050L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = february
            ),
            refuelEvent(
                1000L,
                BigDecimal("10"),
                BigDecimal("2"),
                BigDecimal("20"),
                fullFillUp = true,
                date = LocalDate.of(2025, 1, 10)
            )
        )

        every { refuelRepository.getAllWithinTime(any(), any()) } returns flowOf(data)

        // init block + this explicit load
        viewModel.loadRefuelData(RANGE_START, RANGE_END)
        advanceUntilIdle()

        // 10 / 50 * 100 = 20.00 liquid per hundred distance units, one point per interval
        assertThat(viewModel.fuelConsumptionChart.value.map { it.value })
            .containsExactly(BigDecimal("20.00"), BigDecimal("20.00"))
            .inOrder()

        unitsFlow.value = Units(fuelConsumption = FuelConsumption.DISTANCE_PER_LIQUID)
        advanceUntilIdle()

        // The same stretches read the other way round: 50 / 10 = 5.00, on the same dates
        assertThat(viewModel.fuelConsumptionChart.value.map { it.value })
            .containsExactly(BigDecimal("5.00"), BigDecimal("5.00"))
            .inOrder()
        assertThat(viewModel.fuelConsumptionChart.value.map { it.time })
            .containsExactly(february, march)
            .inOrder()
        // The stored events did not change, so the repository must not have been queried again.
        coVerify(exactly = 2) { refuelRepository.getAllWithinTime(any(), any()) }
    }

    // endregion

    // region Maintenance data loading

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadMaintenanceData collects data and clears loading state`() = runTest {
        val service = MaintenanceServiceType(id = 1, serviceName = "Oil change")
        val data = listOf(maintenanceEvent(BigDecimal("50"), service))
        every { maintenanceRepository.getAllWithinTime(any(), any()) } returns flowOf(data)

        viewModel.loadMaintenanceData(RANGE_START, RANGE_END)

        advanceUntilIdle()

        assertThat(viewModel.maintenanceData.value).isEqualTo(data)
        assertThat(viewModel.isLoading.value).isFalse()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadMaintenanceData does nothing when end is null`() = runTest {
        // The init block already triggered one call; a null-bound call must not add another.
        viewModel.loadMaintenanceData(RANGE_START, null)

        advanceUntilIdle()

        coVerify(exactly = 1) { maintenanceRepository.getAllWithinTime(any(), any()) }
        assertThat(viewModel.maintenanceData.value).isEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadMaintenanceData does nothing when start is null`() = runTest {
        // The init block already triggered one call; a null-bound call must not add another.
        viewModel.loadMaintenanceData(null, RANGE_END)

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

        viewModel.loadMaintenanceData(RANGE_START, RANGE_END)

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

        viewModel.loadInspectionData(RANGE_START, RANGE_END)

        advanceUntilIdle()

        assertThat(viewModel.inspectionData.value).isEqualTo(data)
        assertThat(viewModel.inspectionCount.value).isEqualTo(2)
        assertThat(viewModel.isLoading.value).isFalse()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadInspectionData does nothing when end is null`() = runTest {
        // The init block already triggered one call; a null-bound call must not add another.
        viewModel.loadInspectionData(RANGE_START, null)

        advanceUntilIdle()

        coVerify(exactly = 1) { inspectionRepository.getAllWithinTime(any(), any()) }
        assertThat(viewModel.inspectionData.value).isEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadInspectionData does nothing when start is null`() = runTest {
        // The init block already triggered one call; a null-bound call must not add another.
        viewModel.loadInspectionData(null, RANGE_END)

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

        viewModel.loadInspectionData(RANGE_START, RANGE_END)

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

    @OptIn(ExperimentalCoroutinesApi::class)
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

