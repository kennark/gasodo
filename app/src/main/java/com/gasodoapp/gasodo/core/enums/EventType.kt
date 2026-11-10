package com.gasodoapp.gasodo.core.enums

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gasodoapp.gasodo.R

/**
 * Types of events that can be tracked in the car refuel tracker.
 *
 * [labelRes] is the user facing name. It is resolved through [label] rather than being stored as
 * a `String`, so the wording follows the app language and lives with the other translations.
 */
enum class EventType(@param:StringRes val labelRes: Int) {
    REFUEL(R.string.event_type_refuel),
    MAINTENANCE(R.string.event_type_maintenance),
    INSPECTION(R.string.event_type_inspection);

    /**
     * Localised name of this event type, for rendering inside a composable.
     */
    @Composable
    fun label(): String = stringResource(labelRes)
}
