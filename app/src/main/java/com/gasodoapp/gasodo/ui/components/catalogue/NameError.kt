package com.gasodoapp.gasodo.ui.components.catalogue

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gasodoapp.gasodo.R

/**
 * Reason why the name typed into a catalogue editor cannot be saved.
 */
enum class NameError {
    /** The name is blank */
    EMPTY,

    /** Another entry already uses this name */
    DUPLICATE,
}

/**
 * User facing message for a [NameError].
 *
 * @param typeRes noun of the thing being edited, e.g. [R.string.noun_service]. Passed as a resource
 * rather than a resolved string so the surrounding sentence can be translated as a whole.
 */
@Composable
fun NameError.message(@StringRes typeRes: Int): String = when (this) {
    NameError.EMPTY -> stringResource(R.string.error_name_empty)
    NameError.DUPLICATE -> stringResource(R.string.error_name_duplicate, stringResource(typeRes))
}
