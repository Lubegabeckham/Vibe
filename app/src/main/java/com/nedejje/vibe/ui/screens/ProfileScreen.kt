package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nedejje.vibe.session.SessionManager
import com.nedejje.vibe.ui.navigation.Screen

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

    val currentUser by SessionManager.currentUser.collectAsState()

    var displayName by remember(currentUser) { mutableStateOf(currentUser?.name  ?: "") }
    var email       by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }
    var phone       by remember(currentUser) { mutableStateOf(currentUser?.phone ?: "") }

    val initials = remember(displayName) {
        displayName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
    }

    val bookings = remember {
        mutableStateListOf(
            BookingRecord("1", "Nyege Nyege Festival",  "Sept 4, 2025",  "VIP",      BookingStatus.UPCOMING,  150_000L),
            BookingRecord("2", "Blankets and Wine",     "Oct 15, 2025",  "Ordinary", BookingStatus.UPCOMING,   50_000L),
            BookingRecord("3", "Rooftop Jazz Night",    "Aug 2, 2025",   "Ordinary", BookingStatus.ATTENDED,   30_000L),
            BookingRecord("4", "Comedy Nite Kampala",   "Jul 20, 2025",  "VIP",      BookingStatus.ATTENDED,   80_000L),
            BookingRecord("5", "Pearl of Africa Run",   "Jun 5, 2025",   "Ordinary", BookingStatus.CANCELLED,  0L)
        )
    }

    val upcomingCount = bookings.count { it.status == BookingStatus.UPCOMING }
    val attendedCount = bookings.count { it.status == BookingStatus.ATTENDED }
    val totalSpent    = bookings.filter { it.status != BookingStatus.CANCELLED }.sumOf { it.amountPaid }

    var activeFilter     by remember { mutableStateOf<BookingStatus?>(null) }
    val filteredBookings = if (activeFilter == null) bookings else bookings.filter { it.status == activeFilter }

    // Edit sheet
    var showEditSheet by remember { mutableStateOf(false) }
    var editName      by remember { mutableStateOf(displayName) }
    var editEmail     by remember { mutableStateOf(email) }
    var editPhone     by remember { mutableStateOf(phone) }
    var nameError     by remember { mutableStateOf(false) }

    if (showEditSheet) {
        ModalBottomSheet(onDismissRequest = { showEditSheet = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Edit Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = editName, onValueChange = { editName = it; nameError = false },
                    label = { Text("Full Name") }, isError = nameError,
                    supportingText = { if (nameError) Text("Name cannot be empty") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, null, Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editEmail, onValueChange = { editEmail = it },
                    label = { Text("Email") }, singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Email, null, Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editPhone, onValueChange = { editPhone = it },
                    label = { Text("Phone") }, singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Phone, null, Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (editName.isBlank()) { nameError = true; return@Button }
                        displayName = editName.trim()
                        email = editEmail.trim()
                        phone = editPhone.trim()
                        // Persist to session
                        SessionManager.updateProfile(displayName, email, phone)
                        showEditSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save Changes") }
            }
        }
    }

    // Logout dialog
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
                title = { Text("My Profile") },
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
            // Avatar hero
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Avatar circle
                        Surface(
                            modifier = Modifier.size(88.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(initials, style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (phone.isNotBlank()) {
                            Text(phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        // Admin badge
                        if (currentUser?.isAdmin == true) {
                            Spacer(Modifier.height(6.dp))
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
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
                        Spacer(Modifier.height(12.dp))
                        FilledTonalButton(onClick = { showEditSheet = true }) {
                            Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Edit Profile")
                        }
                    }
                }
            }

            // Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileStatCard("Upcoming", "$upcomingCount",  Icons.Default.CalendarToday, Modifier.weight(1f))
                    ProfileStatCard("Attended",  "$attendedCount", Icons.Default.CheckCircle,    Modifier.weight(1f))
                    ProfileStatCard(
                        "Spent",
                        if (totalSpent >= 1_000_000) "${"%.1f".format(totalSpent / 1_000_000.0)}M" else "${totalSpent / 1_000}K",
                        Icons.Default.AccountBalanceWallet, Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            // Bookings header + filter
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("My Bookings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${filteredBookings.size} ticket${if (filteredBookings.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                Spacer(Modifier.height(8.dp))
            }

            if (filteredBookings.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("No bookings found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(filteredBookings, key = { it.id }) { booking ->
                BookingHistoryItem(booking, Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
            }

            item {
                Spacer(Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Log Out")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileStatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BookingHistoryItem(booking: BookingRecord, modifier: Modifier = Modifier) {
    val (badgeContainer, badgeContent) = when (booking.status) {
        BookingStatus.UPCOMING  -> MaterialTheme.colorScheme.primaryContainer  to MaterialTheme.colorScheme.primary
        BookingStatus.ATTENDED  -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        BookingStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant    to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.eventName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(booking.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (booking.amountPaid > 0) {
                    Text("UGX ${booking.amountPaid / 1_000}K", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(shape = RoundedCornerShape(8.dp), color = badgeContainer) {
                    Text(booking.tier, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall, color = badgeContent, fontWeight = FontWeight.SemiBold)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface) {
                    Text(booking.status.label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall, color = badgeContent)
                }
            }
        }
    }
}
