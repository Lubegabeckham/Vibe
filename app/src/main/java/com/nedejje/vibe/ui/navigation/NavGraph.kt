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
<<<<<<< HEAD
    startDest: String = Screen.Splash.route,
    onThemeToggle: () -> Unit,
    isDarkMode: Boolean
) {
    NavHost(navController = navController, startDestination = startDest) {

        // ── Auth ──────────────────────────────────────────────────────────
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route)  { LoginScreen(navController) }
        composable(Screen.Signup.route) { SignupScreen(navController) }

        // ── User ──────────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(navController, onThemeToggle, isDarkMode)
        }
        composable(Screen.Profile.route)  { ProfileScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController, onThemeToggle, isDarkMode) }

        // ── Admin ─────────────────────────────────────────────────────────
        composable(Screen.AdminHome.route) {
            AdminHomeScreen(navController, onThemeToggle, isDarkMode)
        }

        // ── Event Editor (Admin) ──────────────────────────────────────────
=======
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
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
        composable(
            route = Screen.EventEditor.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            EventEditorScreen(navController, eventId)
        }
<<<<<<< HEAD

        // ── Event Detail (User) ───────────────────────────────────────────
=======
        
        // Event Detail (User)
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            EventDetailScreen(navController, eventId)
        }

<<<<<<< HEAD
        // ── Ticket Purchase ───────────────────────────────────────────────
=======
        // Ticket Purchase (User)
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
        composable(
            route = Screen.TicketPurchase.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            TicketPurchaseScreen(navController, eventId)
        }

<<<<<<< HEAD
        // ── Event Tools ───────────────────────────────────────────────────
=======
        // Event Tools (Admin/Organiser)
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
        composable(
            route = Screen.Contribution.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            ContributionScreen(navController, eventId)
        }

        composable(
            route = Screen.GuestManager.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            GuestManagerScreen(navController, eventId)
        }

        composable(
            route = Screen.BudgetTracker.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            BudgetTrackerScreen(navController, eventId)
        }

        composable(
            route = Screen.WrapReport.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            WrapReportScreen(navController, eventId)
        }

<<<<<<< HEAD
        composable(
            route = Screen.Invitation.route,
            arguments = listOf(navArgument("eventId") {
                type = NavType.StringType; nullable = true; defaultValue = null
            })
        ) { backStackEntry ->
=======
        composable(Screen.Invitation.route) { backStackEntry ->
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
            val eventId = backStackEntry.arguments?.getString("eventId")
            InvitationScreen(navController, eventId)
        }

<<<<<<< HEAD
=======
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
        composable(Screen.Team.route) { TeamScreen(navController) }
    }
}
