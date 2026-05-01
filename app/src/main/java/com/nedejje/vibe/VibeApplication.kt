package com.nedejje.vibe

import android.app.Application
import com.nedejje.vibe.db.UserEntity
import com.nedejje.vibe.db.VibeDatabase
import com.nedejje.vibe.repository.*
import com.nedejje.vibe.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

class AppContainer(application: Application) {
    private val db = VibeDatabase.getInstance(application)

    val eventRepository        = EventRepository(db.eventDao())
    val userRepository         = UserRepository(db.userDao())
    val guestRepository        = GuestRepository(db.guestDao())
    val ticketRepository       = TicketRepository(db.ticketDao())
    val budgetRepository       = BudgetRepository(db.budgetDao())
    val favoriteRepository     = FavoriteRepository(db.favoriteDao())
    val paymentRepository      = PaymentRepository(db.paymentDao())
    // NOTE: ContributionRepository removed — ContributionEntity/Dao no longer exist.
    // If you still need contribution features, re-add them as a separate entity+dao.
}

class VibeApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        SessionManager.init(this)
        // Ensure the fixed admin account exists (uses same SHA-256 as AuthViewModel)
        seedAdminAccount()
    }

    private fun seedAdminAccount() {
        CoroutineScope(Dispatchers.IO).launch {
            val adminEmail    = "admin@vibe.app"
            val adminPassword = "Admin@Vibe2025!"
            if (container.userRepository.getByEmail(adminEmail) == null) {
                container.userRepository.insert(
                    UserEntity(
                        id           = "vibe-admin-fixed-id",
                        name         = "Admin",
                        email        = adminEmail,
                        phone        = "",
                        isAdmin      = true,
                        passwordHash = hashPassword(adminPassword)
                    )
                )
            }
        }
    }

    /** SHA-256 hex — must match AuthViewModel.hashPassword exactly. */
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}