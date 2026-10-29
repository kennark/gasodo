package com.gasodoapp.gasodo.ui.components.catalogue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gasodoapp.gasodo.ui.components.NoDataCard

/**
 * Search field plus list body shared by the catalogue management screens.
 *
 * @param items already filtered and ordered entries to render.
 * @param key stable key for the lazy list, usually the entity id.
 * @param title display name of an entry.
 * @param notes optional secondary text shown under the title; blank values are hidden.
 */
@Composable
fun <T> CatalogueList(
    items: List<T>,
    searchQuery: String,
    searchLabel: String,
    emptyTitle: String,
    emptySubtitle: String,
    key: (T) -> Any,
    title: (T) -> String,
    notes: (T) -> String?,
    onSearchQueryChange: (String) -> Unit,
    onEditClick: (T) -> Unit,
    onDeleteClick: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text(searchLabel) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(items, key = key) { item ->
                CatalogueItemRow(
                    title = title(item),
                    notes = notes(item),
                    onEditClick = { onEditClick(item) },
                    onDeleteClick = { onDeleteClick(item) }
                )
            }

            if (items.isEmpty()) {
                item {
                    NoDataCard(
                        title = emptyTitle,
                        subtitle = emptySubtitle
                    )
                }
            }
        }
    }
}
