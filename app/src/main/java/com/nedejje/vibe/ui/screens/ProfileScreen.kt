package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nedejje.vibe.data.DataManager
import com.nedejje.vibe.ui.navigation.Screen

// ── Booking model ──────────────────────────────────────────────────────────────

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

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {

    // ── User state (would come from AuthManager / UserRepository in production) ──
    var displayName  by remember { mutableStateOf("Lubega Beckham Jusper") }
    var email        by remember { mutableStateOf("beckham@nedejje.vibe") }
    var phone        by remember { mutableStateOf("+256 700 123 456") }

    // Initials derived from display name
    val initials = remember(displayName) {
        displayName.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .take(2)
            .joinToString("")
    }

    // ── Bookings (would be fetched per-user in production) ────────────────
    val bookings = remember {
        mutableStateListOf(
            BookingRecord("1", "Nyege Nyege Festival",     "Sept 4, 2025",  "VIP",      BookingStatus.UPCOMING,   150_000L),
            BookingRecord("2", "Blankets and Wine",        "Oct 15, 2025",  "Ordinary", BookingStatus.UPCOMING,    50_000L),
            BookingRecord("3", "Rooftop Jazz Night",       "Aug 2, 2025",   "Ordinary", BookingStatus.ATTENDED,    30_000L),
            BookingRecord("4", "Comedy Nite Kampala",      "Jul 20, 2025",  "VIP",      BookingStatus.ATTENDED,    80_000L),
            BookingRecord("5", "Pearl of Africa Marathon", "Jun 5, 2025",   "Ordinary", BookingStatus.CANCELLED,   0L)
        )
    }

    // Stats
    val upcomingCount  = bookings.count { it.status == BookingStatus.UPCOMING }
    val attendedCount  = bookings.count { it.status == BookingStatus.ATTENDED }
    val totalSpent     = bookings.filter { it.status != BookingStatus.CANCELLED }.sumOf { it.amountPaid }

    // Active filter tab
    var activeFilter by remember { mutableStateOf<BookingStatus?>(null) }
    val filteredBookings = if (activeFilter == null) bookings else bookings.filter { it.status == activeFilter }

    // ── Edit profile sheet ─────────────────────────────────────────────────
    var showEditSheet by remember { mutableStateOf(false) }
    var editName  by remember { mutableStateOf(displayName) }
    var editEmail by remember { mutableStateOf(email) }
    var editPhone by remember { mutableStateOf(phone) }
    var nameError by remember { mutableStateOf(false) }

    if (showEditSheet) {
        ModalBottomSheet(onDismissRequest = { showEditSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Edit Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it; nameError = false },
                    label = { Text("Full Name") },
                    isError = nameError,
                    supportingText = { if (nameError) Text("Name cannot be empty") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editEmail,
                    onValueChange = { editEmail = it },
                    label = { Text("Email") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editPhone,
                    onValueChange = { editPhone = it },
                    label = { Text("Phone") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (editName.isBlank()) { nameError = true; return@Button }
                        displayName = editName.trim()
                        email = editEmail.trim()
                        phone = editPhone.trim()
                        showEditSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save Changes") }
            }
        }
    }

    // ── Logout confirmation ────────────────────────────────────────────────
    var showLogoutDialog by remember { mutableStateOf(false) }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Log Out?") },
            text = { Text("You will be signed out of your account.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Log Out") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ── Scaffold ───────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Team.route) }) {
                        Icon(Icons.Default.Groups, contentDescription = "Team")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Avatar + name ──────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    modifier = Modifier.size(88.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(initials, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (phone.isNotBlank()) {
                    Text(phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(onClick = { showEditSheet = true }) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Edit Profile")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Stats row ──────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileStatCard("Upcoming", "$upcomingCount",  Icons.Default.CalendarToday, Modifier.weight(1f))
                    ProfileStatCard("Attended",  "$attendedCount", Icons.Default.CheckCircle,    Modifier.weight(1f))
                    ProfileStatCard(
                        "Spent",
                        if (totalSpent >= 1_000_000) "${"%.1f".format(totalSpent / 1_000_000.0)}M"
                        else "${totalSpent / 1_000}K",
                        Icons.Default.AccountBalanceWallet,
                        Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Bookings header + filter chips ─────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("My Bookings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${filteredBookings.size} ticket${if (filteredBookings.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeFilter == null,
                        onClick = { activeFilter = null },
                        label = { Text("All") }
                    )
                    BookingStatus.entries.forEach { status ->
                        FilterChip(
                            selected = activeFilter == status,
                            onClick = { activeFilter = if (activeFilter == status) null else status },
                            label = { Text(status.label) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Empty state ────────────────────────────────────────────────
            if (filteredBookings.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null,
                            modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("No ${activeFilter?.label?.lowercase() ?: ""} bookings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ── Booking list ───────────────────────────────────────────────
            items(filteredBookings, key = { it.id }) { booking ->
                BookingHistoryItem(
                    booking = booking,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Logout ─────────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ── Stat card ──────────────────────────────────────────────────────────────────

@Composable
private fun ProfileStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Booking history item ───────────────────────────────────────────────────────

@Composable
fun BookingHistoryItem(booking: BookingRecord, modifier: Modifier = Modifier) {
    val (badgeContainer, badgeContent) = when (booking.status) {
        BookingStatus.UPCOMING   -> MaterialTheme.colorScheme.primaryContainer  to MaterialTheme.colorScheme.primary
        BookingStatus.ATTENDED   -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        BookingStatus.CANCELLED  -> MaterialTheme.colorScheme.surfaceVariant    to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.eventName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(booking.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (booking.amountPaid > 0) {
                    Text(
                        "UGX ${"${booking.amountPaid / 1_000}K"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = badgeContainer
                ) {
                    Text(
                        booking.tier,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeContent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        booking.status.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeContent
                    )
                }
            }
        }
    }
}