package com.gasodoapp.gasodo.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarScaffold(
    title: String,
    subtitle: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    applyBottomPadding: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {

    Scaffold(
        contentWindowInsets = if (applyBottomPadding) WindowInsets.safeDrawing else
            WindowInsets.statusBars.union(
                WindowInsets.displayCutout
            ),
        topBar = {
            TopAppBar(
                title = {
                    Text(title)
                },
                subtitle = {
                    if (subtitle != null)
                        Text(subtitle)
                },
                navigationIcon = {
                    navigationIcon?.invoke()
                },
                actions = {
                    actions?.invoke()
                })
        }
    ) { values ->
        content(values)
    }
}