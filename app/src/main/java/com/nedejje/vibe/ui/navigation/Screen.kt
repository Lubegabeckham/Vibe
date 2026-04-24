package com.nedejje.vibe.ui.navigation

sealed class Screen(val route: String) {

    // ── Auth ───────────────────────────────────────────────────────────────
    object Splash    : Screen("splash")
    object Login     : Screen("login")
    object Signup    : Screen("signup")

    // ── User ───────────────────────────────────────────────────────────────
    object Home      : Screen("home")
    object Profile   : Screen("profile")
    object Settings  : Screen("settings")

    // ── Admin ──────────────────────────────────────────────────────────────
    object AdminHome : Screen("admin_home")

    // ── Event (parameterised) ──────────────────────────────────────────────
    object EventEditor : Screen("event_editor/{eventId}") {
        fun createRoute(eventId: String) = "event_editor/$eventId"
    }
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object TicketPurchase : Screen("ticket_purchase/{eventId}") {
        fun createRoute(eventId: String) = "ticket_purchase/$eventId"
    }

    // ── Event Tools (parameterised) ──────────────────────────────────────
    object Contribution : Screen("contribution/{eventId}") {
        fun createRoute(eventId: String) = "contribution/$eventId"
    }
    object GuestManager : Screen("guest_manager/{eventId}") {
        fun createRoute(eventId: String) = "guest_manager/$eventId"
    }
    object BudgetTracker : Screen("budget_tracker/{eventId}") {
        fun createRoute(eventId: String) = "budget_tracker/$eventId"
    }
    object WrapReport : Screen("wrap_report/{eventId}") {
        fun createRoute(eventId: String) = "wrap_report/$eventId"
    }

    // ── Invitation — supports optional eventId ────────────────────────────
    object Invitation : Screen("invitation?eventId={eventId}") {
        const val routeWithoutParam = "invitation"
        fun createRoute(eventId: String) = "invitation?eventId=$eventId"
    }

    // ── Other ────────────────────────────────────────────────────────────
    object Team : Screen("team")
}
