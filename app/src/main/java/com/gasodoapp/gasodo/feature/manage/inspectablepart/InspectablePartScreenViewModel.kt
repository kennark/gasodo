package com.gasodoapp.gasodo.feature.manage.inspectablepart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gasodoapp.gasodo.core.database.entity.InspectablePart
import com.gasodoapp.gasodo.core.database.repository.InspectablePartRepository
import com.gasodoapp.gasodo.ui.components.catalogue.CatalogueEditorState
import com.gasodoapp.gasodo.ui.components.catalogue.NameError
import com.gasodoapp.gasodo.ui.components.catalogue.PendingCatalogueDelete
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
 * Manages the user defined [InspectablePart] catalogue: listing, filtering, creating, editing and deleting.
 */
@HiltViewModel
class InspectablePartScreenViewModel @Inject constructor(
    private val repository: InspectablePartRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _editor = MutableStateFlow<CatalogueEditorState?>(null)
    val editor: StateFlow<CatalogueEditorState?> = _editor.asStateFlow()

    private val _pendingDelete = MutableStateFlow<PendingCatalogueDelete<InspectablePart>?>(null)
    val pendingDelete: StateFlow<PendingCatalogueDelete<InspectablePart>?> =
        _pendingDelete.asStateFlow()

    /**
     * Every known part, kept hot because [onEditorConfirm] validates new names against it regardless of whether the
     * list is currently on screen.
     */
    private val allParts: StateFlow<List<InspectablePart>> =
        repository.getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList(),
            )

    /**
     * Parts matching [searchQuery], ordered alphabetically and case insensitively.
     */
    val visibleParts: StateFlow<List<InspectablePart>> =
        combine(allParts, _searchQuery) { items, query ->
            items.asSequence()
                .filter { it.partName.contains(query, ignoreCase = true) }
                .sortedBy { it.partName.lowercase() }
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
        _editor.value = CatalogueEditorState()
    }

    fun onEditClick(item: InspectablePart) {
        _editor.value = CatalogueEditorState(
            id = item.id,
            name = item.partName,
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
     * Does nothing but set [CatalogueEditorState.nameError] when the name is blank or taken by another entry.
     */
    fun onEditorConfirm() {
        val current = _editor.value ?: return
        val name = current.name.trim()

        if (name.isBlank()) {
            _editor.value = current.copy(nameError = NameError.EMPTY)
            return
        }

        viewModelScope.launch {
            // Read the list straight from the source of truth instead of the cached state so the duplicate check is
            // correct even before the list has been rendered.
            val isDuplicate = repository.getAll().first().any { existing ->
                existing.id != current.id && existing.partName.equals(name, ignoreCase = true)
            }
            if (isDuplicate) {
                _editor.value = current.copy(nameError = NameError.DUPLICATE)
                return@launch
            }

            val notes = current.notes.trim().ifEmpty { null }

            if (current.isNew) {
                repository.insert(InspectablePart(partName = name, notes = notes))
            } else {
                val existing = repository.getById(current.id) ?: return@launch
                repository.update(existing.copy(partName = name, notes = notes))
            }
            _editor.value = null
        }
    }

    /**
     * Opens the delete confirmation dialog. The usage count is resolved asynchronously so the user can be warned that
     * past inspection events will lose this part.
     */
    fun onDeleteClick(item: InspectablePart) {
        _pendingDelete.value = PendingCatalogueDelete(target = item, usageCount = null)

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
     * [com.gasodoapp.gasodo.ui.components.DeleteDialog] dismisses the dialog before confirming, which clears
     * [pendingDelete] first.
     */
    fun onDeleteConfirm(target: InspectablePart) {
        _pendingDelete.value = null

        viewModelScope.launch {
            repository.delete(target)
        }
    }
}
