package com.example.carrefueltracker.feature.overview

import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carrefueltracker.core.database.entity.RefuelEvent
import com.example.carrefueltracker.core.database.repository.RefuelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class OverviewScreenViewModel @Inject constructor(
    private val refuelRepository: RefuelRepository,
) : ViewModel() {

    // Refuel data for the selected period
    private val _refuelData = MutableStateFlow<List<RefuelEvent>>(emptyList())
    val refuelData: StateFlow<List<RefuelEvent>> = _refuelData.asStateFlow()

    private val _totalCost = MutableStateFlow<Double>(0.00)
    val totalCost: StateFlow<Double> = _totalCost.asStateFlow()

    private val _totalLiters = MutableStateFlow<Double>(0.00)
    val totalLiters: StateFlow<Double> = _totalLiters.asStateFlow()

    private val _mileageStatisticsCanBeCalculated = MutableStateFlow(false)
    val mileageStatisticsCanBeCalculated = _mileageStatisticsCanBeCalculated.asStateFlow()

    private val _totalMileage = MutableStateFlow(0L)
    val totalMileage = _totalMileage.asStateFlow()

    // L per 100km
    private val _fuelConsumption = MutableStateFlow(0.0)
    val fuelConsumption = _fuelConsumption.asStateFlow()

    // € per 100km
    private val _fuelCost = MutableStateFlow(0.0)
    val fuelCost = _fuelCost.asStateFlow()


    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    @OptIn(ExperimentalMaterial3Api::class)
    val dateRangePickerState = DateRangePickerState(
        locale = CalendarLocale.getDefault(),
        initialSelectedStartDate = LocalDate.of(Calendar.YEAR, Calendar.MONTH, 1),
        initialSelectedEndDate = LocalDate.now()
        )

    init {
        loadRefuelData(0, Long.MAX_VALUE)
    }

    fun onDateSelected(start: Long?, end: Long?) {
        if (start != null && end != null)
            loadRefuelData(start, end)
    }

    private fun loadRefuelData(start: Long, end: Long) {
        viewModelScope.launch {
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



    private fun calculateRefuelStatistics(data: List<RefuelEvent>) {
        if (data.isNotEmpty()) {
            _totalCost.value = data.sumOf { it.totalCost ?: 0.0 }
            _totalLiters.value = data.sumOf { it.amountLiters ?: 0.0 }

            if (data.size > 1) {
                _totalMileage.value =
                    data.first().base.mileage?.minus(data.last().base.mileage ?: 0) ?: 0
                _fuelConsumption.value = _totalLiters.value.div(_totalMileage.value).times(100)
                _fuelCost.value = _totalCost.value.div(_totalMileage.value).times(100)
                _mileageStatisticsCanBeCalculated.value = true
            } else {
                _mileageStatisticsCanBeCalculated.value = false
            }
        }
    }
}
