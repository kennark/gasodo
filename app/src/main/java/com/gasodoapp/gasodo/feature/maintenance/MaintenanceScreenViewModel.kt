package com.gasodoapp.gasodo.feature.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import com.gasodoapp.gasodo.core.database.repository.MaintenanceRepository
import com.gasodoapp.gasodo.core.database.repository.SavedLocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MaintenanceScreenViewModel @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository,
    private val locationRepository: SavedLocationRepository
) : ViewModel() {
    val pagedItems: Flow<PagingData<MaintenanceEventWithServices>> =
        maintenanceRepository.getAllByDatePaged()
            .cachedIn(viewModelScope)


    suspend fun getSavedLocation(id: UUID): SavedLocation? {
        return locationRepository.getById(id)
    }

    fun onDeleteMaintenanceEvent(eventWithServices: MaintenanceEventWithServices) {
        viewModelScope.launch {
            maintenanceRepository.delete(eventWithServices.event)
        }
    }
}