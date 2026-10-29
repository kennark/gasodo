package com.gasodoapp.gasodo.ui.components.catalogue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gasodoapp.gasodo.ui.components.MediumLabelText
import com.gasodoapp.gasodo.ui.icons.delete
import com.gasodoapp.gasodo.ui.icons.edit

/**
 * Card row shared by the catalogue management screens
 */
@Composable
fun CatalogueItemRow(
    title: String,
    notes: String?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
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
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!notes.isNullOrBlank()) {
                    MediumLabelText(notes)
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(onClick = onEditClick) {
                    Icon(imageVector = edit, contentDescription = "Edit $title")
                }
                FilledTonalIconButton(
                    onClick = onDeleteClick,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(imageVector = delete, contentDescription = "Delete $title")
                }
            }
        }
    }
}

@Preview
@Composable
private fun CatalogueItemRowPreview() {
    CatalogueItemRow(
        title = "Oil Change",
        notes = "Every 10k km",
        onEditClick = {},
        onDeleteClick = {}
    )
}
