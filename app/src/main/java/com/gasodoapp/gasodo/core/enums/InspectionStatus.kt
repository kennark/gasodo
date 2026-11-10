package com.gasodoapp.gasodo.core.enums

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gasodoapp.gasodo.R

/**
 * Possible outcomes of a vehicle inspection.
 *
 * [labelRes] is the user facing name, resolved through [label] so it follows the app language.
 */
enum class InspectionStatus(@param:StringRes val labelRes: Int) {
    PASS(R.string.inspection_status_pass),
    FAIL(R.string.inspection_status_fail),
    CONDITIONAL_PASS(R.string.inspection_status_conditional_pass);

    /**
     * Localised name of this status, for rendering inside a composable.
     */
    @Composable
    fun label(): String = stringResource(labelRes)
}
