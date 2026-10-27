package com.gasodoapp.gasodo.feature.manage.maintenanceservicetype

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.ui.components.DeleteDialog
import com.gasodoapp.gasodo.ui.components.MediumLabelText
import com.gasodoapp.gasodo.ui.components.NoDataCard
import com.gasodoapp.gasodo.ui.components.TopBarScaffold
import com.gasodoapp.gasodo.ui.icons.add
import com.gasodoapp.gasodo.ui.icons.arrow_back
import com.gasodoapp.gasodo.ui.icons.delete
import com.gasodoapp.gasodo.ui.icons.edit

/**
 * Screen listing every [MaintenanceServiceType] with create, edit and delete actions.
 */
@Composable
fun MaintenanceServiceTypeScreen(
    onBack: () -> Unit,
    viewModel: MaintenanceServiceTypeScreenViewModel =
        hiltViewModel<MaintenanceServiceTypeScreenViewModel>(),
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val serviceTypes by viewModel.visibleServiceTypes.collectAsStateWithLifecycle()
    val editor by viewModel.editor.collectAsStateWithLifecycle()
    val pendingDelete by viewModel.pendingDelete.collectAsStateWithLifecycle()

    TopBarScaffold(
        title = "Maintenance Services",

        applyBottomPadding = true,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(imageVector = arrow_back, contentDescription = arrow_back.name)
            }
        },
        actions = {
            FilledIconButton(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(
                        IconButtonDefaults.smallContainerSize(
                            IconButtonDefaults.IconButtonWidthOption.Wide
                        )
                    ),
                onClick = viewModel::onCreateClick
            ) {
                Icon(imageVector = add, contentDescription = "Add service type")
            }
        }
    ) { innerPadding ->
        MainContent(
            paddingValues = innerPadding,
            searchQuery = searchQuery,
            serviceTypes = serviceTypes,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onEditClick = viewModel::onEditClick,
            onDeleteClick = viewModel::onDeleteClick
        )
    }

    editor?.let { current ->
        ServiceTypeEditorDialog(
            state = current,
            onNameChange = viewModel::onEditorNameChange,
            onNotesChange = viewModel::onEditorNotesChange,
            onDismiss = viewModel::onEditorDismiss,
            onConfirm = viewModel::onEditorConfirm
        )
    }

    pendingDelete?.let { pending ->
        DeleteDialog(
            onDismissRequest = viewModel::onDeleteDismiss,
            onConfirmRequest = { viewModel.onDeleteConfirm(pending.target) },
            title = "Delete \"${pending.target.serviceName}\"?",
            message = deleteMessage(pending)
        )
    }
}

/**
 * Explains the consequences of removing a service type, including the number of past events that
 * reference it. The relation cascades, so those events lose the service.
 */
private fun deleteMessage(pending: PendingServiceTypeDelete): String {
    val usageCount = pending.usageCount
        ?: return "Deleted items can not be recovered."

    return when (usageCount) {
        0 -> "This service is not used by any event. Deleted items can not be recovered."
        1 -> "This service is used by 1 event and will be removed from it. " +
                "Deleted items can not be recovered."

        else -> "This service is used by $usageCount events and will be removed from all of " +
                "them. Deleted items can not be recovered."
    }
}

@Composable
private fun MainContent(
    paddingValues: PaddingValues,
    searchQuery: String,
    serviceTypes: List<MaintenanceServiceType>,
    onSearchQueryChange: (String) -> Unit,
    onEditClick: (MaintenanceServiceType) -> Unit,
    onDeleteClick: (MaintenanceServiceType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search services") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(serviceTypes, key = { it.id }) { type ->
                ServiceTypeRow(
                    type = type,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick
                )
            }

            if (serviceTypes.isEmpty()) {
                item {
                    NoDataCard(
                        title = "No services found",
                        subtitle = "Add a service type to use it in maintenance events"
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceTypeRow(
    type: MaintenanceServiceType,
    onEditClick: (MaintenanceServiceType) -> Unit,
    onDeleteClick: (MaintenanceServiceType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = type.serviceName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!type.notes.isNullOrBlank()) {
                    MediumLabelText(type.notes)
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(onClick = { onEditClick(type) }) {
                    Icon(imageVector = edit, contentDescription = "Edit ${type.serviceName}")
                }
                FilledTonalIconButton(
                    onClick = { onDeleteClick(type) },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(imageVector = delete, contentDescription = "Delete ${type.serviceName}")
                }
            }
        }
    }
}

@Composable
private fun ServiceTypeEditorDialog(
    state: ServiceTypeEditor,
    onNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state.isNew) "New service type" else "Edit service type") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    singleLine = true,
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { error ->
                        { Text(error.message()) }
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
