package com.nedejje.vibe.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    fun getAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE title LIKE :query OR location LIKE :query")
    fun search(query: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: String): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>)

    @Update
    suspend fun update(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("UPDATE events SET isCancelled = :cancelled WHERE id = :id")
    suspend fun updateCancellation(id: String, cancelled: Boolean)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE name LIKE :query OR email LIKE :query")
    fun searchUsers(query: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isAdmin = 0")
    fun getAllNonAdmins(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)
}

@Dao
interface GuestDao {
    @Query("SELECT * FROM guests WHERE eventId = :eventId")
    fun observeByEvent(eventId: String): Flow<List<GuestEntity>>

    @Query("SELECT * FROM guests WHERE eventId = :eventId AND (name LIKE :query OR email LIKE :query)")
    fun searchInEvent(eventId: String, query: String): Flow<List<GuestEntity>>

    @Query("SELECT COUNT(*) FROM guests WHERE eventId = :eventId")
    fun countByEvent(eventId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM guests WHERE eventId = :eventId AND checkedIn = 1")
    fun countCheckedIn(eventId: String): Flow<Int>

    @Query("UPDATE guests SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE guests SET checkedIn = :checkedIn WHERE id = :id")
    suspend fun updateCheckInStatus(id: String, checkedIn: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(guest: GuestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(guests: List<GuestEntity>)

    @Update
    suspend fun update(guest: GuestEntity)

    @Delete
    suspend fun delete(guest: GuestEntity)
}

// Projection for per-tier aggregation used in WrapReport charts
data class TierBreakdown(val tier: String, val revenue: Long, val count: Int)

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets WHERE userId = :userId")
    fun observeByUser(userId: String): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE id = :id")
    suspend fun getById(id: String): TicketEntity?

    @Query("SELECT COUNT(*) FROM tickets WHERE eventId = :eventId AND status = 'PAID'")
    fun countPaidByEvent(eventId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tickets WHERE eventId = :eventId")
    fun countTotalByEvent(eventId: String): Flow<Int>

    @Query("SELECT SUM(price * quantity) FROM tickets WHERE eventId = :eventId AND status = 'PAID'")
    fun revenueByEvent(eventId: String): Flow<Long?>

    // Per-tier breakdown for WrapReport charts
    @Query("SELECT tier, SUM(price * quantity) as revenue, COUNT(*) as count FROM tickets WHERE eventId = :eventId AND status = 'PAID' GROUP BY tier")
    fun tierBreakdownByEvent(eventId: String): Flow<List<TierBreakdown>>

    @Query("UPDATE tickets SET isUsed = :isUsed WHERE id = :id")
    suspend fun updateUsage(id: String, isUsed: Boolean)

    @Query("UPDATE tickets SET isCancelled = :cancelled WHERE id = :id")
    suspend fun updateCancellation(id: String, cancelled: Boolean)

    @Query("UPDATE tickets SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ticket: TicketEntity)
}

@Dao
interface ContributionDao {
    @Query("SELECT * FROM contributions WHERE eventId = :eventId")
    fun observeByEvent(eventId: String): Flow<List<ContributionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contribution: ContributionEntity)

    @Update
    suspend fun update(contribution: ContributionEntity)

    @Delete
    suspend fun delete(contribution: ContributionEntity)

    @Query("UPDATE contributions SET personClaimed = :personClaimed WHERE id = :id")
    suspend fun updateClaim(id: String, personClaimed: String?)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_items WHERE eventId = :eventId")
    fun observeByEvent(eventId: String): Flow<List<BudgetItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: BudgetItemEntity)

    @Update
    suspend fun update(item: BudgetItemEntity)

    @Delete
    suspend fun delete(item: BudgetItemEntity)

    @Query("SELECT SUM(amount) FROM budget_items WHERE eventId = :eventId")
    fun observeTotalByEvent(eventId: String): Flow<Double?>
}

@Dao
interface FavoriteDao {
    @Query("SELECT eventId FROM favorites WHERE userId = :userId")
    fun observeFavorites(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Delete
    suspend fun delete(favorite: FavoriteEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND eventId = :eventId)")
    fun isFavorite(userId: String, eventId: String): Flow<Boolean>
}

@Dao
interface FollowDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun follow(follow: FollowEntity)

    @Delete
    suspend fun unfollow(follow: FollowEntity)

    @Query("SELECT followedId FROM follows WHERE followerId = :userId")
    fun getFollowing(userId: String): Flow<List<String>>

    @Query("SELECT followerId FROM follows WHERE followedId = :userId")
    fun getFollowers(userId: String): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerId = :followerId AND followedId = :followedId)")
    fun isFollowing(followerId: String, followedId: String): Flow<Boolean>
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE eventId = :eventId ORDER BY createdAt DESC")
    fun observeByEvent(eventId: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: ReviewEntity)

    @Delete
    suspend fun delete(review: ReviewEntity)

    @Query("SELECT AVG(rating) FROM reviews WHERE eventId = :eventId")
    fun getAverageRating(eventId: String): Flow<Double?>
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeByUser(userId: String): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentEntity)

    @Update
    suspend fun update(payment: PaymentEntity)

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getById(id: String): PaymentEntity?
}