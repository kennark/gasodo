package com.gasodoapp.gasodo.feature.overview

import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.gasodoapp.gasodo.core.utils.BigDecimalUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OverviewScreenViewModel @Inject constructor(
    private val refuelRepository: RefuelRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val inspectionRepository: InspectionRepository
) : ViewModel() {

    // Refuel data for the selected period
    private val _refuelData = MutableStateFlow<List<RefuelEvent>>(emptyList())
    val refuelData: StateFlow<List<RefuelEvent>> = _refuelData.asStateFlow()

    private val _totalRefuelCost = MutableStateFlow<BigDecimal>(BigDecimal.ZERO)
    val totalRefuelCost: StateFlow<BigDecimal> = _totalRefuelCost.asStateFlow()

    private val _totalLiters = MutableStateFlow<BigDecimal>(BigDecimal.ZERO)
    val totalLiters: StateFlow<BigDecimal> = _totalLiters.asStateFlow()

    private val _totalMileage = MutableStateFlow<Long?>(null)
    val totalMileage = _totalMileage.asStateFlow()

    // L per 100km
    private val _fuelConsumption = MutableStateFlow<BigDecimal?>(null)
    val fuelConsumption = _fuelConsumption.asStateFlow()

    // € per 100km
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

    @OptIn(ExperimentalMaterial3Api::class)
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
        loadRefuelData(
            dateRangePickerState.selectedStartDateMillis,
            dateRangePickerState.selectedEndDateMillis
        )
        loadMaintenanceData(
            dateRangePickerState.selectedStartDateMillis,
            dateRangePickerState.selectedEndDateMillis
        )
        loadInspectionData(
            dateRangePickerState.selectedStartDateMillis,
            dateRangePickerState.selectedEndDateMillis
        )
    }

    fun onDateSelected() {
        loadRefuelData(
            dateRangePickerState.selectedStartDateMillis,
            dateRangePickerState.selectedEndDateMillis
        )
        loadMaintenanceData(
            dateRangePickerState.selectedStartDateMillis,
            dateRangePickerState.selectedEndDateMillis
        )
        loadInspectionData(
            dateRangePickerState.selectedStartDateMillis,
            dateRangePickerState.selectedEndDateMillis
        )
    }

    internal fun loadRefuelData(start: Long?, end: Long?) {
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
        if (data.isNotEmpty()) {
            _totalRefuelCost.value = data.sumOf { it.totalCost ?: BigDecimal.ZERO }
            _totalLiters.value = data.sumOf { it.amountLiters ?: BigDecimal.ZERO }
            _averagePricePerLiter.value = data.sumOf { it.pricePerLiter ?: BigDecimal.ZERO }
                .let {
                    if (it == BigDecimal.ZERO)
                        return else
                        it.divide(
                            BigDecimal(data.count { event -> event.pricePerLiter != null }),
                            2, BigDecimalUtils.ROUNDING_MODE
                        )
                }


            if (data.size > 1) {
                _totalMileage.value =
                    data.first { event -> event.base.mileage != null }.base.mileage?.minus(
                        data.last { event -> event.base.mileage != null }.base.mileage ?: 0
                    )

                val firstFullDataEntry =
                    data.indexOfFirst { event -> event.fullFillUp && event.base.mileage != null && event.amountLiters != null }
                val lastFullDataEntry =
                    data.indexOfLast { event -> event.fullFillUp && event.base.mileage != null && event.amountLiters != null }

                val eventsBetweenFullFills =
                    data.subList(firstFullDataEntry, lastFullDataEntry + 1)
                        .sortedBy { event -> event.base.mileage }

                if (eventsBetweenFullFills.size > 1) {
                    val fullFillMileage =
                        eventsBetweenFullFills.last().base.mileage!!.minus(
                            eventsBetweenFullFills.first().base.mileage!!
                        )
                    if (eventsBetweenFullFills.all { event -> event.amountLiters != null }) {
                        val fullFillAmount =
                            eventsBetweenFullFills.subList(1, eventsBetweenFullFills.size)
                                .sumOf { event -> event.amountLiters!! }
                        _fuelConsumption.value = fullFillAmount.divide(
                            BigDecimal(fullFillMileage),
                            BigDecimalUtils.CONTEXT
                        )
                            .multiply(
                                BigDecimal(100)
                            )
                            .setScale(
                                2,
                                BigDecimalUtils.ROUNDING_MODE
                            )
                    }
                    if (eventsBetweenFullFills.all { event -> event.totalCost != null }) {
                        val fullFillCost =
                            eventsBetweenFullFills.subList(1, eventsBetweenFullFills.size)
                                .sumOf { event -> event.totalCost!! }
                        _fuelCost.value = fullFillCost.divide(
                            BigDecimal(fullFillMileage),
                            BigDecimalUtils.CONTEXT
                        )
                            .multiply(
                                BigDecimal(100)
                            )
                            .setScale(
                                2,
                                BigDecimalUtils.ROUNDING_MODE
                            )
                    }
                }
            }
        }
    }

    internal fun loadMaintenanceData(start: Long?, end: Long?) {
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

    internal fun loadInspectionData(start: Long?, end: Long?) {
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
