package com.nedejje.vibe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.nedejje.vibe.session.SessionManager
import com.nedejje.vibe.ui.navigation.NavGraph
import com.nedejje.vibe.ui.navigation.Screen
import com.nedejje.vibe.ui.theme.VibeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Keep your new "Vibe defaults to dark" logic
            var isDarkMode by remember { mutableStateOf(true) }
            VibeTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()

                // Keep your new session management logic
                val startDest = if (SessionManager.isLoggedIn) {
                    if (SessionManager.isAdmin) Screen.AdminHome.route else Screen.Home.route
                } else {
                    Screen.Splash.route
                }

                NavGraph(
                    navController  = navController,
                    startDest      = startDest,
                    onThemeToggle  = { isDarkMode = !isDarkMode },
                    isDarkMode     = isDarkMode
                )
            }
        }
    }
}