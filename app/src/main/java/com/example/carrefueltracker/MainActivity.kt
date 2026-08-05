package com.example.carrefueltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.carrefueltracker.feature.navigation.NavigationBarFrame
import com.example.carrefueltracker.ui.theme.CarRefuelTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CarRefuelTrackerTheme {
                NavigationBarFrame()
            }
        }
    }
}