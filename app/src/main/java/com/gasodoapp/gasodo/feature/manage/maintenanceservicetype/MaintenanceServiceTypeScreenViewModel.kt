package com.gasodoapp.gasodo.feature.manage.maintenanceservicetype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.core.database.repository.MaintenanceServiceTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Reason why the name typed into the editor cannot be saved.
 *
 * The enum carries no user facing text, so the ViewModel stays free of `Context` lookups and
 * validation remains a pure function. The wording lives in the UI layer via [message].
 */
enum class ServiceTypeNameError {
    /** The name is blank once trimmed. */
    EMPTY,

    /** Another service type already uses this name (case insensitive). */
    DUPLICATE,
}

/**
 * User facing message for a [ServiceTypeNameError].
 */
fun ServiceTypeNameError.message(): String = when (this) {
    ServiceTypeNameError.EMPTY -> "Enter a name"
    ServiceTypeNameError.DUPLICATE -> "A service with this name already exists"
}

/**
 * State of the create/edit dialog.
 *
 * @property id identifier of the edited row, or `0L` when a new type is being created.
 */
data class ServiceTypeEditor(
    val id: Long = 0L,
    val name: String = "",
    val notes: String = "",
    val nameError: ServiceTypeNameError? = null,
) {
    val isNew: Boolean
        get() = id == 0L
}

/**
 * A delete request waiting for confirmation.
 *
 * @property usageCount number of maintenance events referencing the type, or `null` while it is
 *   still being looked up.
 */
data class PendingServiceTypeDelete(
    val target: MaintenanceServiceType,
    val usageCount: Int? = null,
)

/**
 * Manages the user defined [MaintenanceServiceType] catalogue: listing, filtering, creating,
 * editing and deleting.
 */
@HiltViewModel
class MaintenanceServiceTypeScreenViewModel @Inject constructor(
    private val repository: MaintenanceServiceTypeRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _editor = MutableStateFlow<ServiceTypeEditor?>(null)
    val editor: StateFlow<ServiceTypeEditor?> = _editor.asStateFlow()

    private val _pendingDelete = MutableStateFlow<PendingServiceTypeDelete?>(null)
    val pendingDelete: StateFlow<PendingServiceTypeDelete?> = _pendingDelete.asStateFlow()

    /**
     * Every known service type, kept hot because [onEditorConfirm] validates new names against it
     * regardless of whether the list is currently on screen.
     */
    private val allServiceTypes: StateFlow<List<MaintenanceServiceType>> =
        repository.getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList(),
            )

    /**
     * Service types matching [searchQuery], ordered alphabetically and case insensitively.
     */
    val visibleServiceTypes: StateFlow<List<MaintenanceServiceType>> =
        combine(allServiceTypes, _searchQuery) { items, query ->
            items.asSequence()
                .filter { it.serviceName.contains(query, ignoreCase = true) }
                .sortedBy { it.serviceName.lowercase() }
                .toList()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun onSearchQueryChange(value: String) {
        _searchQuery.value = value
    }

    fun onCreateClick() {
        _editor.value = ServiceTypeEditor()
    }

    fun onEditClick(item: MaintenanceServiceType) {
        _editor.value = ServiceTypeEditor(
            id = item.id,
            name = item.serviceName,
            notes = item.notes.orEmpty()
        )
    }

    fun onEditorDismiss() {
        _editor.value = null
    }

    fun onEditorNameChange(value: String) {
        _editor.update { current ->
            // Clear a stale error as soon as the user starts fixing the name.
            current?.copy(name = value, nameError = null)
        }
    }

    fun onEditorNotesChange(value: String) {
        _editor.update { current -> current?.copy(notes = value) }
    }

    /**
     * Validates and persists the editor contents, then closes the dialog.
     *
     * Does nothing but set [ServiceTypeEditor.nameError] when the name is blank or taken by
     * another entry.
     */
    fun onEditorConfirm() {
        val current = _editor.value ?: return
        val name = current.name.trim()

        if (name.isBlank()) {
            _editor.value = current.copy(nameError = ServiceTypeNameError.EMPTY)
            return
        }

        viewModelScope.launch {
            // Read the list straight from the source of truth instead of the cached state so the
            // duplicate check is correct even before the list has been rendered.
            val isDuplicate = repository.getAll().first().any { existing ->
                existing.id != current.id && existing.serviceName.equals(name, ignoreCase = true)
            }
            if (isDuplicate) {
                _editor.value = current.copy(nameError = ServiceTypeNameError.DUPLICATE)
                return@launch
            }

            val notes = current.notes.trim().ifEmpty { null }

            if (current.isNew) {
                repository.insert(
                    MaintenanceServiceType(serviceName = name, notes = notes)
                )
            } else {
                val existing = repository.getById(current.id) ?: return@launch
                repository.update(
                    existing.copy(serviceName = name, notes = notes)
                )
            }
            _editor.value = null
        }
    }

    /**
     * Opens the delete confirmation dialog. The usage count is resolved asynchronously so the
     * user can be warned that past maintenance events will lose this service.
     */
    fun onDeleteClick(item: MaintenanceServiceType) {
        _pendingDelete.value = PendingServiceTypeDelete(target = item, usageCount = null)

        viewModelScope.launch {
            val count = repository.countUsage(item.id)
            _pendingDelete.update { current ->
                // Only fill in the count if the same item is still pending.
                if (current?.target?.id == item.id) current.copy(usageCount = count) else current
            }
        }
    }

    fun onDeleteDismiss() {
        _pendingDelete.value = null
    }

    /**
     * Deletes [target] and closes the confirmation dialog.
     *
     * The target is passed in rather than read from [pendingDelete] because the confirm button of
     * [com.gasodoapp.gasodo.ui.components.DeleteDialog] dismisses the dialog before confirming,
     * which clears [pendingDelete] first.
     */
    fun onDeleteConfirm(target: MaintenanceServiceType) {
        _pendingDelete.value = null

        viewModelScope.launch {
            repository.delete(target)
        }
    }
}
