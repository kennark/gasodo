package com.gasodoapp.gasodo.feature.overview

import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.getSelectedEndDate
import androidx.compose.material3.getSelectedStartDate
import androidx.compose.material3.setSelection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import com.gasodoapp.gasodo.core.database.junctions.InspectionEventWithParts
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import com.gasodoapp.gasodo.core.database.repository.InspectionRepository
import com.gasodoapp.gasodo.core.database.repository.MaintenanceRepository
import com.gasodoapp.gasodo.core.database.repository.RefuelRepository
import com.gasodoapp.gasodo.core.enums.DefaultTimePeriod
import com.gasodoapp.gasodo.core.enums.InspectionStatus
import com.gasodoapp.gasodo.core.enums.units.FuelConsumption
import com.gasodoapp.gasodo.core.settings.AppSettings
import com.gasodoapp.gasodo.core.settings.SettingsRepository
import com.gasodoapp.gasodo.core.settings.Units
import com.gasodoapp.gasodo.core.utils.BigDecimalUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OverviewScreenViewModel @Inject constructor(
    private val refuelRepository: RefuelRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val inspectionRepository: InspectionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    /**
     * Unit preferences, used for the suffixes rendered next to every statistic.
     */
    val units: StateFlow<Units> =
        settingsRepository.units
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = Units(),
            )

    /**
     * Refuels of the selected period.
     *
     * Kept only so the derived figures can be recomputed when the fuel consumption unit changes: the
     * stored events do not change then, so the repository flow does not emit again. The screen reads
     * [refuelStatistics] instead.
     */
    private val _refuelData = MutableStateFlow<List<RefuelEvent>>(emptyList())

    /**
     * Everything the refuel card shows for the selected period.
     */
    private val _refuelStatistics = MutableStateFlow(RefuelStatistics())
    val refuelStatistics: StateFlow<RefuelStatistics> = _refuelStatistics.asStateFlow()

    /**
     * Everything the maintenance card shows for the selected period.
     */
    private val _maintenanceStatistics = MutableStateFlow(MaintenanceStatistics())
    val maintenanceStatistics: StateFlow<MaintenanceStatistics> =
        _maintenanceStatistics.asStateFlow()

    /**
     * Everything the inspection card shows for the selected period.
     */
    private val _inspectionStatistics = MutableStateFlow(InspectionStatistics())
    val inspectionStatistics: StateFlow<InspectionStatistics> =
        _inspectionStatistics.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Range of the statistics currently on screen.
     *
     * The picker starts out empty on purpose: the range depends on the stored default, which is read
     * asynchronously, so [applyDefaultTimePeriod] does the initial selection once it is known. Until
     * then the screen shows its "period not set" state instead of flashing a range that is about to
     * be replaced.
     */
    val dateRangePickerState = DateRangePickerState(
        locale = CalendarLocale.getDefault(),
    )

    init {
        applyDefaultTimePeriod()
        recalculateOnFuelConsumptionChange()
    }

    /**
     * Selects the stored default period and loads the data it covers.
     *
     * Only the first emission is read: the picker belongs to this screen instance, so a change made
     * in the settings later on applies to the next visit rather than yanking the range from under a
     * user who is already looking at their statistics.
     */
    private fun applyDefaultTimePeriod() {
        viewModelScope.launch {
            val period = settingsRepository.settings
                .catch { _ -> emit(AppSettings()) }
                .map { it.overviewDefaultTimePeriod }
                .first()

            selectTimePeriod(period)
        }
    }

    /**
     * Moves the picker to the range [period] covers today and loads the data for it.
     */
    private fun selectTimePeriod(period: DefaultTimePeriod) {
        val range = period.dateRange()
        dateRangePickerState.setSelection(range.start, range.endInclusive)
        loadDataForSelectedRange()
    }

    fun onDateSelected() {
        loadDataForSelectedRange()
    }

    /**
     * Recomputes the fuel figures when the user switches the fuel consumption unit.
     *
     * The stored events do not change, so the repository flow does not emit again. Without this the
     * screen would keep showing a value computed with the previous unit until the data is reloaded.
     */
    private fun recalculateOnFuelConsumptionChange() {
        viewModelScope.launch {
            units
                .drop(1)
                .map { it.fuelConsumption }
                .distinctUntilChanged()
                .collect { recalculateRefuelFigures(_refuelData.value) }
        }
    }

    /**
     * Loads all three event types for the range currently selected in the date picker.
     *
     * The picker works in epoch milliseconds, so the conversion to [LocalDate] happens here, at the
     * boundary, and everything downstream works with days.
     */
    private fun loadDataForSelectedRange() {
        val start = dateRangePickerState.getSelectedStartDate()
        val end = dateRangePickerState.getSelectedEndDate()

        loadRefuelData(start, end)
        loadMaintenanceData(start, end)
        loadInspectionData(start, end)
    }

    internal fun loadRefuelData(start: LocalDate?, end: LocalDate?) {
        viewModelScope.launch {
            if (start != null && end != null) {
                _isLoading.value = true
                refuelRepository.getAllWithinTime(start, end)
                    .catch { _ ->
                        _refuelData.value = emptyList()
                        recalculateRefuelFigures(emptyList())
                    }
                    .collect { data ->
                        _refuelData.value = data
                        recalculateRefuelFigures(data)
                        _isLoading.value = false
                    }
            }
        }
    }

    /**
     * Recomputes every refuel derived value from [data].
     *
     * Both the card statistics and the chart depend on [Units.fuelConsumption], so the two are
     * always refreshed together to keep the chart from drifting out of sync with the figures
     * rendered next to its button.
     */
    private fun recalculateRefuelFigures(data: List<RefuelEvent>) {
        calculateRefuelStatistics(data)
        calculateDetailedFuelConsumptionChart(data)
    }

    internal fun calculateRefuelStatistics(data: List<RefuelEvent>) {
        if (data.isEmpty()) {
            updateRefuelStatistics(RefuelStatistics())
            return
        }

        val totals = RefuelStatistics(
            eventCount = data.size,
            totalCost = data.sumOf { event -> event.totalCost ?: BigDecimal.ZERO },
            totalLiters = data.sumOf { event -> event.amountLiters ?: BigDecimal.ZERO },
            averagePricePerLiter = calculateAveragePricePerLiter(data),
            // A single event covers no distance, even when it carries a mileage.
            totalMileage = calculateTotalMileage(data).takeIf { data.size > 1 },
        )

        updateRefuelStatistics(withConsumptionFigures(totals, data))
    }

    /**
     * Replaces the card figures while carrying the chart over.
     *
     * The chart is produced by [calculateDetailedFuelConsumptionChart] from the same events, so
     * recomputing the figures must not drop it. Both are always triggered together because they
     * depend on the same [Units.fuelConsumption].
     */
    private fun updateRefuelStatistics(statistics: RefuelStatistics) {
        _refuelStatistics.update { current ->
            statistics.copy(fuelConsumptionChart = current.fuelConsumptionChart)
        }
    }

    /**
     * Fills the consumption figures of [statistics] in, leaving them unset when the events do not
     * delimit a measurable interval.
     */
    private fun withConsumptionFigures(
        statistics: RefuelStatistics,
        data: List<RefuelEvent>,
    ): RefuelStatistics {
        val eventsBetweenFullFills = eventsBetweenFullFills(data)
        if (eventsBetweenFullFills.size <= 1) {
            return statistics
        }

        val fullFillMileage =
            eventsBetweenFullFills.last().base.mileage!! - eventsBetweenFullFills.first().base.mileage!!

        // A zero distance between the two full fill-ups (two entries on the same mileage) would
        // make the divisions below blow up, so both figures are left unset.
        if (fullFillMileage <= 0) {
            return statistics
        }

        return statistics.copy(
            fuelConsumption = calculateFuelConsumption(
                eventsBetweenFullFills,
                fullFillMileage,
                units.value.fuelConsumption
            ),
            fuelCost = calculateFuelCost(eventsBetweenFullFills, fullFillMileage),
        )
    }

    /**
     * Mean price paid per liquid unit, over the events that actually carry a price.
     *
     * Returns `null` when no event has a price, so the UI can render its "no data" state instead
     * of a meaningless zero.
     */
    private fun calculateAveragePricePerLiter(data: List<RefuelEvent>): BigDecimal? {
        val pricedEvents = data.filter { event -> event.pricePerLiter != null }
        if (pricedEvents.isEmpty()) {
            return null
        }

        return pricedEvents.sumOf { event -> event.pricePerLiter!! }
            .divide(
                BigDecimal(pricedEvents.size),
                2,
                BigDecimalUtils.ROUNDING_MODE
            )
    }

    /**
     * Distance covered between the highest and the lowest mileage of the range.
     *
     * The repository already returns the events sorted by mileage descending, but the extremes are
     * resolved by comparison so the result does not depend on that ordering.
     */
    private fun calculateTotalMileage(data: List<RefuelEvent>): Long? {
        val mileages = data.mapNotNull { event -> event.base.mileage }
        if (mileages.isEmpty()) {
            return null
        }

        return mileages.max() - mileages.min()
    }

    /**
     * Events between the first and the last full fill-up, both included and in ascending mileage
     * order.
     *
     * Consumption can only be measured between two full fill-ups, so anything outside that window
     * is dropped. Every event between them carries a mileage and an amount, because an event without
     * them cannot be part of a full fill-up in the first place. Nothing is returned when fewer than
     * two such events exist.
     */
    private fun eventsBetweenFullFills(data: List<RefuelEvent>): List<RefuelEvent> {
        val firstFullFill = data.indexOfFirst { event -> event.canAnchorInterval() }
        val lastFullFill = data.indexOfLast { event -> event.canAnchorInterval() }

        // indexOfFirst returns -1 when no full fill-up exists, indexOfLast returns -1 as well.
        if (firstFullFill == -1 || lastFullFill == -1 || lastFullFill <= firstFullFill) {
            return emptyList()
        }

        return data.subList(firstFullFill, lastFullFill + 1)
            .sortedBy { event -> event.base.mileage }
    }

    /**
     * Whether this event can open or close a measured interval.
     *
     * A consumption figure needs two full fill-ups and the mileage and volume between them, so an
     * event missing any of those cannot take part. Shared by [eventsBetweenFullFills] and
     * [calculateDetailedFuelConsumptionChart] so the card and the chart always measure the same
     * fill-ups.
     */
    private fun RefuelEvent.canAnchorInterval(): Boolean =
        fullFillUp && base.mileage != null && amountLiters != null

    /**
     * Fuel burnt over the distance of the full fill-ups, in the unit chosen in the settings.
     *
     * The first full fill-up only serves as the starting point of the measurement, so only the
     * liquid of the events after it is counted.
     *
     * `L/100 km` is the liquid per hundred distance units, `MPG` is its inverted ratio, which is why
     * both are produced from the same volume and distance. Returns `null` when any event is missing
     * an amount, so the UI falls back to its "no data" state.
     */
    private fun calculateFuelConsumption(
        eventsBetweenFullFills: List<RefuelEvent>,
        fullFillMileage: Long,
        fuelConsumption: FuelConsumption,
    ): BigDecimal? {
        if (eventsBetweenFullFills.any { event -> event.amountLiters == null }) {
            return null
        }

        val fullFillAmount = eventsBetweenFullFills
            .subList(1, eventsBetweenFullFills.size)
            .sumOf { event -> event.amountLiters!! }

        return fuelConsumption.convertConsumption(fullFillAmount, BigDecimal(fullFillMileage))
    }

    /**
     * Cost of the fuel bought between the full fill-ups, per hundred distance units.
     *
     * The unit is fixed rather than following [Units.fuelConsumption]: the label the UI renders is
     * [Units.costPerDistanceLabel] (`€/100 km`), so inverting this ratio would contradict it.
     * Returns `null` when any event is missing a cost.
     */
    private fun calculateFuelCost(
        eventsBetweenFullFills: List<RefuelEvent>,
        fullFillMileage: Long,
    ): BigDecimal? {
        if (eventsBetweenFullFills.any { event -> event.totalCost == null }) {
            return null
        }

        val fullFillCost = eventsBetweenFullFills
            .subList(1, eventsBetweenFullFills.size)
            .sumOf { event -> event.totalCost!! }

        return fullFillCost
            .divide(BigDecimal(fullFillMileage), BigDecimalUtils.CONTEXT)
            .multiply(BigDecimal(100))
            .setScale(2, BigDecimalUtils.ROUNDING_MODE)
    }

    /**
     * Turns a volume and a distance into a consumption figure for [this] unit.
     *
     * [volume] and [distance] must describe the same stretch of road, so the two branches only
     * differ by the ratio they build from them.
     */
    private fun FuelConsumption.convertConsumption(
        volume: BigDecimal,
        distance: BigDecimal,
    ): BigDecimal = when (this) {
        // Liquid burnt per hundred distance units: volume / distance * 100.
        FuelConsumption.LIQUID_PER_100_DISTANCE -> volume
            .divide(distance, BigDecimalUtils.CONTEXT)
            .multiply(BigDecimal(100))

        // Distance covered per liquid unit, the inverse ratio: distance / volume.
        FuelConsumption.DISTANCE_PER_LIQUID -> distance
            .divide(volume, BigDecimalUtils.CONTEXT)
    }.setScale(2, BigDecimalUtils.ROUNDING_MODE)


    internal fun loadMaintenanceData(start: LocalDate?, end: LocalDate?) {
        viewModelScope.launch {
            if (start != null && end != null) {
                _isLoading.value = true
                maintenanceRepository.getAllWithinTime(start, end)
                    .catch { _ ->
                        calculateMaintenanceStatistics(emptyList())
                    }
                    .collect { data ->
                        calculateMaintenanceStatistics(data)
                        _isLoading.value = false
                    }
            }
        }
    }

    internal fun calculateMaintenanceStatistics(data: List<MaintenanceEventWithServices>) {
        val actions = data.flatMap { event -> event.services }

        _maintenanceStatistics.value = MaintenanceStatistics(
            eventCount = data.size,
            totalCost = data.sumOf { event -> event.event.totalCost ?: BigDecimal.ZERO },
            actionCount = actions.size,
            topActions = actions
                .groupBy { service -> service.serviceName }
                .map { (_, services) -> TopMaintenanceAction(services.first(), services.size) }
                .sortedByDescending { action -> action.count }
                .take(3),
        )
    }

    internal fun loadInspectionData(start: LocalDate?, end: LocalDate?) {
        viewModelScope.launch {
            if (start != null && end != null) {
                _isLoading.value = true
                inspectionRepository.getAllWithinTime(start, end)
                    .catch { _ ->
                        calculateInspectionStatistics(emptyList())
                    }
                    .collect { data ->
                        calculateInspectionStatistics(data)
                        _isLoading.value = false
                    }
            }
        }
    }

    internal fun calculateInspectionStatistics(data: List<InspectionEventWithParts>) {
        val totalCount = data.size

        if (totalCount == 0) {
            _inspectionStatistics.value = InspectionStatistics()
            return
        }

        val passCount = data.count { it.event.status == InspectionStatus.PASS }
        val failCount = data.count { it.event.status == InspectionStatus.FAIL }
        val conditionalPassCount =
            data.count { it.event.status == InspectionStatus.CONDITIONAL_PASS }

        _inspectionStatistics.value = InspectionStatistics(
            totalCount = totalCount,
            passCount = passCount,
            failCount = failCount,
            conditionalPassCount = conditionalPassCount,
            passPercentage = percentage(passCount, totalCount),
            failPercentage = percentage(failCount, totalCount),
            conditionalPassPercentage = percentage(conditionalPassCount, totalCount),
        )
    }

    private fun percentage(count: Int, total: Int): BigDecimal =
        BigDecimal(count)
            .multiply(BigDecimal(100))
            .divide(BigDecimal(total), 2, BigDecimalUtils.ROUNDING_MODE)

    /**
     * Builds one [TimelineDataPoint] per interval between two consecutive full fill-ups.
     *
     * An interval opens at a full fill-up and closes at the next one, and only the volume refuelled
     * after the opening point is counted, because the tank was already full when the interval
     * started. Intervals that contain an event without an amount, that cover no distance, or that
     * cannot be dated are skipped rather than zero-filled: the resulting number would not describe
     * anything real, so no point is emitted and the chart simply joins the surviving ones.
     *
     * Each point is dated to the closing fill-up, the moment its consumption is realised, and the
     * list is ordered by ascending date so the renderer can join consecutive points.
     */
    internal fun calculateDetailedFuelConsumptionChart(data: List<RefuelEvent>) {
        _refuelStatistics.update { current ->
            current.copy(fuelConsumptionChart = buildDataPoints(data))
        }
    }

    /**
     * The points of the consumption chart for [data], ordered by ascending date.
     *
     * Empty when no interval can be measured, so a period that yields nothing drops the previous
     * series instead of keeping it: rendering old data as if it belonged to the new range is worse
     * than rendering no curve at all.
     */
    private fun buildDataPoints(data: List<RefuelEvent>): List<TimelineDataPoint> {
        if (data.size < 2) {
            return emptyList()
        }

        // An event without a mileage cannot be placed between two fill-ups, so it takes no part in
        // the intervals. The repository returns the events by descending mileage, so re-sort.
        val events = data
            .filter { event -> event.base.mileage != null }
            .sortedBy { event -> event.base.mileage }

        val points = mutableListOf<TimelineDataPoint>()
        var index = 0

        while (index < events.size) {
            val start = events[index]
            if (!start.canAnchorInterval()) {
                index++
                continue
            }

            val endIndex = (index + 1 until events.size).firstOrNull { candidate ->
                events[candidate].canAnchorInterval()
            }

            // The last full fill-up closes the previous interval but opens nothing on its own.
            if (endIndex == null) {
                break
            }

            createDataPoint(events.subList(index, endIndex + 1))?.let(points::add)

            // Continue from the closing fill-up, which is the opening one of the next interval.
            index = endIndex
        }

        // Intervals are measured in mileage order because that is what the ratio is built from, but
        // the renderer draws them on a timeline and joins them in list order. The two orders only
        // coincide when the driver logs entries as they go, so sort by date to keep a late or
        // back-dated entry from bending the curve backwards on itself.
        return points.sortedBy { point -> point.time }
    }

    /**
     * Turns one interval into a plottable point, or `null` when the interval cannot be measured.
     *
     * [interval] spans from the opening full fill-up to the closing one, both included, and is
     * already ordered by ascending mileage.
     */
    private fun createDataPoint(interval: List<RefuelEvent>): TimelineDataPoint? {
        val start = interval.first()
        val end = interval.last()

        // Every event in the window contributes its volume, so a single missing amount makes the
        // whole figure unreliable.
        if (interval.any { event -> event.amountLiters == null }) {
            return null
        }

        val distance = end.base.mileage!! - start.base.mileage!!

        // Two fill-ups logged at the same mileage describe no driven distance, which would make
        // the ratio either infinite or meaningless.
        if (distance <= 0) {
            return null
        }

        val date = end.base.date ?: return null

        val volume = interval
            .drop(1)
            .sumOf { event -> event.amountLiters!! }

        val value = units.value.fuelConsumption
            .convertConsumption(volume, BigDecimal(distance))

        return TimelineDataPoint(time = date, value = value)
    }
}