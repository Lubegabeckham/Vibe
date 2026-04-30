package com.nedejje.vibe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nedejje.vibe.db.*
import com.nedejje.vibe.repository.*
import com.nedejje.vibe.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

// ── AuthState ──────────────────────────────────────────────────────────────────
sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserEntity) : AuthState()
    data class Error(val message: String)    : AuthState()
}

// ── AuthViewModel ──────────────────────────────────────────────────────────────
class AuthViewModel(private val userRepo: UserRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            delay(500)
            val user = userRepo.getByEmail(email)
            if (user != null && user.passwordHash == password) {
                SessionManager.login(user)
                _authState.value = AuthState.Success(user)
            } else {
                _authState.value = AuthState.Error("Invalid credentials")
            }
        }
    }

    fun signup(name: String, email: String, phone: String, password: String, isAdmin: Boolean = false) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val newUser = UserEntity(
                id           = UUID.randomUUID().toString(),
                name         = name.trim(),
                email        = email.trim(),
                phone        = phone.trim(),
                isAdmin      = isAdmin,
                passwordHash = password 
            )
            userRepo.insert(newUser)
            SessionManager.login(newUser)
            _authState.value = AuthState.Success(newUser)
        }
    }

    fun sendPasswordReset(email: String) {
        // Simulated reset logic
    }

    class Factory(private val repo: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(repo) as T
    }
}

// ── HomeViewModel ──────────────────────────────────────────────────────────────
class HomeViewModel(
    private val repo: EventRepository,
    private val favRepo: FavoriteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val events: StateFlow<List<EventEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { q ->
            if (q.isBlank()) repo.allEvents else repo.search(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun toggleFavorite(eventId: String, isFav: Boolean) {
        viewModelScope.launch {
            favRepo.toggleFavorite(SessionManager.userId, eventId, isFav)
        }
    }

    class Factory(
        private val repo: EventRepository,
        private val favRepo: FavoriteRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(repo, favRepo) as T
    }
}

// ── EventDetailViewModel ───────────────────────────────────────────────────────
class EventDetailViewModel(
    private val eventRepo : EventRepository,
    private val ticketRepo: TicketRepository,
    private val favRepo: FavoriteRepository
) : ViewModel() {

    private val _event       = MutableStateFlow<EventEntity?>(null)
    private val _ticketCount = MutableStateFlow(0)
    private val _revenue     = MutableStateFlow(0L)

    val event:       StateFlow<EventEntity?> = _event.asStateFlow()
    val ticketCount: StateFlow<Int>          = _ticketCount.asStateFlow()
    val revenue:     StateFlow<Long>         = _revenue.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val isFavorite: StateFlow<Boolean> = _event.flatMapLatest { e ->
        if (e == null) flowOf(false)
        else favRepo.isFavorite(SessionManager.userId, e.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), false)

    fun load(eventId: String) {
        viewModelScope.launch { _event.value = eventRepo.getById(eventId) }
        viewModelScope.launch { ticketRepo.countPaidByEvent(eventId).collect  { _ticketCount.value = it } }
        viewModelScope.launch { ticketRepo.revenueByEvent(eventId).collect { _revenue.value = it ?: 0L } }
    }

    fun toggleFavorite() {
        val currentEvent = _event.value ?: return
        viewModelScope.launch {
            favRepo.toggleFavorite(SessionManager.userId, currentEvent.id, !isFavorite.value)
        }
    }

    class Factory(
        private val eventRepo : EventRepository,
        private val ticketRepo: TicketRepository,
        private val favRepo: FavoriteRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EventDetailViewModel(eventRepo, ticketRepo, favRepo) as T
    }
}

// ── EventEditorViewModel ───────────────────────────────────────────────────────
class EventEditorViewModel(private val repo: EventRepository) : ViewModel() {

    private val _event = MutableStateFlow<EventEntity?>(null)
    val event: StateFlow<EventEntity?> = _event.asStateFlow()

    fun load(eventId: String) {
        viewModelScope.launch { _event.value = repo.getById(eventId) }
    }

    fun save(
        id: String?, title: String, location: String, date: String,
        description: String, category: String, isFree: Boolean,
        priceOrdinary: Long, priceVIP: Long, priceVVIP: Long,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val entity = EventEntity(
                id           = id ?: UUID.randomUUID().toString(),
                title        = title.trim(),
                location     = location.trim(),
                date         = date.trim(),
                description  = description.trim(),
                category     = category,
                isFree       = isFree,
                priceOrdinary= priceOrdinary,
                priceVIP     = priceVIP,
                priceVVIP    = priceVVIP
            )
            if (id == null) repo.add(entity) else repo.update(entity)
            onDone()
        }
    }

    fun delete(id: String, onDone: () -> Unit) {
        viewModelScope.launch { repo.delete(id); onDone() }
    }

    class Factory(private val repo: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = EventEditorViewModel(repo) as T
    }
}

// ── TicketViewModel ────────────────────────────────────────────────────────────
class TicketViewModel(
    private val ticketRepo: TicketRepository,
    private val eventRepo: EventRepository,
    private val guestRepo: GuestRepository
) : ViewModel() {

    private val _event = MutableStateFlow<EventEntity?>(null)
    val event: StateFlow<EventEntity?> = _event.asStateFlow()

    val tickets = MutableStateFlow<List<TicketEntity>>(emptyList())

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _event.value = eventRepo.getById(eventId)
        }
    }

    fun loadForUser(userId: String) {
        viewModelScope.launch { ticketRepo.observeByUser(userId).collect { tickets.value = it } }
    }

    fun purchase(
        eventId: String, userId: String, tier: String,
        price: Long, quantity: Int = 1, status: String = "PAID", onDone: (String) -> Unit
    ) {
        viewModelScope.launch {
            val ticketId = ticketRepo.purchaseTicket(eventId, userId, tier, price, quantity, status)
            
            // Automatically add user to guest list for this event
            val user = SessionManager.currentUser.value
            if (user != null) {
                guestRepo.createGuest(
                    eventId = eventId,
                    name = user.name,
                    email = user.email,
                    phone = user.phone,
                    tag = tier,
                    dietaryRestrictions = "",
                    userId = userId
                )
            }
            
            onDone(ticketId)
        }
    }

    fun markUsed(ticketId: String) { 
        viewModelScope.launch { 
            ticketRepo.markAsUsed(ticketId)
            // Also update guest status if linked
            val ticket = ticketRepo.getById(ticketId)
            if (ticket != null) {
                guestRepo.observeByEvent(ticket.eventId).first().find { it.userId == ticket.userId }?.let { guest ->
                    guestRepo.checkIn(guest.id)
                }
            }
        } 
    }
    
    fun updateStatus(ticketId: String, status: String) {
        viewModelScope.launch { ticketRepo.updateStatus(ticketId, status) }
    }

    class Factory(
        private val ticketRepo: TicketRepository,
        private val eventRepo: EventRepository,
        private val guestRepo: GuestRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TicketViewModel(ticketRepo, eventRepo, guestRepo) as T
    }
}

// ── GuestManagerViewModel ──────────────────────────────────────────────────────
class GuestManagerViewModel(private val repo: GuestRepository) : ViewModel() {
    private val _eventId     = MutableStateFlow("")
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val guests: StateFlow<List<GuestEntity>> = combine(_eventId, _searchQuery) { id, q -> id to q }
            .debounce(300).flatMapLatest { (eventId, q) ->
                when {
                    eventId.isBlank() -> flowOf(emptyList())
                    q.isBlank()       -> repo.observeByEvent(eventId)
                    else              -> repo.searchInEvent(eventId, q)
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    val guestCount: StateFlow<Int> = _eventId.flatMapLatest { id -> 
        if (id.isBlank()) flowOf(0) else repo.countByEvent(id) 
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 0)

    val checkedInCount: StateFlow<Int> = _eventId.flatMapLatest { id -> 
        if (id.isBlank()) flowOf(0) else repo.countCheckedIn(id) 
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 0)

    fun setEventId(eventId: String) { _eventId.value = eventId }
    fun onSearchQueryChange(q: String) { _searchQuery.value = q }
    fun updateStatus(id: String, status: String) { viewModelScope.launch { repo.updateStatus(id, status) } }
    fun checkIn(id: String)  { viewModelScope.launch { repo.checkIn(id) } }
    fun checkOut(id: String) { viewModelScope.launch { repo.checkOut(id) } }
    fun addGuest(eventId: String, name: String, email: String, phone: String, tag: String, diet: String) {
        viewModelScope.launch { repo.createGuest(eventId, name, email, phone, tag, diet) }
    }
    fun deleteGuest(guest: GuestEntity) { viewModelScope.launch { repo.delete(guest) } }

    class Factory(private val repo: GuestRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = GuestManagerViewModel(repo) as T
    }
}

// ── ContributionViewModel ──────────────────────────────────────────────────────
class ContributionViewModel(private val repo: ContributionRepository) : ViewModel() {
    private val _eventId = MutableStateFlow("")
    @OptIn(ExperimentalCoroutinesApi::class)
    val contributions: StateFlow<List<ContributionEntity>> = _eventId
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeByEvent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    fun setEventId(id: String) { _eventId.value = id }
    fun addContribution(eventId: String, itemName: String, category: String) {
        viewModelScope.launch { repo.add(ContributionEntity(UUID.randomUUID().toString(), eventId, itemName, category)) }
    }
    fun claimItem(id: String, name: String)  { viewModelScope.launch { repo.updateClaim(id, name) } }
    fun unclaimItem(id: String)              { viewModelScope.launch { repo.updateClaim(id, null) } }
    fun deleteContribution(c: ContributionEntity) { viewModelScope.launch { repo.delete(c) } }

    class Factory(private val repo: ContributionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ContributionViewModel(repo) as T
    }
}

// ── BudgetViewModel ────────────────────────────────────────────────────────────
class BudgetViewModel(private val repo: BudgetRepository) : ViewModel() {
    private val _eventId = MutableStateFlow("")
    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<BudgetItemEntity>> = _eventId
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeByEvent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())
    @OptIn(ExperimentalCoroutinesApi::class)
    val totalSpend: StateFlow<Double> = _eventId
        .flatMapLatest { id -> if (id.isBlank()) flowOf(0.0) else repo.observeTotalByEvent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 0.0)

    fun setEventId(id: String) { _eventId.value = id }
    fun addItem(eventId: String, name: String, amount: Double) {
        viewModelScope.launch { repo.add(BudgetItemEntity(UUID.randomUUID().toString(), eventId, name.trim(), amount)) }
    }
    fun deleteItem(item: BudgetItemEntity) { viewModelScope.launch { repo.delete(item) } }

    class Factory(private val repo: BudgetRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = BudgetViewModel(repo) as T
    }
}

// ── WrapReportViewModel ────────────────────────────────────────────────────────
class WrapReportViewModel(
    private val eventRepo : EventRepository,
    private val guestRepo : GuestRepository,
    private val budgetRepo: BudgetRepository,
    private val ticketRepo: TicketRepository
) : ViewModel() {
    private val _eventId = MutableStateFlow("")
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val event: StateFlow<EventEntity?> = _eventId.flatMapLatest { id ->
        if (id.isBlank()) flowOf<EventEntity?>(null)
        else flow { emit(eventRepo.getById(id)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val guestCount: StateFlow<Int> = _eventId.flatMapLatest { id -> 
        if (id.isBlank()) flowOf(0) else guestRepo.countByEvent(id) 
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val checkedInCount: StateFlow<Int> = _eventId.flatMapLatest { id -> 
        if (id.isBlank()) flowOf(0) else guestRepo.countCheckedIn(id) 
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalSpend: StateFlow<Double> = _eventId
        .flatMapLatest { id -> if (id.isBlank()) flowOf(0.0) else budgetRepo.observeTotalByEvent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val ticketRevenue: StateFlow<Long> = _eventId.flatMapLatest { id -> 
        if (id.isBlank()) flowOf(0L) else ticketRepo.revenueByEvent(id).map { it ?: 0L } 
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), 0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val budgetItems: StateFlow<List<BudgetItemEntity>> = _eventId.flatMapLatest { id -> 
        if (id.isBlank()) flowOf(emptyList<BudgetItemEntity>()) else budgetRepo.observeByEvent(id) 
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    fun setEventId(id: String) { _eventId.value = id }

    class Factory(
        private val er : EventRepository,
        private val gr : GuestRepository,
        private val br: BudgetRepository,
        private val tr: TicketRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = 
            WrapReportViewModel(er, gr, br, tr) as T
    }
}

// ── InvitationViewModel ────────────────────────────────────────────────────────
class InvitationViewModel(private val eventRepo: EventRepository) : ViewModel() {
    private val _eventId = MutableStateFlow("")
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val event: StateFlow<EventEntity?> = _eventId.flatMapLatest { id ->
        if (id.isBlank()) flowOf<EventEntity?>(null)
        else flow { emit(eventRepo.getById(id)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), null)

    fun setEventId(id: String) { _eventId.value = id }

    class Factory(private val repo: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = InvitationViewModel(repo) as T
    }
}
