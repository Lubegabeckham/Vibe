package com.nedejje.vibe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nedejje.vibe.data.DataManager
import com.nedejje.vibe.data.Event
import com.nedejje.vibe.ui.navigation.Screen
import java.util.Locale

// ── Ticket tier model ──────────────────────────────────────────────────────────

data class TicketTier(
    val id: String,
    val label: String,
    val description: String,
    val priceUgx: Long,
    val isFree: Boolean = false
)

private fun Event.tiers(): List<TicketTier> = if (isFree) {
    listOf(TicketTier("free", "Free Entry", "General admission, no charge", 0L, isFree = true))
} else {
    buildList {
        if (priceOrdinary > 0) add(TicketTier("ordinary", "Ordinary", "Standard access to all event areas", priceOrdinary))
        if (priceVIP     > 0) add(TicketTier("vip",      "VIP",      "Priority seating & exclusive lounge access", priceVIP))
        if (priceVVIP    > 0) add(TicketTier("vvip",     "VVIP",     "Front-row seats, meet & greet, gift bag", priceVVIP))
    }.ifEmpty {
        listOf(TicketTier("ordinary", "Ordinary", "Standard access to all event areas", priceOrdinary))
    }
}

private fun formatPrice(amount: Long) = String.format(Locale.getDefault(), "%,d", amount)

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketPurchaseScreen(navController: NavController, eventId: String?) {
    val event = remember(eventId) { DataManager.getEventById(eventId ?: "") }

    // ── State ──────────────────────────────────────────────────────────────
    val tiers = remember(event) { event?.tiers() ?: emptyList() }
    var selectedTierId by remember(tiers) { mutableStateOf(tiers.firstOrNull()?.id ?: "") }
    var quantity by remember { mutableIntStateOf(1) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var bookingConfirmed by remember { mutableStateOf(false) }

    val selectedTier = tiers.find { it.id == selectedTierId }
    val unitPrice = selectedTier?.priceUgx ?: 0L
    val totalPrice = unitPrice * quantity
    val maxQuantity = 10

    // ── Confirm dialog ─────────────────────────────────────────────────────
    if (showConfirmDialog && event != null && selectedTier != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Confirm Booking") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(event.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    ConfirmRow("Tier",     selectedTier.label)
                    ConfirmRow("Quantity", "$quantity ticket${if (quantity > 1) "s" else ""}")
                    ConfirmRow(
                        "Total",
                        if (selectedTier.isFree) "Free" else "UGX ${formatPrice(totalPrice)}"
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    bookingConfirmed = true
                    showConfirmDialog = false
                }) { Text("Pay & Book") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ── Booking success sheet ──────────────────────────────────────────────
    if (bookingConfirmed) {
        ModalBottomSheet(
            onDismissRequest = {
                bookingConfirmed = false
                navController.navigate(Screen.Profile.route) {
                    popUpTo(Screen.Home.route)
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🎟", style = MaterialTheme.typography.displayMedium)
                Text(
                    "Booking Confirmed!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Your $quantity ${selectedTier?.label ?: ""} ticket${if (quantity > 1) "s" else ""} for\n${event?.title ?: ""} ${if ((selectedTier?.isFree == true)) "are reserved" else "have been paid for"}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (selectedTier?.isFree == false) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Amount Paid", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "UGX ${formatPrice(totalPrice)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = {
                        bookingConfirmed = false
                        navController.navigate(Screen.Profile.route) { popUpTo(Screen.Home.route) }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("View My Tickets") }
                OutlinedButton(
                    onClick = {
                        bookingConfirmed = false
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Back to Event") }
            }
        }
    }

    // ── Scaffold ───────────────────────────────────────────────────────────
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

        // ── Event not found ────────────────────────────────────────────────
        if (event == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.EventBusy, contentDescription = null,
                        modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Event not found", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = { navController.popBackStack() }) { Text("Go Back") }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Event header ───────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(event.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (event.date.isNotBlank()) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null,
                                modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(event.date, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (event.location.isNotBlank()) {
                            Text("·", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Icon(Icons.Default.LocationOn, contentDescription = null,
                                modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(event.location, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                HorizontalDivider()

                // ── Tier selection ─────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Ticket Tier", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    tiers.forEach { tier ->
                        TierCard(
                            tier = tier,
                            selected = selectedTierId == tier.id,
                            onClick = { selectedTierId = tier.id }
                        )
                    }
                }

                // ── Quantity selector ──────────────────────────────────────
                AnimatedVisibility(visible = selectedTier?.isFree == false) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HorizontalDivider()
                        Text("Quantity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FilledIconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                enabled = quantity > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = quantity.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("ticket${if (quantity > 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            FilledIconButton(
                                onClick = { if (quantity < maxQuantity) quantity++ },
                                enabled = quantity < maxQuantity
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                            Spacer(Modifier.weight(1f))
                            if (quantity == maxQuantity) {
                                Text(
                                    "Max $maxQuantity per booking",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // ── Sticky order summary + CTA ─────────────────────────────────
            Surface(
                shadowElevation = 8.dp,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Order summary
                    if (selectedTier != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "${selectedTier.label} × $quantity",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    if (selectedTier.isFree) "Free" else "UGX ${formatPrice(totalPrice)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTier.isFree)
                                        MaterialTheme.colorScheme.tertiary
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                            }
                            if (!selectedTier.isFree && quantity > 1) {
                                Text(
                                    "UGX ${formatPrice(unitPrice)} each",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (selectedTier?.isFree == true) "Reserve Free Ticket" else "Confirm Booking",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

// ── Tier card ──────────────────────────────────────────────────────────────────

@Composable
private fun TierCard(tier: TicketTier, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        border = if (selected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Column(modifier = Modifier.weight(1f)) {
                Text(tier.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(tier.description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = if (tier.isFree) "FREE" else "UGX ${formatPrice(tier.priceUgx)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (tier.isFree) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ── Confirm row ────────────────────────────────────────────────────────────────

@Composable
private fun ConfirmRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}