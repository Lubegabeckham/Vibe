package com.nedejje.vibe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.nedejje.vibe.db.GuestEntity
import com.nedejje.vibe.viewmodel.GuestManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestManagerScreen(
    navController: NavController,
    eventId: String?
) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: GuestManagerViewModel = viewModel(
        factory = GuestManagerViewModel.Factory(app.container.guestRepository)
    )

    LaunchedEffect(eventId) {
        eventId?.let { viewModel.setEventId(it) }
    }

    val guests by viewModel.guests.collectAsStateWithLifecycle()
    val guestCount by viewModel.guestCount.collectAsStateWithLifecycle()
    val checkedInCount by viewModel.checkedInCount.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Guest Manager", fontWeight = FontWeight.Bold)
                        Text("Verify and track attendance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Export CSV */ }) {
                        Icon(Icons.Default.FileDownload, "Export")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.PersonAdd, "Add Guest")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            // ── Stats Dashboard ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox("Registered", "$guestCount", Icons.Default.People, Modifier.weight(1f))
                StatBox("Checked In", "$checkedInCount", Icons.Default.CheckCircle, Modifier.weight(1f), 
                    color = MaterialTheme.colorScheme.tertiary)
            }

            // ── Progress bar ──────────────────────────────────────────────
            val progress = if (guestCount > 0) checkedInCount.toFloat() / guestCount else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(Modifier.height(16.dp))

            // ── Guest List ────────────────────────────────────────────────
            if (guests.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No guests registered yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(guests, key = { it.id }) { guest ->
                        EnhancedGuestCard(
                            guest = guest,
                            onCheckInToggle = {
                                if (guest.checkedIn) viewModel.checkOut(guest.id)
                                else viewModel.checkIn(guest.id)
                            },
                            onDelete = { viewModel.deleteGuest(guest) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGuestDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, email, phone, tag ->
                eventId?.let { viewModel.addGuest(it, name, email, phone, tag, "") }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun StatBox(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(24.dp), tint = color)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = color)
                Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun EnhancedGuestCard(
    guest: GuestEntity,
    onCheckInToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (guest.checkedIn) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(guest.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(guest.tag, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), 
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Text(guest.status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCheckInToggle) {
                    Icon(
                        imageVector = if (guest.checkedIn) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Check In",
                        tint = if (guest.checkedIn) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun AddGuestDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("Regular") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Manual Guest") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                // Simple tag picker
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Regular", "VIP", "Staff").forEach { t ->
                        FilterChip(selected = tag == t, onClick = { tag = t }, label = { Text(t) })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name, email, phone, tag) }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
