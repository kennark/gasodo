package com.gasodoapp.gasodo.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gasodoapp.gasodo.core.enums.ThemeMode
import com.gasodoapp.gasodo.core.enums.units.Currency
import com.gasodoapp.gasodo.core.enums.units.Distance
import com.gasodoapp.gasodo.core.enums.units.Liquid
import com.gasodoapp.gasodo.core.settings.AppSettings
import com.gasodoapp.gasodo.core.settings.Units
import com.gasodoapp.gasodo.ui.components.TopBarScaffold
import com.gasodoapp.gasodo.ui.icons.arrow_back
import com.gasodoapp.gasodo.ui.theme.GasodoTheme

/**
 * Screen listing the user configurable preferences.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsScreenViewModel = hiltViewModel<SettingsScreenViewModel>(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val units by viewModel.units.collectAsStateWithLifecycle()

    TopBarScaffold(
        title = "Settings",
        applyBottomPadding = true,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(imageVector = arrow_back, contentDescription = arrow_back.name)
            }
        },
    ) { innerPadding ->
        SettingsContent(
            settings = settings,
            units = units,
            onThemeModeChange = viewModel::onThemeModeChange,
            onDistanceUnitChange = viewModel::onDistanceUnitChange,
            onLiquidUnitChange = viewModel::onLiquidUnitChange,
            onCurrencyUnitChange = viewModel::onCurrencySymbolChange,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun SettingsContent(
    settings: AppSettings,
    units: Units,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDistanceUnitChange: (Distance) -> Unit,
    onLiquidUnitChange: (Liquid) -> Unit,
    onCurrencyUnitChange: (Currency) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsCard(title = "Appearance") {
            ThemeModeSelector(
                themeMode = settings.themeMode,
                onThemeModeChange = onThemeModeChange,
            )
        }

        SettingsCard(title = "Units") {
            SettingsDropdown(
                label = "Distance",
                options = Distance.entries,
                selected = units.distance,
                optionLabel = Distance::displayName,
                onSelect = onDistanceUnitChange,
            )
            SettingsDropdown(
                label = "Fuel volume",
                options = Liquid.entries,
                selected = units.liquid,
                optionLabel = Liquid::displayName,
                onSelect = onLiquidUnitChange,
            )
            SettingsDropdown(
                label = "Currency symbol",
                options = Currency.entries,
                selected = units.currency,
                optionLabel = Currency::displayName,
                onSelect = onCurrencyUnitChange,
            )
        }
    }
}

@Composable
private fun ThemeModeSelector(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = themeMode.description(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ThemeMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == themeMode,
                    onClick = { onThemeModeChange(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ThemeMode.entries.size,
                    ),
                    label = { Text(mode.displayName) },
                )
            }
        }
    }
}

/**
 * Read only dropdown used to pick one of the [options].
 *
 * The text field only displays the current selection, the menu is what changes it.
 */
@Composable
private fun <T> SettingsDropdown(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    var expanded by remember { mutableStateOf(value = false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = optionLabel(selected),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            supportingText = supportingText?.let { text ->
                { Text(text) }
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

/**
 * Explains what the current [ThemeMode] means.
 */
private fun ThemeMode.description(): String = when (this) {
    ThemeMode.SYSTEM -> "Follow the device setting"
    ThemeMode.LIGHT -> "Always use light colours"
    ThemeMode.DARK -> "Always use dark colours"
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentSystemPreview() {
    GasodoTheme(darkTheme = false) {
        SettingsContent(
            settings = AppSettings(themeMode = ThemeMode.SYSTEM),
            units = Units(),
            onThemeModeChange = {},
            onDistanceUnitChange = {},
            onLiquidUnitChange = {},
            onCurrencyUnitChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentLightPreview() {
    GasodoTheme(darkTheme = false) {
        SettingsContent(
            settings = AppSettings(themeMode = ThemeMode.LIGHT),
            units = Units(),
            onThemeModeChange = {},
            onDistanceUnitChange = {},
            onLiquidUnitChange = {},
            onCurrencyUnitChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentDarkPreview() {
    GasodoTheme(darkTheme = true) {
        SettingsContent(
            settings = AppSettings(themeMode = ThemeMode.DARK),
            units = Units(),
            onThemeModeChange = {},
            onDistanceUnitChange = {},
            onLiquidUnitChange = {},
            onCurrencyUnitChange = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentImperialUnitsPreview() {
    GasodoTheme(darkTheme = false) {
        SettingsContent(
            settings = AppSettings(),
            units = Units(distance = Distance.MILE, liquid = Liquid.GALLON),
            onThemeModeChange = {},
            onDistanceUnitChange = {},
            onLiquidUnitChange = {},
            onCurrencyUnitChange = {},
        )
    }
}
