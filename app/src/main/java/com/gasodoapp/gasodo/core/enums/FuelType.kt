package com.gasodoapp.gasodo.core.enums

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gasodoapp.gasodo.R

/**
 * Types of fuel available for refueling events.
 *
 * [labelRes] is the user facing name, resolved through [label] so it follows the app language.
 */
enum class FuelType(@param:StringRes val labelRes: Int) {
    PETROL(R.string.fuel_type_petrol),
    DIESEL(R.string.fuel_type_diesel),
    PREMIUM(R.string.fuel_type_premium),
    ELECTRIC(R.string.fuel_type_electric),
    HYBRID(R.string.fuel_type_hybrid),
    LPG(R.string.fuel_type_lpg),
    OTHER(R.string.fuel_type_other);

    /**
     * Localised name of this fuel type, for rendering inside a composable.
     */
    @Composable
    fun label(): String = stringResource(labelRes)
}
