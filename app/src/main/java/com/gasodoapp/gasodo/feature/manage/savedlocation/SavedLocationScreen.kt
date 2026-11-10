package com.gasodoapp.gasodo.feature.manage.savedlocation

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
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.ui.components.DeleteDialog
import com.gasodoapp.gasodo.ui.components.TopBarScaffold
import com.gasodoapp.gasodo.ui.components.catalogue.CatalogueList
import com.gasodoapp.gasodo.ui.components.catalogue.PendingCatalogueDelete
import com.gasodoapp.gasodo.ui.icons.add
import com.gasodoapp.gasodo.ui.icons.arrow_back

/**
 * Screen listing every [SavedLocation] with create, edit and delete actions.
 */
@Composable
fun SavedLocationScreen(
    onBack: () -> Unit,
    viewModel: SavedLocationScreenViewModel =
        hiltViewModel<SavedLocationScreenViewModel>(),
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val locations by viewModel.visibleLocations.collectAsStateWithLifecycle()
    val editor by viewModel.editor.collectAsStateWithLifecycle()
    val pendingDelete by viewModel.pendingDelete.collectAsStateWithLifecycle()

    TopBarScaffold(
        title = stringResource(R.string.manage_locations_title),
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
                    contentDescription = stringResource(R.string.manage_locations_add_cd)
                )
            }
        }
    ) { innerPadding ->
        CatalogueList(
            items = locations,
            searchQuery = searchQuery,
            searchLabel = stringResource(R.string.manage_locations_search_hint),
            emptyTitle = stringResource(R.string.manage_locations_empty_title),
            emptySubtitle = stringResource(R.string.manage_locations_empty_subtitle),
            key = { it.id },
            title = { it.name },
            notes = { it.address },
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onEditClick = viewModel::onEditClick,
            onDeleteClick = viewModel::onDeleteClick,
            modifier = Modifier.padding(innerPadding)
        )
    }

    editor?.let { current ->
        SavedLocationEditorDialog(
            state = current,
            onNameChange = viewModel::onEditorNameChange,
            onDismiss = viewModel::onEditorDismiss,
            onConfirm = viewModel::onEditorConfirm
        )
    }

    pendingDelete?.let { pending ->
        DeleteDialog(
            onDismissRequest = viewModel::onDeleteDismiss,
            onConfirmRequest = { viewModel.onDeleteConfirm(pending.target) },
            title = stringResource(R.string.manage_delete_title, pending.target.name),
            message = deleteMessage(pending)
        )
    }
}

/**
 * Explains the consequences of removing a location. The relation uses `SET_NULL`, so past events keep their data but
 * lose the location.
 */
@Composable
private fun deleteMessage(pending: PendingCatalogueDelete<SavedLocation>): String {
    val usageCount = pending.usageCount
        ?: return stringResource(R.string.delete_dialog_message)

    return if (usageCount == 0) {
        stringResource(R.string.manage_locations_delete_unused)
    } else {
        pluralStringResource(R.plurals.manage_locations_delete_used, usageCount, usageCount)
    }
}
