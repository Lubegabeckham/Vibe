package com.nedejje.vibe.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        EventEntity::class,
        UserEntity::class,
        GuestEntity::class,
        TicketEntity::class,
        ContributionEntity::class,
        BudgetItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class VibeDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao
    abstract fun guestDao(): GuestDao
    abstract fun ticketDao(): TicketDao
    abstract fun contributionDao(): ContributionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile private var INSTANCE: VibeDatabase? = null

        // Migration: v1 → v2 adds passwordHash column
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN passwordHash TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): VibeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VibeDatabase::class.java,
                    "vibe_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch { seedDatabase(database) }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDatabase(db: VibeDatabase) {
            val adminId = UUID.randomUUID().toString()
            val userId  = UUID.randomUUID().toString()
            val event1  = UUID.randomUUID().toString()
            val event2  = UUID.randomUUID().toString()
            val event3  = UUID.randomUUID().toString()

            // Seed users — demo: password stored as plain text; use BCrypt in production
            db.userDao().insert(UserEntity(
                id = adminId, name = "Admin Vibe", email = "admin@vibe.ug",
                phone = "+256700000000", isAdmin = true, passwordHash = "admin123"
            ))
            db.userDao().insert(UserEntity(
                id = userId, name = "Demo Guest", email = "guest@vibe.ug",
                phone = "+256700000001", isAdmin = false, passwordHash = "guest123"
            ))

            // Seed events
            db.eventDao().insertAll(listOf(
                EventEntity(
                    id = event1, title = "Nyege Nyege Festival",
                    location = "Jinja, Uganda", date = "Sept 4–7, 2025",
                    description = "The biggest music and arts festival in East Africa. Four days of non-stop music, art, and culture on the banks of the Nile.",
                    isFree = false, priceOrdinary = 150_000, priceVIP = 350_000, priceVVIP = 500_000
                ),
                EventEntity(
                    id = event2, title = "Blankets & Wine Kampala",
                    location = "Lugogo Cricket Oval, Kampala", date = "Sat, Oct 18, 2025 · 2:00 PM",
                    description = "Uganda's most beloved picnic-style music festival. Bring your blanket, your friends, and your appetite for great music.",
                    isFree = false, priceOrdinary = 100_000, priceVIP = 250_000, priceVVIP = 400_000
                ),
                EventEntity(
                    id = event3, title = "Ugandan Startup Expo 2025",
                    location = "Kampala Innovation Hub, Industrial Area", date = "Sun, Nov 2, 2025 · 10:00 AM",
                    description = "Meet the next generation of Ugandan entrepreneurs. Demo day, investor meetups, and workshops. Free and open to all.",
                    isFree = true, priceOrdinary = 0, priceVIP = 0, priceVVIP = 0
                )
            ))
        }
    }
}
