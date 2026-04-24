package com.nedejje.vibe.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VibeDao {

    // ── Events ─────────────────────────────────────────────────────────────
    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE title LIKE :query OR location LIKE :query")
    fun searchEvents(query: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    // ── Users ──────────────────────────────────────────────────────────────
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // ── Guests ─────────────────────────────────────────────────────────────
    @Query("SELECT * FROM guests WHERE eventId = :eventId")
    fun getGuestsForEvent(eventId: String): Flow<List<GuestEntity>>

    @Query("SELECT * FROM guests WHERE eventId = :eventId AND (name LIKE :query OR email LIKE :query)")
    fun searchGuestsInEvent(eventId: String, query: String): Flow<List<GuestEntity>>

    @Query("SELECT COUNT(*) FROM guests WHERE eventId = :eventId")
    fun getGuestCountForEvent(eventId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM guests WHERE eventId = :eventId AND checkedIn = 1")
    fun getCheckedInCountForEvent(eventId: String): Flow<Int>

    @Query("UPDATE guests SET status = :status WHERE id = :guestId")
    suspend fun updateGuestStatus(guestId: String, status: String)

    @Query("UPDATE guests SET checkedIn = :checkedIn WHERE id = :guestId")
    suspend fun updateGuestCheckInStatus(guestId: String, checkedIn: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuest(guest: GuestEntity)

    @Update
    suspend fun updateGuest(guest: GuestEntity)

    @Delete
    suspend fun deleteGuest(guest: GuestEntity)

    // ── Tickets ────────────────────────────────────────────────────────────
    @Query("SELECT * FROM tickets WHERE userId = :userId")
    fun getTicketsForUser(userId: String): Flow<List<TicketEntity>>

    @Query("SELECT COUNT(*) FROM tickets WHERE eventId = :eventId")
    fun getTicketCountForEvent(eventId: String): Flow<Int>

    @Query("SELECT SUM(price * quantity) FROM tickets WHERE eventId = :eventId")
    fun getRevenueForEvent(eventId: String): Flow<Long?>

    @Query("UPDATE tickets SET isUsed = :isUsed WHERE id = :ticketId")
    suspend fun updateTicketUsage(ticketId: String, isUsed: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity)
}
