package com.gasodoapp.gasodo.feature.refuel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.database.repository.RefuelRepository
import com.gasodoapp.gasodo.core.database.repository.SavedLocationRepository
import com.gasodoapp.gasodo.core.settings.SettingsRepository
import com.gasodoapp.gasodo.core.settings.Units
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RefuelScreenViewModel @Inject constructor(
    private val refuelRepository: RefuelRepository,
    private val locationRepository: SavedLocationRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val pagedItems: Flow<PagingData<RefuelEvent>> =
        refuelRepository.getAllByDatePaged()
            .cachedIn(viewModelScope)

    /**
     * Unit preferences, used for the suffixes rendered next to mileage, volumes and amounts.
     */
    val units: StateFlow<Units> =
        settingsRepository.units
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = Units(),
            )

    fun onDeleteEvent(event: RefuelEvent) {
        viewModelScope.launch {
            refuelRepository.delete(event)
        }
    }

    suspend fun getSavedLocation(id: UUID): SavedLocation? {
        return locationRepository.getById(id)
    }

    // region Multi-select

    private val _selection = MutableStateFlow(RefuelSelection())

    /**
     * Which events are currently selected, and whether selection mode is on.
     */
    val selection: StateFlow<RefuelSelection> = _selection.asStateFlow()

    /**
     * Enters selection mode from the overflow menu, with nothing selected yet.
     */
    fun onSelectClick() {
        _selection.value = RefuelSelection(active = true)
    }

    /**
     * Enters selection mode if needed and selects [id]. Long pressing an already selected event
     * keeps it selected.
     */
    fun onEventLongPress(id: UUID) {
        _selection.update { current ->
            RefuelSelection(active = true, ids = current.ids + id)
        }
    }

    /**
     * Toggles [id] while selection mode is on. Outside selection mode this is a no-op so the card
     * keeps its default behaviour.
     */
    fun onEventTap(id: UUID) {
        _selection.update { current ->
            if (!current.active) return@update current

            val ids = if (id in current.ids) current.ids - id else current.ids + id
            // Deselecting the last event leaves selection mode, like other Material apps do.
            RefuelSelection(active = ids.isNotEmpty(), ids = ids)
        }
    }

    /**
     * Leaves selection mode and drops every selected id.
     */
    fun onClearSelection() {
        _selection.value = RefuelSelection()
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
            refuelRepository.deleteByIds(ids)
        }
    }

    // endregion
}

/**
 * Selection state of the refuel list.
 *
 * [active] is tracked separately from [ids] because selection mode can be entered from the overflow
 * menu before any event is picked.
 */
data class RefuelSelection(
    val active: Boolean = false,
    val ids: Set<UUID> = emptySet(),
) {
    val count: Int get() = ids.size
}