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
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

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

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets WHERE userId = :userId")
    fun observeByUser(userId: String): Flow<List<TicketEntity>>

    @Query("SELECT COUNT(*) FROM tickets WHERE eventId = :eventId")
    fun countByEvent(eventId: String): Flow<Int>

    @Query("SELECT SUM(price * quantity) FROM tickets WHERE eventId = :eventId")
    fun revenueByEvent(eventId: String): Flow<Long?>

    @Query("UPDATE tickets SET isUsed = :isUsed WHERE id = :id")
    suspend fun updateUsage(id: String, isUsed: Boolean)

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
