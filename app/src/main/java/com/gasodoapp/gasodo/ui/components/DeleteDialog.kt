package com.gasodoapp.gasodo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun DeleteDialog(
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit
) {
    BasicAlertDialog(
        { onDismissRequest() }
    ) {
        DeleteDialogContent(onDismissRequest, onConfirmRequest)
    }
}

@Composable
private fun DeleteDialogContent(onDismissRequest: () -> Unit, onConfirmRequest: () -> Unit) {
    Card(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Delete this event?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Deleted items can not be recovered."
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    },
                ) {
                    Text("Dismiss")
                }
                TextButton(
                    onClick = {
                        onDismissRequest()
                        onConfirmRequest()
                    },
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}


@Preview
@Composable
fun DeleteDialogPreview() {
    DeleteDialogContent({}, {})
}