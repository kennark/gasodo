package com.gasodoapp.gasodo.feature.refuel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.database.repository.RefuelRepository
import com.gasodoapp.gasodo.core.database.repository.SavedLocationRepository
import com.gasodoapp.gasodo.core.expansion.collapse
import com.gasodoapp.gasodo.core.expansion.collapseAll
import com.gasodoapp.gasodo.core.expansion.toggleExpanded
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
        _expandedIds.collapse(event.id)
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
        _expandedIds.collapse(ids)
        viewModelScope.launch {
            refuelRepository.deleteByIds(ids)
        }
    }

    // endregion

    // region Expansion

    private val _expandedIds = MutableStateFlow<Set<UUID>>(emptySet())

    /**
     * Which events currently show their extra details. Held here rather than in the row composable
     * so an expanded card stays open while it is scrolled out of the viewport and back.
     */
    val expandedIds: StateFlow<Set<UUID>> = _expandedIds.asStateFlow()

    /**
     * Opens [id] if it is closed and closes it if it is open.
     */
    fun onToggleExpanded(id: UUID) {
        _expandedIds.toggleExpanded(id)
    }

    /**
     * Closes every expanded card. Called when the screen goes away so coming back starts collapsed.
     */
    fun onCollapseAll() {
        _expandedIds.collapseAll()
    }

    // endregion
}

/**
 * Selection state of the refuel list.
 */
typealias RefuelSelection = SelectionState<UUID>