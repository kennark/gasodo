package com.gasodoapp.gasodo.ui.components.catalogue

import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gasodoapp.gasodo.R

/**
 * Create/edit dialog shared by the catalogue management screens.
 *
 * @param nounRes noun of the edited thing, used in error messages, e.g. [R.string.noun_service].
 * @param newTitleRes title shown while creating a new entry.
 * @param editTitleRes title shown while editing an existing entry.
 */
@Composable
fun CatalogueEditorDialog(
    state: CatalogueEditorState,
    @StringRes nounRes: Int,
    @StringRes newTitleRes: Int,
    @StringRes editTitleRes: Int,
    onNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (state.isNew) newTitleRes else editTitleRes)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.common_name)) },
                    singleLine = true,
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { error ->
                        { Text(error.message(nounRes)) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = onNotesChange,
                    label = { Text(stringResource(R.string.common_notes_optional)) },
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
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Preview
@Composable
private fun CatalogueEditorDialogPreview() {
    CatalogueEditorDialog(
        state = CatalogueEditorState(name = "Oil Change", notes = "Every 10k km"),
        nounRes = R.string.noun_service,
        newTitleRes = R.string.manage_services_new_title,
        editTitleRes = R.string.manage_services_edit_title,
        onNameChange = {},
        onNotesChange = {},
        onDismiss = {},
        onConfirm = {}
    )
}
