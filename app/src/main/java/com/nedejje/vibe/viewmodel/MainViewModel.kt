package com.nedejje.vibe.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.nedejje.vibe.session.SessionManager
import com.nedejje.vibe.ui.navigation.Screen

class MainViewModel : ViewModel() {
    private val _isDarkMode = mutableStateOf(true)
    val isDarkMode: State<Boolean> = _isDarkMode

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun getStartDestination(): String {
        return if (SessionManager.isLoggedIn) {
            if (SessionManager.isAdmin) Screen.AdminHome.route else Screen.Home.route
        } else {
            Screen.Splash.route
        }
    }
}