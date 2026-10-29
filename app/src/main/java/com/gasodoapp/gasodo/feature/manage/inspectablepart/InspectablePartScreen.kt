package com.gasodoapp.gasodo.feature.manage.inspectablepart

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
import com.gasodoapp.gasodo.core.database.entity.InspectablePart
import com.gasodoapp.gasodo.ui.components.DeleteDialog
import com.gasodoapp.gasodo.ui.components.TopBarScaffold
import com.gasodoapp.gasodo.ui.components.catalogue.CatalogueEditorDialog
import com.gasodoapp.gasodo.ui.components.catalogue.CatalogueList
import com.gasodoapp.gasodo.ui.components.catalogue.PendingCatalogueDelete
import com.gasodoapp.gasodo.ui.icons.add
import com.gasodoapp.gasodo.ui.icons.arrow_back

/**
 * Screen listing every [InspectablePart] with create, edit and delete actions.
 */
@Composable
fun InspectablePartScreen(
    onBack: () -> Unit,
    viewModel: InspectablePartScreenViewModel =
        hiltViewModel<InspectablePartScreenViewModel>(),
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val parts by viewModel.visibleParts.collectAsStateWithLifecycle()
    val editor by viewModel.editor.collectAsStateWithLifecycle()
    val pendingDelete by viewModel.pendingDelete.collectAsStateWithLifecycle()

    TopBarScaffold(
        title = "Inspectable Parts",
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
                Icon(imageVector = add, contentDescription = "Add inspectable part")
            }
        }
    ) { innerPadding ->
        CatalogueList(
            items = parts,
            searchQuery = searchQuery,
            searchLabel = "Search parts",
            emptyTitle = "No parts found",
            emptySubtitle = "Add a part to use it in inspection events",
            key = { it.id },
            title = { it.partName },
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
            noun = "part",
            newTitle = "New inspectable part",
            editTitle = "Edit inspectable part",
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
            title = "Delete \"${pending.target.partName}\"?",
            message = deleteMessage(pending)
        )
    }
}

/**
 * Explains the consequences of removing a part, including the number of past inspections that reference it. The
 * relation cascades, so those inspections lose the part.
 */
private fun deleteMessage(pending: PendingCatalogueDelete<InspectablePart>): String {
    val usageCount = pending.usageCount
        ?: return "Deleted items can not be recovered."

    return when (usageCount) {
        0 -> "This part is not used by any inspection. Deleted items can not be recovered."
        1 -> "This part is used by 1 inspection and will be removed from it. " +
                "Deleted items can not be recovered."

        else -> "This part is used by $usageCount inspections and will be removed from all of " +
                "them. Deleted items can not be recovered."
    }
}
