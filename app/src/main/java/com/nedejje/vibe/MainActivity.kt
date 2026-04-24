package com.nedejje.vibe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
<<<<<<< HEAD
import com.nedejje.vibe.session.SessionManager
import com.nedejje.vibe.ui.navigation.NavGraph
import com.nedejje.vibe.ui.navigation.Screen
=======
import com.nedejje.vibe.ui.navigation.NavGraph
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
import com.nedejje.vibe.ui.theme.VibeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
<<<<<<< HEAD
            var isDarkMode by remember { mutableStateOf(true) } // Vibe defaults to dark
            VibeTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()

                // Determine start destination based on existing session
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
=======
            var isDarkMode by remember { mutableStateOf(false) }
            VibeTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                NavGraph(navController = navController, onThemeToggle = { isDarkMode = !isDarkMode }, isDarkMode = isDarkMode)
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
            }
        }
    }
}
