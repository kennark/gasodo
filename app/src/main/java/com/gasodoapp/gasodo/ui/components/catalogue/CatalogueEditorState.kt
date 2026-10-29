package com.gasodoapp.gasodo.ui.components.catalogue

/**
 * State of the create/edit dialog shared by the catalogue management screens.
 *
 * @property id identifier of the edited row, or `0L` when a new entry is being created.
 */
data class CatalogueEditorState(
    val id: Long = 0L,
    val name: String = "",
    val notes: String = "",
    val nameError: NameError? = null,
) {
    val isNew: Boolean
        get() = id == 0L
}

/**
 * A delete request waiting for confirmation.
 *
 * @property usageCount number of events referencing the entry, or `null` while it is still being looked up.
 */
data class PendingCatalogueDelete<T>(
    val target: T,
    val usageCount: Int? = null,
)
