package com.nedejje.vibe.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 1,
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
        @Volatile
        private var INSTANCE: VibeDatabase? = null

        fun getInstance(context: Context): VibeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VibeDatabase::class.java,
                    "vibe_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    seedDatabase(database)
                                }
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
            val event1Id = UUID.randomUUID().toString()
            val event2Id = UUID.randomUUID().toString()

            // Seed admin user
            db.userDao().insert(
                UserEntity(
                    id = adminId,
                    name = "Admin",
                    email = "admin@vibe.ug",
                    phone = "+256700000000",
                    isAdmin = true
                )
            )

            // Seed sample events
            db.eventDao().insertAll(
                listOf(
                    EventEntity(
                        id = event1Id,
                        title = "Kampala Jazz Night",
                        location = "Serena Hotel, Kampala",
                        date = "Sat, 14 Jun 2025 · 7:00 PM",
                        description = "An unforgettable evening of live jazz featuring top local and international artists.",
                        isFree = false,
                        priceOrdinary = 50_000,
                        priceVIP = 150_000,
                        priceVVIP = 300_000
                    ),
                    EventEntity(
                        id = event2Id,
                        title = "Ugandan Startup Expo",
                        location = "Kampala Innovation Hub",
                        date = "Sun, 22 Jun 2025 · 10:00 AM",
                        description = "Meet the next generation of Ugandan entrepreneurs and innovators. Free and open to all.",
                        isFree = true,
                        priceOrdinary = 0,
                        priceVIP = 0,
                        priceVVIP = 0
                    )
                )
            )
        }
    }
}
