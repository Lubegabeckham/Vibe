package com.nedejje.vibe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nedejje.vibe.data.DataManager
import com.nedejje.vibe.data.Event
import com.nedejje.vibe.ui.navigation.Screen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
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
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Manager Dashboard") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.EventEditor.createRoute("new")) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Event") }
            )
        }
    ) { padding ->
        LazyColumn(
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
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
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                }
            }
        }
    }
}