package com.example.carrefueltracker.feature.navigation

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
    navigationIcon: @Composable () -> Unit,
    actions: @Composable () -> Unit,
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
                navigationIcon = {
                    navigationIcon()
                },
                actions = {
                    actions()
                })
        }
    ) { values ->
        content(values)
    }
}