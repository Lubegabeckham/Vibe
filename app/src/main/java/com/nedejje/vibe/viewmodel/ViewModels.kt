package com.nedejje.vibe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nedejje.vibe.db.*
import com.nedejje.vibe.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

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
class AuthViewModel(private val userRepo: UserRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            delay(1000) // Simulate network delay
            
            // For now, we accept any non-empty password for users in our DB
            // In a real app, you'd check a hashed password.
            val user = userRepo.getByEmail(email)
            if (user != null) {
                _authState.value = AuthState.Success(user)
            } else {
                _authState.value = AuthState.Error("User not found. Please sign up.")
            }
        }
    }

    fun signup(name: String, email: String, phone: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            delay(1000)
            
            val existing = userRepo.getByEmail(email)
            if (existing != null) {
                _authState.value = AuthState.Error("Email already registered.")
            } else {
                val newUser = UserEntity(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    email = email,
                    phone = phone,
                    isAdmin = email.contains("admin") // Simple rule for demo
                )
                userRepo.insert(newUser)
                _authState.value = AuthState.Success(newUser)
            }
        }
    }

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
class HomeViewModel(private val eventRepo: EventRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val events: StateFlow<List<EventEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) eventRepo.allEvents
            else eventRepo.search(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    class Factory(private val repo: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
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
        private val ticketRepo: TicketRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            EventDetailViewModel(eventRepo, ticketRepo) as T
    }
}

// ---------------------------------------------------------------------------
// EventEditorViewModel — drives EventEditorScreen
// ---------------------------------------------------------------------------
class EventEditorViewModel(private val repo: EventRepository) : ViewModel() {

    private val _event = MutableStateFlow<EventEntity?>(null)
    val event: StateFlow<EventEntity?> = _event.asStateFlow()

    fun load(eventId: String) {
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
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            val entity = EventEntity(
                id = id ?: UUID.randomUUID().toString(),
                title = title.trim(),
                location = location.trim(),
                date = date.trim(),
                description = description.trim(),
                isFree = isFree,
                priceOrdinary = priceOrdinary,
                priceVIP = priceVIP,
                priceVVIP = priceVVIP
            )
            if (id == null) repo.add(entity) else repo.update(entity)
            onDone()
        }
    }

    fun delete(id: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.delete(id)
            onDone()
        }
    }

    class Factory(private val repo: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            EventEditorViewModel(repo) as T
    }
}

// ---------------------------------------------------------------------------
// GuestManagerViewModel — drives GuestManagerScreen
// ---------------------------------------------------------------------------
class GuestManagerViewModel(private val repo: GuestRepository) : ViewModel() {

    private val _eventId = MutableStateFlow("")
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
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
class BudgetViewModel(private val repo: BudgetRepository) : ViewModel() {

    private val _eventId = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<BudgetItemEntity>> = _eventId
        .flatMapLatest { id ->
            if (id.isBlank()) flowOf(emptyList())
            else repo.observeByEvent(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val totalSpend: StateFlow<Double> = _eventId
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
    private val budgetRepo: BudgetRepository,
    private val ticketRepo: TicketRepository
) : ViewModel() {

    private val _eventId = MutableStateFlow("")

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
        private val budgetRepo: BudgetRepository,
        private val ticketRepo: TicketRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            WrapReportViewModel(eventRepo, guestRepo, budgetRepo, ticketRepo) as T
    }
}

// ---------------------------------------------------------------------------
// InvitationViewModel — drives InvitationScreen
// ---------------------------------------------------------------------------
class InvitationViewModel(private val eventRepo: EventRepository) : ViewModel() {

    private val _eventId = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val event = _eventId.flatMapLatest { id ->
        if (id.isBlank()) flowOf(null) else flowOf(eventRepo.getById(id))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setEventId(eventId: String) { _eventId.value = eventId }

    class Factory(private val repo: EventRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            InvitationViewModel(repo) as T
    }
}
