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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gasodoapp.gasodo.R
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
        title = stringResource(R.string.manage_services_title),
        applyBottomPadding = true,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = arrow_back,
                    contentDescription = stringResource(R.string.cd_back)
                )
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
                Icon(
                    imageVector = add,
                    contentDescription = stringResource(R.string.manage_services_add_cd)
                )
            }
        }
    ) { innerPadding ->
        CatalogueList(
            items = serviceTypes,
            searchQuery = searchQuery,
            searchLabel = stringResource(R.string.manage_services_search_hint),
            emptyTitle = stringResource(R.string.manage_services_empty_title),
            emptySubtitle = stringResource(R.string.manage_services_empty_subtitle),
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
            nounRes = R.string.noun_service,
            newTitleRes = R.string.manage_services_new_title,
            editTitleRes = R.string.manage_services_edit_title,
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
            title = stringResource(R.string.manage_delete_title, pending.target.serviceName),
            message = deleteMessage(pending)
        )
    }
}

/**
 * Explains the consequences of removing a service type, including the number of past events that
 * reference it. The relation cascades, so those events lose the service.
 */
@Composable
private fun deleteMessage(pending: PendingCatalogueDelete<MaintenanceServiceType>): String {
    val usageCount = pending.usageCount
        ?: return stringResource(R.string.delete_dialog_message)

    return if (usageCount == 0) {
        stringResource(R.string.manage_services_delete_unused)
    } else {
        pluralStringResource(R.plurals.manage_services_delete_used, usageCount, usageCount)
    }
}
