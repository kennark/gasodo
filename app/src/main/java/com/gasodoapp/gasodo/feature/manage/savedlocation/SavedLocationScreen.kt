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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        title = "Saved Locations",
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
                Icon(imageVector = add, contentDescription = "Add saved location")
            }
        }
    ) { innerPadding ->
        CatalogueList(
            items = locations,
            searchQuery = searchQuery,
            searchLabel = "Search locations",
            emptyTitle = "No locations found",
            emptySubtitle = "Add a location to use it in events",
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
            title = "Delete \"${pending.target.name}\"?",
            message = deleteMessage(pending)
        )
    }
}

/**
 * Explains the consequences of removing a location. The relation uses `SET_NULL`, so past events keep their data but
 * lose the location.
 */
private fun deleteMessage(pending: PendingCatalogueDelete<SavedLocation>): String {
    val usageCount = pending.usageCount
        ?: return "Deleted items can not be recovered."

    return when (usageCount) {
        0 -> "This location is not used by any event. Deleted items can not be recovered."
        1 -> "This location is used by 1 event, which will no longer show a location. " +
                "Deleted items can not be recovered."

        else -> "This location is used by $usageCount events, which will no longer show a " +
                "location. Deleted items can not be recovered."
    }
}
