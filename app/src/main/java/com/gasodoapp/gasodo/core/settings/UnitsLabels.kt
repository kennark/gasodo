package com.gasodoapp.gasodo.core.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Composable display labels derived from [Units] that need a resource lookup.
 *
 * The shorthand and symbol based labels live on [Units] itself because they are plain strings and
 * can therefore be read from ViewModels and unit tests. The label below spells a unit out in words,
 * so it has to be resolved inside a composable.
 */

/**
 * Name of a single fuel volume, for labels that read as a sentence, for example
 * `Price per Gallon` rather than `Price per gal`.
 */
@Composable
fun Units.liquidSingularLabel(): String = stringResource(liquid.singularNameRes)
