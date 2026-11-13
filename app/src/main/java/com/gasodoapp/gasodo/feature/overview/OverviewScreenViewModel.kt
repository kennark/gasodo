package com.gasodoapp.gasodo.feature.overview

import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.getSelectedEndDate
import androidx.compose.material3.getSelectedStartDate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import com.gasodoapp.gasodo.core.database.junctions.InspectionEventWithParts
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import com.gasodoapp.gasodo.core.database.repository.InspectionRepository
import com.gasodoapp.gasodo.core.database.repository.MaintenanceRepository
import com.gasodoapp.gasodo.core.database.repository.RefuelRepository
import com.gasodoapp.gasodo.core.enums.InspectionStatus
import com.gasodoapp.gasodo.core.enums.units.FuelConsumption
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OverviewScreenViewModel @Inject constructor(
    private val refuelRepository: RefuelRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val inspectionRepository: InspectionRepository,
    settingsRepository: SettingsRepository
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

    // Refuel data for the selected period
    private val _refuelData = MutableStateFlow<List<RefuelEvent>>(emptyList())
    val refuelData: StateFlow<List<RefuelEvent>> = _refuelData.asStateFlow()

    private val _totalRefuelCost = MutableStateFlow<BigDecimal>(BigDecimal.ZERO)
    val totalRefuelCost: StateFlow<BigDecimal> = _totalRefuelCost.asStateFlow()

    private val _totalLiters = MutableStateFlow<BigDecimal>(BigDecimal.ZERO)
    val totalLiters: StateFlow<BigDecimal> = _totalLiters.asStateFlow()

    private val _totalMileage = MutableStateFlow<Long?>(null)
    val totalMileage = _totalMileage.asStateFlow()

    /**
     * Consumption over the distance between the first and the last full fill-up, expressed in the
     * unit picked in the settings: either liquid per hundred distance units (`L/100 km`) or
     * distance per liquid unit (`mi/gal`). Both are computed from the same events, only the ratio
     * is inverted, which is why a single field can serve both.
     */
    private val _fuelConsumption = MutableStateFlow<BigDecimal?>(null)
    val fuelConsumption = _fuelConsumption.asStateFlow()

    /**
     * Cost of the fuel burnt over a hundred distance units (for example `€/100 km`).
     *
     * It deliberately ignores [Units.fuelConsumption]: the label rendered next to it
     * ([Units.costPerDistanceLabel]) always reads as a cost per hundred distance units.
     */
    private val _fuelCost = MutableStateFlow<BigDecimal?>(null)
    val fuelCost = _fuelCost.asStateFlow()

    private val _averagePricePerLiter = MutableStateFlow<BigDecimal?>(null)
    val averagePricePerLiter = _averagePricePerLiter.asStateFlow()

    private val _maintenanceData = MutableStateFlow<List<MaintenanceEventWithServices>>(emptyList())
    val maintenanceData = _maintenanceData.asStateFlow()

    private val _totalMaintenanceCost = MutableStateFlow<BigDecimal>(BigDecimal.ZERO)
    val totalMaintenanceCost = _totalMaintenanceCost.asStateFlow()

    private val _maintenanceActions = MutableStateFlow<List<MaintenanceServiceType>>(emptyList())
    val maintenanceActions = _maintenanceActions.asStateFlow()

    private val _topMaintenanceActions = MutableStateFlow<List<TopMaintenanceAction>>(emptyList())
    val topMaintenanceActions = _topMaintenanceActions.asStateFlow()

    private val _inspectionData = MutableStateFlow<List<InspectionEventWithParts>>(emptyList())
    val inspectionData = _inspectionData.asStateFlow()

    private val _inspectionCount = MutableStateFlow(0)
    val inspectionCount = _inspectionCount.asStateFlow()

    private val _passCount = MutableStateFlow(0)
    val passCount = _passCount.asStateFlow()

    private val _failCount = MutableStateFlow(0)
    val failCount = _failCount.asStateFlow()

    private val _conditionalPassCount = MutableStateFlow(0)
    val conditionalPassCount = _conditionalPassCount.asStateFlow()

    private val _passPercentage = MutableStateFlow(BigDecimal.ZERO)
    val passPercentage = _passPercentage.asStateFlow()

    private val _failPercentage = MutableStateFlow(BigDecimal.ZERO)
    val failPercentage = _failPercentage.asStateFlow()

    private val _conditionalPassPercentage = MutableStateFlow(BigDecimal.ZERO)
    val conditionalPassPercentage = _conditionalPassPercentage.asStateFlow()


    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val dateRangePickerState = DateRangePickerState(
        locale = CalendarLocale.getDefault(),
        initialSelectedStartDate = LocalDate.of(
            LocalDate.now().year,
            LocalDate.now().monthValue,
            1
        ),
        initialSelectedEndDate = LocalDate.now()
    )

    init {
        loadDataForSelectedRange()
        recalculateOnFuelConsumptionChange()
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
                .collect { calculateRefuelStatistics(_refuelData.value) }
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
                    }
                    .collect { data ->
                        _refuelData.value = data
                        calculateRefuelStatistics(_refuelData.value)
                        _isLoading.value = false
                    }
            }
        }
    }


    internal fun calculateRefuelStatistics(data: List<RefuelEvent>) {
        if (data.isEmpty()) {
            return
        }

        _totalRefuelCost.value = data.sumOf { it.totalCost ?: BigDecimal.ZERO }
        _totalLiters.value = data.sumOf { it.amountLiters ?: BigDecimal.ZERO }
        _averagePricePerLiter.value = calculateAveragePricePerLiter(data)

        if (data.size <= 1) {
            return
        }

        _totalMileage.value = calculateTotalMileage(data)

        val eventsBetweenFullFills = eventsBetweenFullFills(data)
        if (eventsBetweenFullFills.size <= 1) {
            return
        }

        val fullFillMileage =
            eventsBetweenFullFills.last().base.mileage!! - eventsBetweenFullFills.first().base.mileage!!

        // A zero distance between the two full fill-ups (two entries on the same mileage) would
        // make the divisions below blow up, so both figures are left as they are.
        if (fullFillMileage <= 0) {
            return
        }

        _fuelConsumption.value = calculateFuelConsumption(
            eventsBetweenFullFills,
            fullFillMileage,
            units.value.fuelConsumption
        )
        _fuelCost.value = calculateFuelCost(eventsBetweenFullFills, fullFillMileage)
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
        val firstFullFill = data.indexOfFirst { event ->
            event.fullFillUp && event.base.mileage != null && event.amountLiters != null
        }
        val lastFullFill = data.indexOfLast { event ->
            event.fullFillUp && event.base.mileage != null && event.amountLiters != null
        }

        // indexOfFirst returns -1 when no full fill-up exists, indexOfLast returns -1 as well.
        if (firstFullFill == -1 || lastFullFill == -1 || lastFullFill <= firstFullFill) {
            return emptyList()
        }

        return data.subList(firstFullFill, lastFullFill + 1)
            .sortedBy { event -> event.base.mileage }
    }

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
                        _maintenanceData.value = emptyList()
                    }
                    .collect { data ->
                        _maintenanceData.value = data
                        calculateMaintenanceStatistics(_maintenanceData.value)
                        _isLoading.value = false
                    }
            }
        }
    }

    internal fun calculateMaintenanceStatistics(data: List<MaintenanceEventWithServices>) {
        if (data.isNotEmpty()) {
            _totalMaintenanceCost.value = data.sumOf { it.event.totalCost ?: BigDecimal.ZERO }
            _maintenanceActions.value = data.flatMap { it.services }

            _topMaintenanceActions.value = _maintenanceActions.value
                .groupBy { it.serviceName }
                .map { (_, services) -> TopMaintenanceAction(services.first(), services.size) }
                .sortedByDescending { it.count }
                .take(3)
        }
    }

    internal fun loadInspectionData(start: LocalDate?, end: LocalDate?) {
        viewModelScope.launch {
            if (start != null && end != null) {
                _isLoading.value = true
                inspectionRepository.getAllWithinTime(start, end)
                    .catch { _ ->
                        _inspectionData.value = emptyList()
                    }
                    .collect { data ->
                        _inspectionData.value = data
                        calculateInspectionStatistics(_inspectionData.value)
                        _isLoading.value = false
                    }
            }
        }
    }

    internal fun calculateInspectionStatistics(data: List<InspectionEventWithParts>) {
        val totalCount = data.size

        _inspectionCount.value = totalCount

        if (totalCount == 0) {
            _passCount.value = 0
            _failCount.value = 0
            _conditionalPassCount.value = 0
            _passPercentage.value = BigDecimal.ZERO
            _failPercentage.value = BigDecimal.ZERO
            _conditionalPassPercentage.value = BigDecimal.ZERO
            return
        }

        val passCount = data.count { it.event.status == InspectionStatus.PASS }
        val failCount = data.count { it.event.status == InspectionStatus.FAIL }
        val conditionalPassCount =
            data.count { it.event.status == InspectionStatus.CONDITIONAL_PASS }

        _passCount.value = passCount
        _failCount.value = failCount
        _conditionalPassCount.value = conditionalPassCount

        _passPercentage.value = percentage(passCount, totalCount)
        _failPercentage.value = percentage(failCount, totalCount)
        _conditionalPassPercentage.value = percentage(conditionalPassCount, totalCount)
    }

    private fun percentage(count: Int, total: Int): BigDecimal =
        BigDecimal(count)
            .multiply(BigDecimal(100))
            .divide(BigDecimal(total), 2, BigDecimalUtils.ROUNDING_MODE)
}
