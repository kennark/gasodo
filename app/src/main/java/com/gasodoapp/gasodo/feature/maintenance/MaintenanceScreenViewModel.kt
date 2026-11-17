package com.gasodoapp.gasodo.feature.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import com.gasodoapp.gasodo.core.database.repository.MaintenanceRepository
import com.gasodoapp.gasodo.core.database.repository.SavedLocationRepository
import com.gasodoapp.gasodo.core.selection.SelectionState
import com.gasodoapp.gasodo.core.selection.beginSelection
import com.gasodoapp.gasodo.core.selection.clearSelection
import com.gasodoapp.gasodo.core.selection.selectItem
import com.gasodoapp.gasodo.core.selection.toggleItem
import com.gasodoapp.gasodo.core.settings.SettingsRepository
import com.gasodoapp.gasodo.core.settings.Units
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MaintenanceScreenViewModel @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository,
    private val locationRepository: SavedLocationRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {
    val pagedItems: Flow<PagingData<MaintenanceEventWithServices>> =
        maintenanceRepository.getAllByDatePaged()
            .cachedIn(viewModelScope)

    /**
     * Unit preferences, used for the suffixes rendered next to mileage and costs.
     */
    val units: StateFlow<Units> =
        settingsRepository.units
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = Units(),
            )


    suspend fun getSavedLocation(id: UUID): SavedLocation? {
        return locationRepository.getById(id)
    }

    fun onDeleteMaintenanceEvent(eventWithServices: MaintenanceEventWithServices) {
        viewModelScope.launch {
            maintenanceRepository.delete(eventWithServices.event)
        }
    }

    // region Multi-select

    private val _selection = MutableStateFlow(MaintenanceSelection())

    /**
     * Which maintenance events are currently selected, and whether selection mode is on.
     */
    val selection: StateFlow<MaintenanceSelection> = _selection.asStateFlow()

    /**
     * Enters selection mode from the overflow menu, with nothing selected yet.
     */
    fun onSelectClick() {
        _selection.beginSelection()
    }

    /**
     * Enters selection mode if needed and selects [id]. Long pressing an already selected event
     * keeps it selected.
     */
    fun onEventLongPress(id: UUID) {
        _selection.selectItem(id)
    }

    /**
     * Toggles [id] while selection mode is on. Outside selection mode this is a no-op so the card
     * keeps its default behaviour.
     */
    fun onEventTap(id: UUID) {
        _selection.toggleItem(id)
    }

    /**
     * Leaves selection mode and drops every selected id.
     */
    fun onClearSelection() {
        _selection.clearSelection()
    }

    /**
     * Deletes every selected event and leaves selection mode.
     *
     * The ids are read once up front so a concurrent selection change cannot shrink the deletion
     * halfway through.
     */
    fun onDeleteSelected() {
        val ids = _selection.value.ids
        if (ids.isEmpty()) return

        onClearSelection()
        viewModelScope.launch {
            maintenanceRepository.deleteByIds(ids)
        }
    }

    // endregion
}

/**
 * Selection state of the maintenance event list.
 */
typealias MaintenanceSelection = SelectionState<UUID>