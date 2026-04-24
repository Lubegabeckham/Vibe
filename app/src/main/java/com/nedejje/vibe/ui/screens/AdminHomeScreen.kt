package com.nedejje.vibe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
<<<<<<< HEAD
import androidx.compose.foundation.background
=======
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
<<<<<<< HEAD
import androidx.compose.foundation.shape.RoundedCornerShape
=======
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
<<<<<<< HEAD
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nedejje.vibe.VibeApplication
import com.nedejje.vibe.db.EventEntity
import com.nedejje.vibe.session.SessionManager
import com.nedejje.vibe.ui.navigation.Screen
import com.nedejje.vibe.viewmodel.HomeViewModel
=======
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nedejje.vibe.data.DataManager
import com.nedejje.vibe.data.Event
import com.nedejje.vibe.ui.navigation.Screen
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
<<<<<<< HEAD
fun AdminHomeScreen(
    navController: NavController,
    onThemeToggle: () -> Unit = {},
    isDarkMode: Boolean = true
) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(app.container.eventRepository))

    val events by viewModel.events.collectAsStateWithLifecycle()
    val currentUser by SessionManager.currentUser.collectAsState()
    var eventPendingDelete by remember { mutableStateOf<EventEntity?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    val totalEvents = events.size
    val freeEvents  = events.count { it.isFree }
    val paidEvents  = totalEvents - freeEvents

    // Delete dialog
    eventPendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { eventPendingDelete = null },
            icon  = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Event?") },
            text  = { Text("\"${target.title}\" will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        // In a real screen, call EventEditorViewModel.delete(target.id)
                        eventPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { eventPendingDelete = null }) { Text("Cancel") }
=======
fun AdminHomeScreen(navController: NavController) {
    // Collect events as state so the list recomposes on changes
    val events by remember { derivedStateOf { DataManager.events.toList() } }

    // Track which event is pending deletion for the confirmation dialog
    var eventPendingDelete by remember { mutableStateOf<Event?>(null) }

    // Derived stats
    val totalEvents = events.size
    val freeEvents = events.count { it.isFree }
    val paidEvents = totalEvents - freeEvents

    // --- Delete confirmation dialog ---
    eventPendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { eventPendingDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Event?") },
            text = { Text("\"${target.title}\" will be permanently removed. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        DataManager.removeEvent(target)
                        eventPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { eventPendingDelete = null }) {
                    Text("Cancel")
                }
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
<<<<<<< HEAD
                title = {
                    Column {
                        Text(
                            "Admin Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            currentUser?.email ?: "organiser",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(if (isDarkMode) Icons.Default.WbSunny else Icons.Default.Brightness2, null)
                    }
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Switch to User View") },
                                leadingIcon = { Icon(Icons.Default.People, null) },
                                onClick = { showMenu = false; navController.navigate(Screen.Home.route) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Log Out", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    SessionManager.logout()
                                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
=======
                title = { Text("Event Manager Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.EventEditor.createRoute("new")) },
<<<<<<< HEAD
                icon    = { Icon(Icons.Default.Add, null) },
                text    = { Text("New Event") },
                containerColor = MaterialTheme.colorScheme.primary
=======
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Event") }
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
            )
        }
    ) { padding ->
        LazyColumn(
<<<<<<< HEAD
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Welcome banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text("Welcome back,", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Text(
                            currentUser?.name?.split(" ")?.firstOrNull() ?: "Admin",
                            color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Manage your events and guests", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // Stats row
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminStatCard("Total Events", "$totalEvents", Icons.Default.EventNote, Modifier.weight(1f))
                    AdminStatCard("Free",         "$freeEvents",  Icons.Default.LocalOffer, Modifier.weight(1f))
                    AdminStatCard("Paid",         "$paidEvents",  Icons.Default.ConfirmationNumber, Modifier.weight(1f))
                }
            }

            // Organiser tools header
            item {
                Text("Organiser Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            // Tool grid – navigate to first event if any, or "new" if none
            item {
                val firstEventId = events.firstOrNull()?.id ?: "new"
=======
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Stats row ──────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatChip(label = "Total", value = "$totalEvents", modifier = Modifier.weight(1f))
                    StatChip(label = "Free", value = "$freeEvents", modifier = Modifier.weight(1f))
                    StatChip(label = "Paid", value = "$paidEvents", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Organizer tools ────────────────────────────────────────────
            item {
                Text(
                    text = "Organizer Tools",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
<<<<<<< HEAD
                    AdminToolIcon("Guests",  Icons.Default.Groups)                  { navController.navigate(Screen.GuestManager.createRoute(firstEventId)) }
                    AdminToolIcon("Budget",  Icons.Default.AccountBalanceWallet)    { navController.navigate(Screen.BudgetTracker.createRoute(firstEventId)) }
                    AdminToolIcon("Potluck", Icons.Default.Restaurant)              { navController.navigate(Screen.Contribution.createRoute(firstEventId)) }
                    AdminToolIcon("Reports", Icons.Default.BarChart)                { navController.navigate(Screen.WrapReport.createRoute(firstEventId)) }
                }
            }

            // Section header
=======
                    ToolIcon("Guests", Icons.Default.Groups) {
                        navController.navigate(Screen.GuestManager.route)
                    }
                    ToolIcon("Budget", Icons.Default.AccountBalanceWallet) {
                        navController.navigate(Screen.BudgetTracker.route)
                    }
                    ToolIcon("Potluck", Icons.Default.Restaurant) {
                        navController.navigate(Screen.Contribution.route)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Section header ─────────────────────────────────────────────
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
<<<<<<< HEAD
                    Text("Your Events", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${events.size} event${if (events.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (events.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.EventNote, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("No events yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FilledTonalButton(onClick = { navController.navigate(Screen.EventEditor.createRoute("new")) }) {
                            Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Create First Event")
                        }
                    }
                }
            }

            items(events, key = { it.id }) { event ->
                AdminEventCard(
                    event          = event,
                    onEdit         = { navController.navigate(Screen.EventEditor.createRoute(event.id)) },
                    onDeleteRequest = { eventPendingDelete = event },
                    onManage       = { navController.navigate(Screen.GuestManager.createRoute(event.id)) }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun AdminStatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminToolIcon(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, label, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AdminEventCard(
    event: EventEntity,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
    onManage: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(event.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(event.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (event.isFree) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        if (event.isFree) "FREE" else "UGX ${String.format(Locale.getDefault(), "%,d", event.priceOrdinary)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (event.isFree) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onManage, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Groups, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Manage", fontSize = 12.sp)
                }
                OutlinedButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit", Modifier.size(16.dp))
                }
                OutlinedButton(
                    onClick = onDeleteRequest,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, "Delete", Modifier.size(16.dp))
=======
                    Text(
                        text = "Your Events",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (events.isNotEmpty()) {
                        Text(
                            text = "${events.size} event${if (events.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Empty state ────────────────────────────────────────────────
            if (events.isEmpty()) {
                item {
                    EmptyEventsPlaceholder(
                        onCreateClick = {
                            navController.navigate(Screen.EventEditor.createRoute("new"))
                        }
                    )
                }
            }

            // ── Event list ─────────────────────────────────────────────────
            items(events, key = { it.id }) { event ->
                AdminEventCard(
                    event = event,
                    onEdit = { navController.navigate(Screen.EventEditor.createRoute(event.id)) },
                    onDeleteRequest = { eventPendingDelete = event }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) } // FAB clearance
        }
    }
}

// ── Stat chip ──────────────────────────────────────────────────────────────────

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Empty state placeholder ────────────────────────────────────────────────────

@Composable
private fun EmptyEventsPlaceholder(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.EventNote,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "No events yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Tap the button below to create your first event.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilledTonalButton(onClick = onCreateClick) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create Event")
        }
    }
}

// ── Tool icon ──────────────────────────────────────────────────────────────────

@Composable
fun ToolIcon(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// ── Admin event card ───────────────────────────────────────────────────────────

@Composable
fun AdminEventCard(
    event: Event,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit   // renamed: triggers dialog, not direct delete
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = event.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (event.isFree) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Free", style = MaterialTheme.typography.labelSmall) },
                        icon = { Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                } else {
                    Text(
                        text = "UGX ${String.format(Locale.getDefault(), "%,d", event.priceOrdinary)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDeleteRequest) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
                }
            }
        }
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
