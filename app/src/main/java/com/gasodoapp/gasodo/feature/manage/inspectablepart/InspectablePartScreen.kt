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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gasodoapp.gasodo.R
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
        title = stringResource(R.string.manage_parts_title),
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
                    contentDescription = stringResource(R.string.manage_parts_add_cd)
                )
            }
        }
    ) { innerPadding ->
        CatalogueList(
            items = parts,
            searchQuery = searchQuery,
            searchLabel = stringResource(R.string.manage_parts_search_hint),
            emptyTitle = stringResource(R.string.manage_parts_empty_title),
            emptySubtitle = stringResource(R.string.manage_parts_empty_subtitle),
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
            nounRes = R.string.noun_part,
            newTitleRes = R.string.manage_parts_new_title,
            editTitleRes = R.string.manage_parts_edit_title,
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
            title = stringResource(R.string.manage_delete_title, pending.target.partName),
            message = deleteMessage(pending)
        )
    }
}

/**
 * Explains the consequences of removing a part, including the number of past inspections that reference it. The
 * relation cascades, so those inspections lose the part.
 */
@Composable
private fun deleteMessage(pending: PendingCatalogueDelete<InspectablePart>): String {
    val usageCount = pending.usageCount
        ?: return stringResource(R.string.delete_dialog_message)

    return if (usageCount == 0) {
        stringResource(R.string.manage_parts_delete_unused)
    } else {
        pluralStringResource(R.plurals.manage_parts_delete_used, usageCount, usageCount)
    }
}
