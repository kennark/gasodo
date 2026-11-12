package com.gasodoapp.gasodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gasodoapp.gasodo.feature.navigation.NavigationBarFrame
import com.gasodoapp.gasodo.ui.theme.GasodoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val darkTheme = themeMode.isDarkTheme()
            val useDynamicTheme by viewModel.useDynamicTheme.collectAsStateWithLifecycle()

            // enableEdgeToEdge only looks at the platform theme, so an in-app override would
            // otherwise leave the system bar icons with the wrong contrast.
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }

            GasodoTheme(
                darkTheme = darkTheme,
                useDynamicTheme = useDynamicTheme
            ) {
                NavigationBarFrame()
            }
        }
    }
}
