package com.gasodoapp.gasodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gasodoapp.gasodo.feature.navigation.NavigationBarFrame
import com.gasodoapp.gasodo.ui.theme.GasodoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            GasodoTheme {
                NavigationBarFrame()
            }
        }
    }
}