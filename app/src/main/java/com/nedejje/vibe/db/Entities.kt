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
    val category: String = "All",
    val isFree: Boolean,
    val priceOrdinary: Long,
    val priceVIP: Long,
    val priceVVIP: Long,
    val isCancelled: Boolean = false,
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
    val passwordHash: String = "",
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
    val userId: String?,
    val name: String,
    val email: String,
    val phone: String,
    val status: String,            // "Confirmed" | "Pending" | "Declined"
    val tag: String,               // "VIP" | "Regular" | "Staff" etc.
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
    val tier: String,
    val price: Long,
    val quantity: Int = 1,
    val purchasedAt: Long = System.currentTimeMillis(),
    val isUsed: Boolean = false,
    val isCancelled: Boolean = false,
    val status: String = "PAID",   // "PAID" | "PENDING"
    val paymentId: String? = null
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
    val category: String,
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

// ── Favorite ──────────────────────────────────────────────────────────────────
@Entity(
    tableName = "favorites",
    primaryKeys = ["userId", "eventId"],
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = EventEntity::class, parentColumns = ["id"], childColumns = ["eventId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("eventId")]
)
data class FavoriteEntity(
    val userId: String,
    val eventId: String,
    val savedAt: Long = System.currentTimeMillis()
)

// ── Social ────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "follows",
    primaryKeys = ["followerId", "followedId"],
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["followerId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["followedId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("followedId")]
)
data class FollowEntity(
    val followerId: String,
    val followedId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class,  parentColumns = ["id"], childColumns = ["userId"],  onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = EventEntity::class, parentColumns = ["id"], childColumns = ["eventId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("userId"), Index("eventId")]
)
data class ReviewEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val eventId: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)

// ── Payments ──────────────────────────────────────────────────────────────────
@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Long,
    val provider: String,
    val phoneNumber: String,
    val status: String,
    val createdAt: Long = System.currentTimeMillis()
)
