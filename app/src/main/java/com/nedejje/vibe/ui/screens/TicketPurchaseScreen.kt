package com.nedejje.vibe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nedejje.vibe.R
import com.nedejje.vibe.VibeApplication
import com.nedejje.vibe.db.EventEntity
import com.nedejje.vibe.session.SessionManager
import com.nedejje.vibe.ui.navigation.Screen
import com.nedejje.vibe.viewmodel.TicketViewModel
import java.util.Locale

// ── Models ──────────────────────────────────────────────────────────

data class TicketTier(
    val id: String,
    val label: String,
    val description: String,
    val priceUgx: Long,
    val isFree: Boolean = false
)

enum class PaymentMethod(val label: String) {
    MTN("MTN MoMo"),
    AIRTEL("Airtel Money"),
    CARD("Credit/Debit Card"),
    LATER("Pay Later (Pending)")
}

private fun EventEntity.tiers(): List<TicketTier> = if (isFree) {
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TicketPurchaseScreen(navController: NavController, eventId: String?) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: TicketViewModel = viewModel(
        factory = TicketViewModel.Factory(
            app.container.ticketRepository,
            app.container.eventRepository,
            app.container.guestRepository
        )
    )

    LaunchedEffect(eventId) {
        eventId?.let { viewModel.loadEvent(it) }
    }

    val event by viewModel.event.collectAsStateWithLifecycle()

    // ── State ──────────────────────────────────────────────────────────────
    val tiers = remember(event) { event?.tiers() ?: emptyList() }
    var selectedTierId by remember(tiers) { mutableStateOf(tiers.firstOrNull()?.id ?: "") }
    var quantity by remember { mutableIntStateOf(1) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var bookingConfirmed by remember { mutableStateOf(false) }

    // Payment State
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.MTN) }
    var phoneNumber by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val selectedTier = tiers.find { it.id == selectedTierId }
    val unitPrice = selectedTier?.priceUgx ?: 0L
    val totalPrice = unitPrice * quantity
    val maxQuantity = 10

    // ── Payment Bottom Sheet ─────────────────────────────────────────────
    if (showPaymentSheet && event != null && selectedTier != null) {
        ModalBottomSheet(onDismissRequest = { if (!isProcessing) showPaymentSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Select Payment Method", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                // Methods - Changed from FlowRow to Row with horizontalScroll to fix NoSuchMethodError crash
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethod.entries.forEach { method ->
                        PaymentMethodChip(method, selectedPaymentMethod == method) { selectedPaymentMethod = it }
                    }
                }

                when (selectedPaymentMethod) {
                    PaymentMethod.CARD -> {
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it },
                            label = { Text("Card Number") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = { Icon(Icons.Default.CreditCard, null) }
                        )
                    }
                    PaymentMethod.LATER -> {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Your ticket will be marked as PENDING until payment is verified at the entrance.",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    else -> {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Icon(Icons.Default.PhoneAndroid, null) },
                            placeholder = { Text("07...") }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        isProcessing = true
                        val userId = SessionManager.userId
                        if (userId.isBlank()) {
                            isProcessing = false
                            return@Button
                        }
                        val status = if (selectedPaymentMethod == PaymentMethod.LATER) "PENDING" else "PAID"
                        viewModel.purchase(
                            eventId = event!!.id,
                            userId = userId,
                            tier = selectedTier.label,
                            price = unitPrice,
                            quantity = quantity,
                            status = status,
                            onDone = {
                                isProcessing = false
                                showPaymentSheet = false
                                bookingConfirmed = true
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isProcessing && (phoneNumber.isNotBlank() || cardNumber.isNotBlank() || selectedPaymentMethod == PaymentMethod.LATER)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        val btnText = if (selectedPaymentMethod == PaymentMethod.LATER) "Confirm Reservation" else "Pay UGX ${formatPrice(totalPrice)}"
                        Text(btnText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // ── Booking success sheet ──────────────────────────────────────────────
    if (bookingConfirmed) {
        ModalBottomSheet(
            onDismissRequest = {
                bookingConfirmed = false
                navController.navigate(Screen.Profile.route) { popUpTo(Screen.Home.route) }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val isPending = selectedPaymentMethod == PaymentMethod.LATER
                Icon(
                    imageVector = if (isPending) Icons.Default.Schedule else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = if (isPending) MaterialTheme.colorScheme.secondary else Color(0xFF4CAF50)
                )
                Text(
                    text = if (isPending) "Reservation Received" else "Payment Successful!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isPending)
                        "Your ticket for ${event?.title} is reserved. Please complete payment at the venue to activate your ticket."
                    else "Your ticket for ${event?.title} is ready. You'll need to show your QR code at the entrance.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        bookingConfirmed = false
                        navController.navigate(Screen.Profile.route) { popUpTo(Screen.Home.route) }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("View My Ticket") }
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->

        if (event == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Event info
                Column {
                    Text(event!!.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text(event!!.date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(event!!.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HorizontalDivider()

                // Tier selection
                Text("Choose your ticket", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                tiers.forEach { tier ->
                    TierCard(tier, selectedTierId == tier.id) { selectedTierId = tier.id }
                }

                if (selectedTier?.isFree == false) {
                    HorizontalDivider()
                    Text("Number of tickets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        FilledIconButton(onClick = { if (quantity > 1) quantity-- }, enabled = quantity > 1) {
                            Icon(Icons.Default.Remove, null)
                        }
                        Text(quantity.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        FilledIconButton(onClick = { if (quantity < maxQuantity) quantity++ }, enabled = quantity < maxQuantity) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                }
            }

            // Footer
            Surface(shadowElevation = 8.dp) {
                Column(Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Column {
                            Text("Total Amount", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = if (selectedTier?.isFree == true) "FREE" else "UGX ${formatPrice(totalPrice)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = {
                                val userId = SessionManager.userId
                                if (userId.isBlank()) return@Button   // not logged in — shouldn't happen
                                if (selectedTier?.isFree == true) {
                                    viewModel.purchase(event!!.id, userId, selectedTier.label, 0, quantity) {
                                        bookingConfirmed = true
                                    }
                                } else {
                                    showPaymentSheet = true
                                }
                            },
                            modifier = Modifier.height(56.dp).width(160.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (selectedTier?.isFree == true) "Register" else "Book Now")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodChip(method: PaymentMethod, selected: Boolean, onSelect: (PaymentMethod) -> Unit) {
    Surface(
        onClick = { onSelect(method) },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier.height(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(method.label, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
private fun TierCard(tier: TicketTier, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        border = BorderStroke(2.dp, if (selected) MaterialTheme.colorScheme.primary else Color.Transparent),
        colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onClick)
            Column(Modifier.weight(1f)) {
                Text(tier.label, fontWeight = FontWeight.Bold)
                Text(tier.description, style = MaterialTheme.typography.bodySmall)
            }
            Text(if (tier.isFree) "FREE" else "UGX ${formatPrice(tier.priceUgx)}", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        }
    }
}
