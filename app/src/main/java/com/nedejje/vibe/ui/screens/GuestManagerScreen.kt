package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class Guest(val name: String, val status: String, val tag: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestManagerScreen(navController: NavController) {
    val guests = listOf(
        Guest("John Doe", "Confirmed", "VIP"),
        Guest("Jane Smith", "Pending", "Vegan"),
        Guest("Alice Brown", "Declined", "+1"),
        Guest("Bob Wilson", "Confirmed", "Regular")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guest Manager") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(guests) { guest ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = guest.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = guest.tag, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            text = guest.status,
                            color = if (guest.status == "Confirmed") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
