package com.nedejje.vibe.repository

import com.nedejje.vibe.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

// ---------------------------------------------------------------------------
// EventRepository
// ---------------------------------------------------------------------------
class EventRepository(private val dao: EventDao) {
    val allEvents: Flow<List<EventEntity>> = dao.getAll()
    fun search(query: String): Flow<List<EventEntity>> = dao.search("%$query%")
    suspend fun getById(id: String): EventEntity? = dao.getById(id)
    suspend fun add(event: EventEntity) = dao.insert(event)
    suspend fun update(event: EventEntity) = dao.update(event)
    suspend fun delete(id: String) {
        dao.getById(id)?.let { dao.delete(it) }
    }
}

// ---------------------------------------------------------------------------
// UserRepository
// ---------------------------------------------------------------------------
class UserRepository(private val dao: UserDao) {
    suspend fun getById(id: String): UserEntity? = dao.getById(id)
    suspend fun getByEmail(email: String): UserEntity? = dao.getByEmail(email)
    suspend fun insert(user: UserEntity) = dao.insert(user)
}

// ---------------------------------------------------------------------------
// GuestRepository
// ---------------------------------------------------------------------------
class GuestRepository(private val dao: GuestDao) {
    fun observeByEvent(eventId: String): Flow<List<GuestEntity>> = dao.observeByEvent(eventId)
    fun searchInEvent(eventId: String, query: String): Flow<List<GuestEntity>> = dao.searchInEvent(eventId, "%$query%")
    fun countByEvent(eventId: String): Flow<Int> = dao.countByEvent(eventId)
    fun countCheckedIn(eventId: String): Flow<Int> = dao.countCheckedIn(eventId)
    suspend fun updateStatus(guestId: String, status: String) = dao.updateStatus(guestId, status)
    suspend fun checkIn(guestId: String) = dao.updateCheckInStatus(guestId, true)
    suspend fun checkOut(guestId: String) = dao.updateCheckInStatus(guestId, false)
    suspend fun createGuest(eventId: String, name: String, email: String, phone: String, tag: String, dietaryRestrictions: String) {
        val guest = GuestEntity(id = UUID.randomUUID().toString(), eventId = eventId, userId = null, name = name, email = email, phone = phone, status = "Pending", tag = tag, dietaryRestrictions = dietaryRestrictions)
        dao.insert(guest)
    }
    suspend fun delete(guest: GuestEntity) = dao.delete(guest)
}

// ---------------------------------------------------------------------------
// TicketRepository
// ---------------------------------------------------------------------------
class TicketRepository(private val dao: TicketDao) {
    fun observeByUser(userId: String): Flow<List<TicketEntity>> = dao.observeByUser(userId)
    fun countByEvent(eventId: String): Flow<Int> = dao.countByEvent(eventId)
    fun revenueByEvent(eventId: String): Flow<Long?> = dao.revenueByEvent(eventId)
    suspend fun purchaseTicket(eventId: String, userId: String, tier: String, price: Long, quantity: Int): String {
        val id = UUID.randomUUID().toString()
        val ticket = TicketEntity(id = id, eventId = eventId, userId = userId, tier = tier, price = price, quantity = quantity)
        dao.insert(ticket)
        return id
    }
    suspend fun markAsUsed(ticketId: String) = dao.updateUsage(ticketId, true)
}

// ---------------------------------------------------------------------------
// ContributionRepository
// ---------------------------------------------------------------------------
class ContributionRepository(private val dao: ContributionDao) {
    fun observeByEvent(eventId: String): Flow<List<ContributionEntity>> = dao.observeByEvent(eventId)
    suspend fun add(contribution: ContributionEntity) = dao.insert(contribution)
    suspend fun update(contribution: ContributionEntity) = dao.update(contribution)
    suspend fun delete(contribution: ContributionEntity) = dao.delete(contribution)
    suspend fun updateClaim(id: String, personName: String?) = dao.updateClaim(id, personName)
}

// ---------------------------------------------------------------------------
// BudgetRepository
// ---------------------------------------------------------------------------
class BudgetRepository(private val dao: BudgetDao) {
    fun observeByEvent(eventId: String): Flow<List<BudgetItemEntity>> = dao.observeByEvent(eventId)
    fun observeTotalByEvent(eventId: String): Flow<Double> = dao.observeTotalByEvent(eventId).map { it ?: 0.0 }
    suspend fun add(item: BudgetItemEntity) = dao.insert(item)
    suspend fun update(item: BudgetItemEntity) = dao.update(item)
    suspend fun delete(item: BudgetItemEntity) = dao.delete(item)
}
