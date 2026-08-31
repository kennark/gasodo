package com.gasodoapp.gasodo.feature.refuel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.database.repository.RefuelRepository
import com.gasodoapp.gasodo.core.database.repository.SavedLocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RefuelScreenViewModel @Inject constructor(
    private val refuelRepository: RefuelRepository,
    private val locationRepository: SavedLocationRepository
) : ViewModel() {

    val pagedItems: Flow<PagingData<RefuelEvent>> =
        refuelRepository.getAllByDatePaged()
            .cachedIn(viewModelScope)

    fun onDeleteEvent(event: RefuelEvent) {
        viewModelScope.launch {
            refuelRepository.delete(event)
        }
    }

    suspend fun getSavedLocation(id: UUID): SavedLocation? {
        return locationRepository.getById(id)
    }
}