package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nedejje.vibe.R
import com.nedejje.vibe.VibeApplication
import com.nedejje.vibe.session.SessionManager
import com.nedejje.vibe.ui.navigation.Screen
import com.nedejje.vibe.viewmodel.TicketViewModel
import kotlinx.coroutines.launch
import java.util.Locale

enum class BookingStatus(val label: String) {
    UPCOMING("Upcoming"),
    ATTENDED("Attended"),
    CANCELLED("Cancelled")
}

data class BookingRecord(
    val id: String,
    val eventName: String,
    val date: String,
    val tier: String,
    val status: BookingStatus,
    val amountPaid: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val ticketViewModel: TicketViewModel = viewModel(
        factory = TicketViewModel.Factory(
            app.container.ticketRepository, 
            app.container.eventRepository,
            app.container.guestRepository
        )
    )
    val scope = rememberCoroutineScope()

    val currentUser by SessionManager.currentUser.collectAsState()
    
    // Load real tickets from DB
    LaunchedEffect(currentUser) {
        currentUser?.let { ticketViewModel.loadForUser(it.id) }
    }

    val realTickets by ticketViewModel.tickets.collectAsStateWithLifecycle()

    var displayName by remember(currentUser) { mutableStateOf(currentUser?.name  ?: "") }
    var email       by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }
    var phone       by remember(currentUser) { mutableStateOf(currentUser?.phone ?: "") }

    val initials = remember(displayName) {
        displayName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
    }

    // Map DB tickets to UI BookingRecords
    val displayBookings = realTickets.map { t ->
        BookingRecord(
            id = t.id,
            eventName = "Event Ticket", 
            date = "Purchased on ${java.text.SimpleDateFormat("MMM dd", Locale.getDefault()).format(java.util.Date(t.purchasedAt))}",
            tier = t.tier,
            status = if (t.isCancelled) BookingStatus.CANCELLED else if (t.isUsed) BookingStatus.ATTENDED else BookingStatus.UPCOMING,
            amountPaid = t.price * t.quantity
        )
    }

    val upcomingCount = displayBookings.count { it.status == BookingStatus.UPCOMING }
    val attendedCount = displayBookings.count { it.status == BookingStatus.ATTENDED }
    val totalSpent    = displayBookings.filter { it.status != BookingStatus.CANCELLED }.sumOf { it.amountPaid }

    var activeFilter     by remember { mutableStateOf<BookingStatus?>(null) }
    val filteredBookings = if (activeFilter == null) displayBookings else displayBookings.filter { it.status == activeFilter }

    // Edit sheet
    var showEditSheet by remember { mutableStateOf(false) }
    var editName      by remember { mutableStateOf(displayName) }
    var editEmail     by remember { mutableStateOf(email) }
    var editPhone     by remember { mutableStateOf(phone) }
    var nameError     by remember { mutableStateOf(false) }

    if (showEditSheet) {
        ModalBottomSheet(onDismissRequest = { showEditSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.padding_large))
                    .padding(bottom = dimensionResource(R.dimen.spacer_large)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                Text("Edit Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = editName, onValueChange = { editName = it; nameError = false },
                    label = { Text(stringResource(R.string.full_name_label)) }, isError = nameError,
                    supportingText = { if (nameError) Text("Name cannot be empty") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, null, Modifier.size(dimensionResource(R.dimen.icon_size_small) + 2.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
                )
                OutlinedTextField(
                    value = editEmail, onValueChange = { editEmail = it },
                    label = { Text(stringResource(R.string.email_label)) }, singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Email, null, Modifier.size(dimensionResource(R.dimen.icon_size_small) + 2.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
                )
                OutlinedTextField(
                    value = editPhone, onValueChange = { editPhone = it },
                    label = { Text("Phone") }, singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Phone, null, Modifier.size(dimensionResource(R.dimen.icon_size_small) + 2.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
                )
                Button(
                    onClick = {
                        if (editName.isBlank()) { nameError = true; return@Button }
                        displayName = editName.trim()
                        email = editEmail.trim()
                        phone = editPhone.trim()
                        SessionManager.updateProfile(displayName, email, phone)
                        showEditSheet = false
                    },
                    modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.button_height)),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
                ) { Text("Save Changes") }
            }
        }
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon  = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Log Out?") },
            text  = { Text("You will be signed out of your Vibe account.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        SessionManager.logout()
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Log Out") }
            },
            dismissButton = { OutlinedButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title), style = MaterialTheme.typography.titleLarge) },
                actions = {
                    if (currentUser?.isAdmin == true) {
                        IconButton(onClick = { navController.navigate(Screen.AdminHome.route) }) {
                            Icon(Icons.Default.AdminPanelSettings, "Admin", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                        .padding(vertical = dimensionResource(R.dimen.padding_large)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(dimensionResource(R.dimen.profile_image_size)),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = initials, 
                                    style = MaterialTheme.typography.displaySmall, 
                                    fontWeight = FontWeight.Bold, 
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(Modifier.height(dimensionResource(R.dimen.padding_medium)))
                        Text(displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (phone.isNotBlank()) {
                            Text(phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (currentUser?.isAdmin == true) {
                            Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
                            Surface(shape = RoundedCornerShape(dimensionResource(R.dimen.badge_corner_radius)), color = MaterialTheme.colorScheme.secondaryContainer) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                                    Text("Event Organiser", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.height(dimensionResource(R.dimen.padding_medium)))
                        FilledTonalButton(
                            onClick = { showEditSheet = true },
                            shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
                        ) {
                            Icon(Icons.Default.Edit, null, Modifier.size(dimensionResource(R.dimen.icon_size_small)))
                            Spacer(Modifier.width(dimensionResource(R.dimen.padding_small)))
                            Text("Edit Profile")
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.padding_medium)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    ProfileStatCard("Upcoming", "$upcomingCount",  Icons.Default.CalendarToday, Modifier.weight(1f))
                    ProfileStatCard("Attended",  "$attendedCount", Icons.Default.CheckCircle,    Modifier.weight(1f))
                    ProfileStatCard(
                        "Spent",
                        "${stringResource(R.string.ugx_currency)} ${String.format(Locale.getDefault(), "%,d", totalSpent)}",
                        Icons.Default.AccountBalanceWallet, Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_large)))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.padding_medium)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("My Bookings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    val countStr = if (filteredBookings.size == 1) 
                        stringResource(R.string.event_count_singular, filteredBookings.size)
                    else 
                        stringResource(R.string.events_count, filteredBookings.size)
                    
                    Text(
                        text = countStr,
                        style = MaterialTheme.typography.bodySmall, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.padding_medium)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    FilterChip(selected = activeFilter == null, onClick = { activeFilter = null }, label = { Text("All") })
                    BookingStatus.entries.forEach { status ->
                        FilterChip(
                            selected = activeFilter == status,
                            onClick  = { activeFilter = if (activeFilter == status) null else status },
                            label    = { Text(status.label) }
                        )
                    }
                }
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
            }

            if (filteredBookings.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.spacer_large)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, null, Modifier.size(dimensionResource(R.dimen.icon_size_extra_large)), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("No bookings found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(filteredBookings, key = { it.id }) { booking ->
                BookingHistoryItem(
                    booking = booking,
                    onClick = { navController.navigate(Screen.TicketView.createRoute(booking.id)) },
                    onCancel = {
                        scope.launch {
                            app.container.ticketRepository.cancelTicket(booking.id, true)
                            ticketViewModel.loadForUser(currentUser?.id ?: "")
                        }
                    },
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium))
                )
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
            }

            item {
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_large)))
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.padding_medium)).height(dimensionResource(R.dimen.button_height)),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, Modifier.size(dimensionResource(R.dimen.icon_size_small) + 2.dp))
                    Spacer(Modifier.width(dimensionResource(R.dimen.padding_small)))
                    Text(stringResource(R.string.logout_button), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(dimensionResource(R.dimen.spacer_large)))
            }
        }
    }
}

@Composable
private fun ProfileStatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier, 
        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_extra_small))
        ) {
            Icon(icon, null, Modifier.size(dimensionResource(R.dimen.icon_size_small) + 2.dp), tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BookingHistoryItem(
    booking: BookingRecord, 
    onClick: () -> Unit, 
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (badgeContainer, badgeContent) = when (booking.status) {
        BookingStatus.UPCOMING  -> MaterialTheme.colorScheme.primaryContainer  to MaterialTheme.colorScheme.primary
        BookingStatus.ATTENDED  -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        BookingStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer    to MaterialTheme.colorScheme.error
    }
    Card(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.eventName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(booking.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (booking.amountPaid > 0) {
                    Text("${stringResource(R.string.ugx_currency)} ${String.format(Locale.getDefault(), "%,d", booking.amountPaid)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_extra_small))) {
                Surface(shape = RoundedCornerShape(dimensionResource(R.dimen.badge_corner_radius)), color = badgeContainer) {
                    Text(booking.tier, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall, color = badgeContent, fontWeight = FontWeight.SemiBold)
                }
                
                if (booking.status == BookingStatus.UPCOMING) {
                    TextButton(onClick = { onCancel() }, contentPadding = PaddingValues(0.dp)) {
                        Text("Cancel", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Surface(shape = RoundedCornerShape(dimensionResource(R.dimen.badge_corner_radius)), color = MaterialTheme.colorScheme.surface) {
                        Text(booking.status.label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall, color = badgeContent)
                    }
                }
            }
        }
    }
}
