package com.gasodoapp.gasodo.feature.manage.savedlocation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gasodoapp.gasodo.ui.components.MediumLabelText
import com.gasodoapp.gasodo.ui.components.catalogue.message

/**
 * Create/edit dialog for a saved location.
 *
 * Only the name is editable. The coordinates and the address can not be set here yet; they will be picked on a map
 * once that feature lands, so they are only displayed for reference.
 */
@Composable
fun SavedLocationEditorDialog(
    state: SavedLocationEditorState,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state.isNew) "New saved location" else "Edit saved location") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    singleLine = true,
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { error ->
                        { Text(error.message("location")) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                )

                LocationOnMapSection(state = state)
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = state.name.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun LocationOnMapSection(state: SavedLocationEditorState) {
    val address = state.address?.takeIf { it.isNotBlank() }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        MediumLabelText(address ?: "No address set yet")

        // todo The map picker is not implemented yet
        OutlinedButton(
            onClick = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Set location on map")
        }
    }
}

@Preview
@Composable
private fun SavedLocationEditorDialogPreview() {
    SavedLocationEditorDialog(
        state = SavedLocationEditorState(
            name = "Shell Berlin",
            latitude = 52.52,
            longitude = 13.405,
            address = "Hauptstraße 1, 10115 Berlin"
        ),
        onNameChange = {},
        onDismiss = {},
        onConfirm = {}
    )
}
