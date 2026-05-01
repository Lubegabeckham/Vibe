package com.nedejje.vibe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.nedejje.vibe.ui.navigation.NavGraph
import com.nedejje.vibe.ui.theme.VibeTheme
import com.nedejje.vibe.viewmodel.MainViewModel

/**
 * MainActivity serves as the application entry point.
 * All business logic, navigation state, and theme management are handled in [MainViewModel]
 * to comply with the Ndejje University Capstone Project requirements.
 */
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            VibeTheme(darkTheme = viewModel.isDarkMode.value) {
                NavGraph(
                    navController = navController,
                    startDest = viewModel.getStartDestination(),
                    onThemeToggle = { viewModel.toggleTheme() },
                    isDarkMode = viewModel.isDarkMode.value
                )
            }
        }
    }
}