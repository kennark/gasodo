package com.gasodoapp.gasodo.core.expansion

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Expansion state of a list whose rows can be expanded to reveal extra details.
 *
 * The open ids live here rather than inside the row composables because a `LazyColumn` recycles
 * item compositions: a flag remembered inside the row is dropped as soon as the row scrolls out
 * of the viewport. Keeping them alongside the rest of the list state also means they can be
 * pruned when an event is deleted, and cleared when the screen goes away.
 */

/**
 * Opens [id] if it is closed and closes it if it is open.
 */
fun <T> MutableStateFlow<Set<T>>.toggleExpanded(id: T) {
    update { current ->
        if (id in current) current - id else current + id
    }
}

/**
 * Closes [id] if it is open. Used when a single event is deleted.
 */
fun <T> MutableStateFlow<Set<T>>.collapse(id: T) {
    update { current -> current - id }
}

/**
 * Closes every id in [ids]. Used when a whole selection is deleted.
 */
fun <T> MutableStateFlow<Set<T>>.collapse(ids: Collection<T>) {
    update { current -> current - ids.toSet() }
}

/**
 * Closes every row, leaving no open id behind.
 */
fun <T> MutableStateFlow<Set<T>>.collapseAll() {
    value = emptySet()
}
