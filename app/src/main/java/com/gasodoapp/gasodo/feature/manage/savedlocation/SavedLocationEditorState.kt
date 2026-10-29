package com.gasodoapp.gasodo.feature.manage.savedlocation

import com.gasodoapp.gasodo.ui.components.catalogue.NameError
import java.util.UUID

/**
 * State of the create/edit dialog of the saved location screen.
 *
 * @property id identifier of the edited location, or `null` when a new location is being created.
 * @property latitude current latitude of the edited location, kept untouched unless the map picker sets it.
 * @property longitude current longitude of the edited location, kept untouched unless the map picker sets it.
 * @property address current address of the edited location, kept untouched unless the map picker sets it.
 */
data class SavedLocationEditorState(
    val id: UUID? = null,
    val name: String = "",
    val nameError: NameError? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
) {
    val isNew: Boolean
        get() = id == null
}
