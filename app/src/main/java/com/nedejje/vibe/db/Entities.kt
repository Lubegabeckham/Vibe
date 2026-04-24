package com.nedejje.vibe.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ── Event ──────────────────────────────────────────────────────────────────────
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val location: String,
    val date: String,
    val description: String,
    val isFree: Boolean,
    val priceOrdinary: Long,
    val priceVIP: Long,
    val priceVVIP: Long,
    val createdAt: Long = System.currentTimeMillis()
)

// ── User ───────────────────────────────────────────────────────────────────────
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val isAdmin: Boolean = false,
    val passwordHash: String = "",   // store BCrypt hash in production; demo uses plain
    val createdAt: Long = System.currentTimeMillis()
)

// ── Guest ──────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "guests",
    foreignKeys = [
        ForeignKey(entity = EventEntity::class, parentColumns = ["id"], childColumns = ["eventId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UserEntity::class,  parentColumns = ["id"], childColumns = ["userId"],  onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("eventId"), Index("userId")]
)
data class GuestEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val userId: String?,           // null if walk-in / manually added
    val name: String,
    val email: String,
    val phone: String,
    val status: String,            // "Confirmed" | "Pending" | "Declined"
    val tag: String,               // "VIP" | "Regular" | "+1" etc.
    val dietaryRestrictions: String = "",
    val checkedIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// ── Ticket ─────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "tickets",
    foreignKeys = [
        ForeignKey(entity = EventEntity::class, parentColumns = ["id"], childColumns = ["eventId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UserEntity::class,  parentColumns = ["id"], childColumns = ["userId"],  onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("eventId"), Index("userId")]
)
data class TicketEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val userId: String,
    val tier: String,              // "Ordinary" | "VIP" | "VVIP" | "Free"
    val price: Long,               // 0 for free tickets
    val quantity: Int = 1,
    val purchasedAt: Long = System.currentTimeMillis(),
    val isUsed: Boolean = false
)

// ── Contribution (Potluck) ─────────────────────────────────────────────────────
@Entity(
    tableName = "contributions",
    foreignKeys = [
        ForeignKey(entity = EventEntity::class, parentColumns = ["id"], childColumns = ["eventId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("eventId")]
)
data class ContributionEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val itemName: String,
    val category: String, // FOOD, DRINKS, etc.
    val personClaimed: String? = null
)

// ── Budget Item ────────────────────────────────────────────────────────────────
@Entity(
    tableName = "budget_items",
    foreignKeys = [
        ForeignKey(entity = EventEntity::class, parentColumns = ["id"], childColumns = ["eventId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("eventId")]
)
data class BudgetItemEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val name: String,
    val amount: Double
)
