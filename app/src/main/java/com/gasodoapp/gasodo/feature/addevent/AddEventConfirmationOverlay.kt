package com.gasodoapp.gasodo.feature.addevent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gasodoapp.gasodo.ui.icons.check_circle
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * A simple confirmation overlay that displays after successfully saving an event.
 * Shows a success icon and message, then auto-dismisses after a short delay.
 */
@Composable
fun AddEventConfirmationOverlay(
    type: String,
    onNavigationBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1500.milliseconds)
        onNavigationBack()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = check_circle,
                contentDescription = check_circle.name,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$type saved!",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
