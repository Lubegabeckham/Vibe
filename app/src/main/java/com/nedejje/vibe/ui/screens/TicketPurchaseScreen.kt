package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nedejje.vibe.data.DataManager
import com.nedejje.vibe.ui.navigation.Screen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketPurchaseScreen(navController: NavController, eventId: String?) {
    val event = DataManager.getEventById(eventId ?: "")
    var selectedTier by remember { mutableStateOf("Ordinary") }
    var quantity by remember { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Tickets") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (event == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Event not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(text = event.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Select Ticket Tier", style = MaterialTheme.typography.titleMedium)
                if (event.isFree) {
                    RadioButtonItem("Free Entry", selectedTier == "Free") { selectedTier = "Free" }
                } else {
                    RadioButtonItem("Ordinary (UGX ${String.format(Locale.getDefault(), "%,d", event.priceOrdinary)})", selectedTier == "Ordinary") { selectedTier = "Ordinary" }
                    RadioButtonItem("VIP (UGX ${String.format(Locale.getDefault(), "%,d", event.priceVIP)})", selectedTier == "VIP") { selectedTier = "VIP" }
                    RadioButtonItem("VVIP (UGX ${String.format(Locale.getDefault(), "%,d", event.priceVVIP)})", selectedTier == "VVIP") { selectedTier = "VVIP" }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Quantity", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (quantity > 1) quantity-- }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text(text = quantity.toString(), style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = { quantity++ }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        // In a real app, save booking to database
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm Booking")
                }
            }
        }
    }
}

@Composable
fun RadioButtonItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}
