package com.gasodoapp.gasodo.feature.inspection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.database.junctions.InspectionEventWithParts
import com.gasodoapp.gasodo.core.database.repository.InspectionRepository
import com.gasodoapp.gasodo.core.database.repository.SavedLocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InspectionScreenViewModel @Inject constructor(
    private val inspectionRepository: InspectionRepository,
    private val locationRepository: SavedLocationRepository
) : ViewModel() {

    val pagedItems: Flow<PagingData<InspectionEventWithParts>> =
        inspectionRepository.getAllByDatePaged()
            .cachedIn(viewModelScope)

    suspend fun getSavedLocation(id: UUID): SavedLocation? {
        return locationRepository.getById(id)
    }

    fun onDeleteInspectionEvent(eventWithParts: InspectionEventWithParts) {
        viewModelScope.launch {
            inspectionRepository.delete(eventWithParts.event)
        }
    }
}