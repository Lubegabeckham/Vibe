package com.nedejje.vibe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.nedejje.vibe.ui.navigation.NavGraph
import com.nedejje.vibe.ui.theme.VibeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            VibeTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                NavGraph(navController = navController, onThemeToggle = { isDarkMode = !isDarkMode }, isDarkMode = isDarkMode)
            }
        }
    }
}
