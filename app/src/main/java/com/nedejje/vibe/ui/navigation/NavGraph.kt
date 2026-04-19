package com.nedejje.vibe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nedejje.vibe.ui.screens.*

@Composable
fun NavGraph(
    navController: NavHostController,
    onThemeToggle: () -> Unit,
    isDarkMode: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Signup.route) { SignupScreen(navController) }
        
        // User Dashboard
        composable(Screen.Home.route) { 
            HomeScreen(navController, onThemeToggle, isDarkMode) 
        }
        
        // Admin Dashboard
        composable(Screen.AdminHome.route) { 
            AdminHomeScreen(navController) 
        }
        
        // Event Editor (Admin)
        composable(
            route = Screen.EventEditor.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            EventEditorScreen(navController, eventId)
        }
        
        // Event Detail (User)
        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            EventDetailScreen(navController, eventId)
        }

        // Ticket Purchase (User)
        composable(
            route = Screen.TicketPurchase.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            TicketPurchaseScreen(navController, eventId)
        }

        composable(Screen.GuestManager.route) { GuestManagerScreen(navController) }
        composable(Screen.BudgetTracker.route) { BudgetTrackerScreen(navController) }
        composable(Screen.Invitation.route) { InvitationScreen(navController) }
        composable(Screen.Contribution.route) { ContributionScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
        composable(Screen.WrapReport.route) { WrapReportScreen(navController) }
        composable(Screen.Team.route) { TeamScreen(navController) }
    }
}
