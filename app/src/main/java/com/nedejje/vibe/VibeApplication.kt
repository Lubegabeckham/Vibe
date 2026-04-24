package com.nedejje.vibe

import android.app.Application
import com.nedejje.vibe.db.VibeDatabase
import com.nedejje.vibe.repository.*
import com.nedejje.vibe.session.SessionManager

// ---------------------------------------------------------------------------
// AppContainer — manual DI; swap for Hilt/Koin when the project grows
// ---------------------------------------------------------------------------
class AppContainer(application: Application) {
    private val db = VibeDatabase.getInstance(application)

    val eventRepository        = EventRepository(db.eventDao())
    val userRepository         = UserRepository(db.userDao())
    val guestRepository        = GuestRepository(db.guestDao())
    val ticketRepository       = TicketRepository(db.ticketDao())
    val contributionRepository = ContributionRepository(db.contributionDao())
    val budgetRepository       = BudgetRepository(db.budgetDao())
}

// ---------------------------------------------------------------------------
// VibeApplication
// ---------------------------------------------------------------------------
class VibeApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Initialise persistent session before any screen is shown
        SessionManager.init(this)
    }
}
