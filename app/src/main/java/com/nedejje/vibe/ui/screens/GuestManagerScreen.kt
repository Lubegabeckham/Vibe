package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// Ideally move to com.nedejje.vibe.data.Guest.kt alongside Event.kt
data class Guest(
    val id: String,
    val name: String,
    val status: GuestStatus,
    val tag: String
)

enum class GuestStatus { Confirmed, Pending, Declined }

// Stable reference outside composable — avoids recomposition churn
private val sampleGuests = listOf(
    Guest("1", "John Doe",    GuestStatus.Confirmed, "VIP"),
    Guest("2", "Jane Smith",  GuestStatus.Pending,   "Vegan"),
    Guest("3", "Alice Brown", GuestStatus.Declined,  "+1"),
    Guest("4", "Bob Wilson",  GuestStatus.Confirmed, "Regular")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestManagerScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guest Manager") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // AutoMirrored: RTL-safe, consistent with other screens
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(sampleGuests, key = { it.id }) { guest ->
                GuestCard(guest)
            }
        }
    }
}

@Composable
private fun GuestCard(guest: Guest) {
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
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = guest.tag,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(guest.status)
        }
    }
}

@Composable
private fun StatusBadge(status: GuestStatus) {
    val (label, color) = when (status) {
        GuestStatus.Confirmed -> "Confirmed" to MaterialTheme.colorScheme.primary
        GuestStatus.Pending   -> "Pending"   to MaterialTheme.colorScheme.tertiary
        GuestStatus.Declined  -> "Declined"  to MaterialTheme.colorScheme.error
    }
    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = label,
                color = color,
                style = MaterialTheme.typography.labelMedium
            )
        }
    )
}