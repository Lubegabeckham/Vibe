package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guest Manager") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Stats Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total", style = MaterialTheme.typography.labelMedium)
                        Text("$guestCount", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    VerticalDivider(modifier = Modifier.height(32.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Checked In", style = MaterialTheme.typography.labelMedium)
                        Text("$checkedInCount", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(guests, key = { it.id }) { guest ->
                    GuestCard(
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

@Composable
private fun GuestCard(
    guest: GuestEntity,
    onCheckInToggle: () -> Unit,
    onDelete: () -> Unit
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
                    text = guest.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = guest.tag,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = guest.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = when (guest.status) {
                            "Confirmed" -> MaterialTheme.colorScheme.primary
                            "Declined" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.tertiary
                        }
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCheckInToggle) {
                    Icon(
                        imageVector = if (guest.checkedIn) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Check In",
                        tint = if (guest.checkedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
