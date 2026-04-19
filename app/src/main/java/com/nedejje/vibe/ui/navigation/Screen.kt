package com.nedejje.vibe.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home") // User Dashboard
    object AdminHome : Screen("admin_home") // Admin Dashboard
    object EventEditor : Screen("event_editor/{eventId}") {
        fun createRoute(eventId: String) = "event_editor/$eventId"
    }
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object TicketPurchase : Screen("ticket_purchase/{eventId}") {
        fun createRoute(eventId: String) = "ticket_purchase/$eventId"
    }
    object GuestManager : Screen("guest_manager")
    object BudgetTracker : Screen("budget_tracker")
    object Invitation : Screen("invitation")
    object Contribution : Screen("contribution")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object WrapReport : Screen("wrap_report")
    object Team : Screen("team")
}
