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
        BudgetItemEntity::class,
        FavoriteEntity::class,
        FollowEntity::class,
        ReviewEntity::class,
        PaymentEntity::class
    ],
    version = 6, // Bumped to 6 for the new social/payment tables
    exportSchema = false
)
abstract class VibeDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao
    abstract fun guestDao(): GuestDao
    abstract fun ticketDao(): TicketDao
    abstract fun contributionDao(): ContributionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun followDao(): FollowDao
    abstract fun reviewDao(): ReviewDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile private var INSTANCE: VibeDatabase? = null

        fun getInstance(context: Context): VibeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VibeDatabase::class.java,
                    "vibe_database"
                )
                    .fallbackToDestructiveMigration()
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

            db.userDao().insert(UserEntity(
                id = adminId, name = "Admin Vibe", email = "admin@vibe.ug",
                phone = "+256700000000", isAdmin = true, passwordHash = "admin123"
            ))
            db.userDao().insert(UserEntity(
                id = userId, name = "Demo Guest", email = "guest@vibe.ug",
                phone = "+256700000001", isAdmin = false, passwordHash = "guest123"
            ))

            val events = mutableListOf<EventEntity>()
            
            // ── Music & Concerts
            val music = listOf(
                "Nyege Nyege Festival" to "Jinja, Nile Discovery Beach",
                "Jazz on the Nile" to "Serena Hotel, Kampala",
                "Afrobeat Night" to "Cricket Oval, Lugogo",
                "Reggae Waves" to "Entebbe Zoo Beach",
                "Classical Gala" to "Sheraton Gardens",
                "Gospel Groove" to "Kololo Ceremonial Grounds",
                "Hip Hop Summit" to "Munyonyo Resort",
                "Indie Soul Session" to "Design Hub, Bugolobi",
                "Rock in Kampala" to "Kyadondo Rugby Club",
                "Piano & Wine" to "Skyz Hotel, Naguru"
            )
            music.forEachIndexed { i, (title, loc) ->
                events.add(EventEntity(
                    id = UUID.randomUUID().toString(), title = title, location = loc,
                    date = "Sept ${10 + i}, 2026", description = "A massive music celebration.",
                    isFree = false, priceOrdinary = 50000, priceVIP = 150000, priceVVIP = 300000
                ))
            }

            // ── Tech & Startups
            val tech = listOf(
                "Ugandan Startup Expo" to "Kampala Innovation Hub",
                "AI & Robotics Summit" to "Makerere University",
                "Blockchain Conference" to "Protea Hotel",
                "Fintech Forum" to "Stanbic Incubator",
                "Cyber Security Lab" to "Outbox Hub",
                "EdTech Connect" to "The Innovation Village",
                "Cloud Computing Day" to "MTN Nyonyi Gardens",
                "Developer Fest 2025" to "Ndejje University",
                "Data Science Meetup" to "Mubajje Plaza",
                "IoT Workshop" to "Greenbridge Institute"
            )
            tech.forEachIndexed { i, (title, loc) ->
                events.add(EventEntity(
                    id = UUID.randomUUID().toString(), title = title, location = loc,
                    date = "Oct ${5 + i}, 2026", description = "Future-proofing our tech ecosystem.",
                    isFree = i % 3 == 0, priceOrdinary = 20000, priceVIP = 100000, priceVVIP = 200000
                ))
            }

            // ── Sports & Fitness
            val sports = listOf(
                "Kampala City Run" to "Independence Park",
                "Golf Pro Am" to "Uganda Golf Club",
                "Rugby 7s Series" to "Kyadondo Stadium",
                "Boxing Fight Night" to "Lugogo Indoor Arena",
                "Swimming Championships" to "Greenhill Academy",
                "Cyclists Challenge" to "Entebbe Expressway",
                "Yoga in the Park" to "Centenary Park",
                "Inter-University Games" to "Namboole Stadium",
                "Cricket Carnival" to "Lugogo Cricket Oval",
                "E-Sports Tournament" to "Garden City Mall"
            )
            sports.forEachIndexed { i, (title, loc) ->
                events.add(EventEntity(
                    id = UUID.randomUUID().toString(), title = title, location = loc,
                    date = "Nov ${2 + i}, 2026", description = "Energy, fitness, and competition.",
                    isFree = i % 4 == 0, priceOrdinary = 10000, priceVIP = 50000, priceVVIP = 100000
                ))
            }

            // ── Food & Culture
            val food = listOf(
                "Rolex Festival" to "National Theatre Park",
                "Kampala Restaurant Week" to "Various Locations",
                "Wine & Cheese Tasting" to "Le Chateau, Nsambya",
                "Cultural Expo" to "Uganda Museum",
                "Street Food Gala" to "Parliament Avenue",
                "Oktoberfest Kampala" to "Uganda Museum Grounds",
                "Organic Farmers Market" to "Kisementi",
                "Traditional Dance Fest" to "Ndere Centre",
                "Coffee Brewers Summit" to "Bugolobi Coffee Shop",
                "Seafood Sunday" to "Entebbe Pier"
            )
            food.forEachIndexed { i, (title, loc) ->
                events.add(EventEntity(
                    id = UUID.randomUUID().toString(), title = title, location = loc,
                    date = "Dec ${1 + i}, 2026", description = "Taste the vibrant vibes of Uganda.",
                    isFree = i % 2 == 0, priceOrdinary = 15000, priceVIP = 60000, priceVVIP = 120000
                ))
            }

            db.eventDao().insertAll(events)
        }
    }
}
