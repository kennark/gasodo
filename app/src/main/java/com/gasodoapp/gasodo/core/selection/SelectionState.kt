package com.gasodoapp.gasodo.core.selection

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Selection state of an event list that supports multi-select.
 *
 * [active] is tracked separately from [ids] because selection mode can be entered from the overflow
 * menu before any item is picked.
 */
data class SelectionState<T>(
    val active: Boolean = false,
    val ids: Set<T> = emptySet(),
) {
    val count: Int get() = ids.size
}

/**
 * Enters selection mode with nothing selected yet.
 */
fun <T> MutableStateFlow<SelectionState<T>>.beginSelection() {
    value = SelectionState(active = true)
}

/**
 * Enters selection mode if needed and selects [id]. Selecting an already selected item keeps it
 * selected once.
 */
fun <T> MutableStateFlow<SelectionState<T>>.selectItem(id: T) {
    update { current ->
        SelectionState(active = true, ids = current.ids + id)
    }
}

/**
 * Toggles [id] while selection mode is on. Outside selection mode this is a no-op so the row keeps
 * its default behaviour.
 */
fun <T> MutableStateFlow<SelectionState<T>>.toggleItem(id: T) {
    update { current ->
        if (!current.active) return@update current

        val ids = if (id in current.ids) current.ids - id else current.ids + id
        // Deselecting the last item leaves selection mode, like other Material apps do.
        SelectionState(active = ids.isNotEmpty(), ids = ids)
    }
}

/**
 * Leaves selection mode and drops every selected id.
 */
fun <T> MutableStateFlow<SelectionState<T>>.clearSelection() {
    value = SelectionState()
}
