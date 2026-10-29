package com.gasodoapp.gasodo.ui.components.catalogue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Create/edit dialog shared by the catalogue management screens.
 *
 * @param noun lower case name of the edited thing, used in error messages, e.g. "service".
 * @param newTitle title shown while creating a new entry.
 * @param editTitle title shown while editing an existing entry.
 */
@Composable
fun CatalogueEditorDialog(
    state: CatalogueEditorState,
    noun: String,
    newTitle: String,
    editTitle: String,
    onNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state.isNew) newTitle else editTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    singleLine = true,
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { error ->
                        { Text(error.message(noun)) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = onNotesChange,
                    label = { Text("Notes (optional)") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = state.name.isNotBlank()
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

@Preview
@Composable
private fun CatalogueEditorDialogPreview() {
    CatalogueEditorDialog(
        state = CatalogueEditorState(name = "Oil Change", notes = "Every 10k km"),
        noun = "service",
        newTitle = "New service type",
        editTitle = "Edit service type",
        onNameChange = {},
        onNotesChange = {},
        onDismiss = {},
        onConfirm = {}
    )
}
