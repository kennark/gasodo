package com.gasodoapp.gasodo.feature.manage.maintenanceservicetype

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.ui.components.DeleteDialog
import com.gasodoapp.gasodo.ui.components.TopBarScaffold
import com.gasodoapp.gasodo.ui.components.catalogue.CatalogueEditorDialog
import com.gasodoapp.gasodo.ui.components.catalogue.CatalogueList
import com.gasodoapp.gasodo.ui.components.catalogue.PendingCatalogueDelete
import com.gasodoapp.gasodo.ui.icons.add
import com.gasodoapp.gasodo.ui.icons.arrow_back

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
        CatalogueList(
            items = serviceTypes,
            searchQuery = searchQuery,
            searchLabel = "Search services",
            emptyTitle = "No services found",
            emptySubtitle = "Add a service type to use it in maintenance events",
            key = { it.id },
            title = { it.serviceName },
            notes = { it.notes },
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onEditClick = viewModel::onEditClick,
            onDeleteClick = viewModel::onDeleteClick,
            modifier = Modifier.padding(innerPadding)
        )
    }

    editor?.let { current ->
        CatalogueEditorDialog(
            state = current,
            noun = "service",
            newTitle = "New service type",
            editTitle = "Edit service type",
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
private fun deleteMessage(pending: PendingCatalogueDelete<MaintenanceServiceType>): String {
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
