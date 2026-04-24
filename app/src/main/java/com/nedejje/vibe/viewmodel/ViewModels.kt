package com.nedejje.vibe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nedejje.vibe.db.*
import com.nedejje.vibe.repository.*
<<<<<<< HEAD
import com.nedejje.vibe.session.SessionManager
=======
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

<<<<<<< HEAD
// ── AuthState ──────────────────────────────────────────────────────────────────
sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserEntity) : AuthState()
    data class Error(val message: String)    : AuthState()
}

// ── AuthViewModel ──────────────────────────────────────────────────────────────
=======
// ---------------------------------------------------------------------------
// AuthState — for Login/Signup
// ---------------------------------------------------------------------------
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserEntity) : AuthState()
    data class Error(val message: String) : AuthState()
}

// ---------------------------------------------------------------------------
// AuthViewModel — handles Login & Signup
// ---------------------------------------------------------------------------
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
class AuthViewModel(private val userRepo: UserRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
<<<<<<< HEAD
            delay(800)
            val user = userRepo.getByEmail(email)
            if (user != null) {
                // Demo: plain-text comparison. Production: use BCrypt.checkpw(password, user.passwordHash)
                if (password.isBlank()) {
                    _authState.value = AuthState.Error("Password cannot be empty.")
                } else if (user.passwordHash.isNotBlank() && user.passwordHash != password) {
                    _authState.value = AuthState.Error("Incorrect password. Please try again.")
                } else {
                    SessionManager.login(user)
                    _authState.value = AuthState.Success(user)
                }
            } else {
                _authState.value = AuthState.Error("No account found. Please sign up.")
=======
            delay(1000) // Simulate network delay
            
            // For now, we accept any non-empty password for users in our DB
            // In a real app, you'd check a hashed password.
            val user = userRepo.getByEmail(email)
            if (user != null) {
                _authState.value = AuthState.Success(user)
            } else {
                _authState.value = AuthState.Error("User not found. Please sign up.")
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
            }
        }
    }

<<<<<<< HEAD
    fun signup(name: String, email: String, phone: String, password: String, isAdmin: Boolean = false) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            delay(800)
=======
    fun signup(name: String, email: String, phone: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            delay(1000)
            
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
            val existing = userRepo.getByEmail(email)
            if (existing != null) {
                _authState.value = AuthState.Error("Email already registered.")
            } else {
                val newUser = UserEntity(
<<<<<<< HEAD
                    id           = UUID.randomUUID().toString(),
                    name         = name.trim(),
                    email        = email.trim(),
                    phone        = phone.trim(),
                    isAdmin      = isAdmin,
                    passwordHash = password  // Demo: store as-is. Production: BCrypt.hashpw(password, BCrypt.gensalt())
                )
                userRepo.insert(newUser)
                SessionManager.login(newUser)
=======
                    id = UUID.randomUUID().toString(),
                    name = name,
                    email = email,
                    phone = phone,
                    isAdmin = email.contains("admin") // Simple rule for demo
                )
                userRepo.insert(newUser)
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
                _authState.value = AuthState.Success(newUser)
            }
        }
    }

<<<<<<< HEAD
    fun sendPasswordReset(email: String) { /* Integrate Firebase / email SDK here */ }

    class Factory(private val repo: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = AuthViewModel(repo) as T
    }
}

// ── HomeViewModel ──────────────────────────────────────────────────────────────
=======
    fun sendPasswordReset(email: String) {
        // Mock functionality
    }

    class Factory(private val repo: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            AuthViewModel(repo) as T
    }
}

// ---------------------------------------------------------------------------
// HomeViewModel — drives HomeScreen
// ---------------------------------------------------------------------------
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
class HomeViewModel(private val eventRepo: EventRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val events: StateFlow<List<EventEntity>> = _searchQuery
        .debounce(300)
<<<<<<< HEAD
        .flatMapLatest { q ->
            if (q.isBlank()) eventRepo.allEvents else eventRepo.search(q)
=======
        .flatMapLatest { query ->
            if (query.isBlank()) eventRepo.allEvents
            else eventRepo.search(query)
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    class Factory(private val repo: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
<<<<<<< HEAD
        override fun <T : ViewModel> create(modelClass: Class<T>) = HomeViewModel(repo) as T
    }
}

// ── EventDetailViewModel ───────────────────────────────────────────────────────
class EventDetailViewModel(
    private val eventRepo : EventRepository,
    private val ticketRepo: TicketRepository
) : ViewModel() {

    private val _event       = MutableStateFlow<EventEntity?>(null)
    private val _ticketCount = MutableStateFlow(0)
    private val _revenue     = MutableStateFlow(0L)

    val event:       StateFlow<EventEntity?> = _event.asStateFlow()
    val ticketCount: StateFlow<Int>          = _ticketCount.asStateFlow()
    val revenue:     StateFlow<Long>         = _revenue.asStateFlow()

    fun load(eventId: String) {
        viewModelScope.launch { _event.value = eventRepo.getById(eventId) }
        viewModelScope.launch { ticketRepo.countByEvent(eventId).collect  { _ticketCount.value = it } }
        viewModelScope.launch { ticketRepo.revenueByEvent(eventId).collect { _revenue.value = it ?: 0L } }
    }

    class Factory(
        private val eventRepo : EventRepository,
=======
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            HomeViewModel(repo) as T
    }
}

// ---------------------------------------------------------------------------
// EventDetailViewModel — drives EventDetailScreen
// ---------------------------------------------------------------------------
class EventDetailViewModel(
    private val eventRepo: EventRepository,
    private val ticketRepo: TicketRepository
) : ViewModel() {

    private val _event = MutableStateFlow<EventEntity?>(null)
    val event: StateFlow<EventEntity?> = _event.asStateFlow()

    private val _ticketCount = MutableStateFlow(0)
    val ticketCount: StateFlow<Int> = _ticketCount.asStateFlow()

    private val _revenue = MutableStateFlow(0L)
    val revenue: StateFlow<Long> = _revenue.asStateFlow()

    fun load(eventId: String) {
        viewModelScope.launch {
            _event.value = eventRepo.getById(eventId)
        }
        viewModelScope.launch {
            ticketRepo.countByEvent(eventId).collect { _ticketCount.value = it }
        }
        viewModelScope.launch {
            ticketRepo.revenueByEvent(eventId).collect { _revenue.value = it ?: 0L }
        }
    }

    class Factory(
        private val eventRepo: EventRepository,
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
        private val ticketRepo: TicketRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            EventDetailViewModel(eventRepo, ticketRepo) as T
    }
}

<<<<<<< HEAD
// ── EventEditorViewModel ───────────────────────────────────────────────────────
=======
// ---------------------------------------------------------------------------
// EventEditorViewModel — drives EventEditorScreen
// ---------------------------------------------------------------------------
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
class EventEditorViewModel(private val repo: EventRepository) : ViewModel() {

    private val _event = MutableStateFlow<EventEntity?>(null)
    val event: StateFlow<EventEntity?> = _event.asStateFlow()

    fun load(eventId: String) {
<<<<<<< HEAD
        viewModelScope.launch { _event.value = repo.getById(eventId) }
    }

    fun save(
        id: String?, title: String, location: String, date: String,
        description: String, isFree: Boolean,
        priceOrdinary: Long, priceVIP: Long, priceVVIP: Long,
=======
        viewModelScope.launch {
            _event.value = repo.getById(eventId)
        }
    }

    fun save(
        id: String?,
        title: String,
        location: String,
        date: String,
        description: String,
        isFree: Boolean,
        priceOrdinary: Long,
        priceVIP: Long,
        priceVVIP: Long,
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val entity = EventEntity(
<<<<<<< HEAD
                id           = id ?: UUID.randomUUID().toString(),
                title        = title.trim(),
                location     = location.trim(),
                date         = date.trim(),
                description  = description.trim(),
                isFree       = isFree,
                priceOrdinary= priceOrdinary,
                priceVIP     = priceVIP,
                priceVVIP    = priceVVIP
=======
                id = id ?: UUID.randomUUID().toString(),
                title = title.trim(),
                location = location.trim(),
                date = date.trim(),
                description = description.trim(),
                isFree = isFree,
                priceOrdinary = priceOrdinary,
                priceVIP = priceVIP,
                priceVVIP = priceVVIP
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
            )
            if (id == null) repo.add(entity) else repo.update(entity)
            onDone()
        }
    }

    fun delete(id: String, onDone: () -> Unit) {
<<<<<<< HEAD
        viewModelScope.launch { repo.delete(id); onDone() }
=======
        viewModelScope.launch {
            repo.delete(id)
            onDone()
        }
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
    }

    class Factory(private val repo: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
<<<<<<< HEAD
        override fun <T : ViewModel> create(modelClass: Class<T>) = EventEditorViewModel(repo) as T
    }
}

// ── GuestManagerViewModel ──────────────────────────────────────────────────────
class GuestManagerViewModel(private val repo: GuestRepository) : ViewModel() {

    private val _eventId     = MutableStateFlow("")
=======
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            EventEditorViewModel(repo) as T
    }
}

// ---------------------------------------------------------------------------
// GuestManagerViewModel — drives GuestManagerScreen
// ---------------------------------------------------------------------------
class GuestManagerViewModel(private val repo: GuestRepository) : ViewModel() {

    private val _eventId = MutableStateFlow("")
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
<<<<<<< HEAD
    val guests: StateFlow<List<GuestEntity>> =
        combine(_eventId, _searchQuery) { id, q -> id to q }
            .debounce(300)
            .flatMapLatest { (eventId, q) ->
                when {
                    eventId.isBlank() -> flowOf(emptyList())
                    q.isBlank()       -> repo.observeByEvent(eventId)
                    else              -> repo.searchInEvent(eventId, q)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val guestCount: StateFlow<Int> = _eventId
        .flatMapLatest { id -> if (id.isBlank()) flowOf(0) else repo.countByEvent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val checkedInCount: StateFlow<Int> = _eventId
        .flatMapLatest { id -> if (id.isBlank()) flowOf(0) else repo.countCheckedIn(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

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
        override fun <T : ViewModel> create(modelClass: Class<T>) = GuestManagerViewModel(repo) as T
    }
}

// ── TicketViewModel ────────────────────────────────────────────────────────────
class TicketViewModel(private val repo: TicketRepository) : ViewModel() {

    val tickets = MutableStateFlow<List<TicketEntity>>(emptyList())

    fun loadForUser(userId: String) {
        viewModelScope.launch { repo.observeByUser(userId).collect { tickets.value = it } }
    }

    fun purchase(
        eventId: String, userId: String, tier: String,
        price: Long, quantity: Int = 1, onDone: (String) -> Unit
    ) {
        viewModelScope.launch { onDone(repo.purchaseTicket(eventId, userId, tier, price, quantity)) }
    }

    fun markUsed(ticketId: String) { viewModelScope.launch { repo.markAsUsed(ticketId) } }

    class Factory(private val repo: TicketRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = TicketViewModel(repo) as T
    }
}

// ── ContributionViewModel ──────────────────────────────────────────────────────
class ContributionViewModel(private val repo: ContributionRepository) : ViewModel() {

    private val _eventId = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val contributions: StateFlow<List<ContributionEntity>> = _eventId
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeByEvent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setEventId(id: String) { _eventId.value = id }

    fun addContribution(eventId: String, itemName: String, category: String) {
        viewModelScope.launch {
            repo.add(ContributionEntity(UUID.randomUUID().toString(), eventId, itemName, category))
        }
    }

    fun claimItem(id: String, name: String)  { viewModelScope.launch { repo.updateClaim(id, name) } }
    fun unclaimItem(id: String)              { viewModelScope.launch { repo.updateClaim(id, null) } }
    fun deleteContribution(c: ContributionEntity) { viewModelScope.launch { repo.delete(c) } }

    class Factory(private val repo: ContributionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = ContributionViewModel(repo) as T
    }
}

// ── BudgetViewModel ────────────────────────────────────────────────────────────
=======
    val guests: StateFlow<List<GuestEntity>> = combine(_eventId, _searchQuery) { id, q -> id to q }
        .debounce(300)
        .flatMapLatest { (eventId, query) ->
            if (eventId.isBlank()) flowOf(emptyList())
            else if (query.isBlank()) repo.observeByEvent(eventId)
            else repo.searchInEvent(eventId, query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val guestCount: StateFlow<Int> = _eventId
        .flatMapLatest { id ->
            if (id.isBlank()) flowOf(0) else repo.countByEvent(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val checkedInCount: StateFlow<Int> = _eventId
        .flatMapLatest { id ->
            if (id.isBlank()) flowOf(0) else repo.countCheckedIn(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setEventId(eventId: String) { _eventId.value = eventId }

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun updateStatus(guestId: String, status: String) {
        viewModelScope.launch { repo.updateStatus(guestId, status) }
    }

    fun checkIn(guestId: String) {
        viewModelScope.launch { repo.checkIn(guestId) }
    }

    fun checkOut(guestId: String) {
        viewModelScope.launch { repo.checkOut(guestId) }
    }

    fun addGuest(
        eventId: String,
        name: String,
        email: String,
        phone: String,
        tag: String,
        dietaryRestrictions: String
    ) {
        viewModelScope.launch {
            repo.createGuest(
                eventId = eventId,
                name = name,
                email = email,
                phone = phone,
                tag = tag,
                dietaryRestrictions = dietaryRestrictions
            )
        }
    }

    fun deleteGuest(guest: GuestEntity) {
        viewModelScope.launch { repo.delete(guest) }
    }

    class Factory(private val repo: GuestRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            GuestManagerViewModel(repo) as T
    }
}

// ---------------------------------------------------------------------------
// TicketViewModel — drives TicketPurchaseScreen
// ---------------------------------------------------------------------------
class TicketViewModel(private val repo: TicketRepository) : ViewModel() {

    val tickets: MutableStateFlow<List<TicketEntity>> = MutableStateFlow(emptyList())

    fun loadForUser(userId: String) {
        viewModelScope.launch {
            repo.observeByUser(userId).collect { tickets.value = it }
        }
    }

    fun purchase(
        eventId: String,
        userId: String,
        tier: String,
        price: Long,
        quantity: Int = 1,
        onDone: (ticketId: String) -> Unit
    ) {
        viewModelScope.launch {
            val id = repo.purchaseTicket(eventId, userId, tier, price, quantity)
            onDone(id)
        }
    }

    fun markUsed(ticketId: String) {
        viewModelScope.launch { repo.markAsUsed(ticketId) }
    }

    class Factory(private val repo: TicketRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            TicketViewModel(repo) as T
    }
}

// ---------------------------------------------------------------------------
// ContributionViewModel — drives ContributionScreen
// ---------------------------------------------------------------------------
class ContributionViewModel(private val repo: ContributionRepository) : ViewModel() {

    private val _eventId = MutableStateFlow("")
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val contributions: StateFlow<List<ContributionEntity>> = _eventId
        .flatMapLatest { id ->
            if (id.isBlank()) flowOf(emptyList())
            else repo.observeByEvent(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setEventId(eventId: String) { _eventId.value = eventId }

    fun addContribution(eventId: String, itemName: String, category: String) {
        viewModelScope.launch {
            val entity = ContributionEntity(
                id = UUID.randomUUID().toString(),
                eventId = eventId,
                itemName = itemName,
                category = category
            )
            repo.add(entity)
        }
    }

    fun claimItem(contributionId: String, personName: String) {
        viewModelScope.launch {
            repo.updateClaim(contributionId, personName)
        }
    }

    fun unclaimItem(contributionId: String) {
        viewModelScope.launch {
            repo.updateClaim(contributionId, null)
        }
    }

    fun deleteContribution(contribution: ContributionEntity) {
        viewModelScope.launch {
            repo.delete(contribution)
        }
    }

    class Factory(private val repo: ContributionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            ContributionViewModel(repo) as T
    }
}

// ---------------------------------------------------------------------------
// BudgetViewModel — drives BudgetTrackerScreen
// ---------------------------------------------------------------------------
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
class BudgetViewModel(private val repo: BudgetRepository) : ViewModel() {

    private val _eventId = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<BudgetItemEntity>> = _eventId
<<<<<<< HEAD
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.observeByEvent(id) }
=======
        .flatMapLatest { id ->
            if (id.isBlank()) flowOf(emptyList())
            else repo.observeByEvent(id)
        }
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalSpend: StateFlow<Double> = _eventId
<<<<<<< HEAD
        .flatMapLatest { id -> if (id.isBlank()) flowOf(0.0) else repo.observeTotalByEvent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun setEventId(id: String) { _eventId.value = id }

    fun addItem(eventId: String, name: String, amount: Double) {
        viewModelScope.launch {
            repo.add(BudgetItemEntity(UUID.randomUUID().toString(), eventId, name.trim(), amount))
        }
    }

    fun deleteItem(item: BudgetItemEntity) { viewModelScope.launch { repo.delete(item) } }

    class Factory(private val repo: BudgetRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = BudgetViewModel(repo) as T
    }
}

// ── WrapReportViewModel ────────────────────────────────────────────────────────
class WrapReportViewModel(
    private val eventRepo : EventRepository,
    private val guestRepo : GuestRepository,
=======
        .flatMapLatest { id ->
            if (id.isBlank()) flowOf(0.0)
            else repo.observeTotalByEvent(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun setEventId(eventId: String) { _eventId.value = eventId }

    fun addItem(eventId: String, name: String, amount: Double) {
        viewModelScope.launch {
            val item = BudgetItemEntity(
                id = UUID.randomUUID().toString(),
                eventId = eventId,
                name = name.trim(),
                amount = amount
            )
            repo.add(item)
        }
    }

    fun deleteItem(item: BudgetItemEntity) {
        viewModelScope.launch { repo.delete(item) }
    }

    class Factory(private val repo: BudgetRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            BudgetViewModel(repo) as T
    }
}

// ---------------------------------------------------------------------------
// WrapReportViewModel — drives WrapReportScreen
// ---------------------------------------------------------------------------
class WrapReportViewModel(
    private val eventRepo: EventRepository,
    private val guestRepo: GuestRepository,
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
    private val budgetRepo: BudgetRepository,
    private val ticketRepo: TicketRepository
) : ViewModel() {

    private val _eventId = MutableStateFlow("")

<<<<<<< HEAD
    @OptIn(ExperimentalCoroutinesApi::class)
    val event         = _eventId.flatMapLatest { id -> flowOf(if (id.isBlank()) null else eventRepo.getById(id)) }
                                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val guestCount    = _eventId.flatMapLatest { id -> if (id.isBlank()) flowOf(0)   else guestRepo.countByEvent(id) }
                                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val checkedIn     = _eventId.flatMapLatest { id -> if (id.isBlank()) flowOf(0)   else guestRepo.countCheckedIn(id) }
                                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val totalSpend    = _eventId.flatMapLatest { id -> if (id.isBlank()) flowOf(0.0) else budgetRepo.observeTotalByEvent(id) }
                                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)
    val ticketRevenue = _eventId.flatMapLatest { id -> if (id.isBlank()) flowOf(0L)  else ticketRepo.revenueByEvent(id).map { it ?: 0L } }
                                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)
    val budgetItems   = _eventId.flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else budgetRepo.observeByEvent(id) }
                                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setEventId(id: String) { _eventId.value = id }

    class Factory(
        private val eventRepo : EventRepository,
        private val guestRepo : GuestRepository,
=======
    val event = _eventId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(null) else flowOf(eventRepo.getById(id))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val guestCount = _eventId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(0) else guestRepo.countByEvent(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val checkedInCount = _eventId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(0) else guestRepo.countCheckedIn(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalSpend = _eventId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(0.0) else budgetRepo.observeTotalByEvent(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val ticketRevenue = _eventId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(0L) else ticketRepo.revenueByEvent(id).map { it ?: 0L }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val budgetItems = _eventId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(emptyList()) else budgetRepo.observeByEvent(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setEventId(eventId: String) { _eventId.value = eventId }

    class Factory(
        private val eventRepo: EventRepository,
        private val guestRepo: GuestRepository,
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
        private val budgetRepo: BudgetRepository,
        private val ticketRepo: TicketRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            WrapReportViewModel(eventRepo, guestRepo, budgetRepo, ticketRepo) as T
    }
}

<<<<<<< HEAD
// ── InvitationViewModel ────────────────────────────────────────────────────────
=======
// ---------------------------------------------------------------------------
// InvitationViewModel — drives InvitationScreen
// ---------------------------------------------------------------------------
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
class InvitationViewModel(private val eventRepo: EventRepository) : ViewModel() {

    private val _eventId = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
<<<<<<< HEAD
    val event = _eventId.flatMapLatest { id -> flowOf(if (id.isBlank()) null else eventRepo.getById(id)) }
                        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setEventId(id: String) { _eventId.value = id }

    class Factory(private val repo: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = InvitationViewModel(repo) as T
=======
    val event = _eventId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(null) else flowOf(eventRepo.getById(id))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setEventId(eventId: String) { _eventId.value = eventId }

    class Factory(private val repo: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            InvitationViewModel(repo) as T
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
    }
}
