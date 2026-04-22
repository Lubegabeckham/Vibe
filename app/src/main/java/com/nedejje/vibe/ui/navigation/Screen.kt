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

    // ── Invitation — supports optional eventId so it works both as a
    //    standalone screen and driven by a real event ─────────────────────
    object Invitation : Screen("invitation?eventId={eventId}") {
        const val routeWithoutParam = "invitation"
        fun createRoute(eventId: String) = "invitation?eventId=$eventId"
    }

    // ── Wrap report — driven by event when eventId is provided ────────────
    object WrapReport : Screen("wrap_report?eventId={eventId}") {
        const val routeWithoutParam = "wrap_report"
        fun createRoute(eventId: String) = "wrap_report?eventId=$eventId"
    }

    // ── Organiser tools (no params) ────────────────────────────────────────
    object GuestManager  : Screen("guest_manager")
    object BudgetTracker : Screen("budget_tracker")
    object Contribution  : Screen("contribution")
    object Team          : Screen("team")
}