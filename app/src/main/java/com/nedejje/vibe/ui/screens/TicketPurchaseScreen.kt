package com.nedejje.vibe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

// ── Ticket tier model ──────────────────────────────────────────────────────────

data class TicketTier(
    val id: String,
    val label: String,
    val description: String,
    val priceUgx: Long,
    val isFree: Boolean = false
)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketPurchaseScreen(navController: NavController, eventId: String?) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: TicketViewModel = viewModel(
        factory = TicketViewModel.Factory(app.container.ticketRepository, app.container.eventRepository)
    )

    LaunchedEffect(eventId) {
        eventId?.let { viewModel.loadEvent(it) }
    }

    val event by viewModel.event.collectAsStateWithLifecycle()

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
            title = { Text(stringResource(R.string.confirm_booking_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
                    Text(event!!.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    ConfirmRow(stringResource(R.string.tier_label), selectedTier.label)
                    ConfirmRow(
                        stringResource(R.string.quantity_label), 
                        "$quantity ${if (quantity > 1) stringResource(R.string.tickets_label) else stringResource(R.string.ticket_label)}"
                    )
                    ConfirmRow(
                        stringResource(R.string.total_label),
                        if (selectedTier.isFree) stringResource(R.string.free_badge) else "${stringResource(R.string.ugx_currency)} ${formatPrice(totalPrice)}"
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.purchase(
                        eventId = event!!.id,
                        userId = SessionManager.userId,
                        tier = selectedTier.label,
                        price = unitPrice,
                        quantity = quantity,
                        onDone = {
                            bookingConfirmed = true
                            showConfirmDialog = false
                        }
                    )
                }) { Text(stringResource(R.string.pay_book_button)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) { Text(stringResource(R.string.back)) }
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
                    .padding(horizontal = dimensionResource(R.dimen.padding_large))
                    .padding(bottom = dimensionResource(R.dimen.spacer_extra_large)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                Text("🎟", style = MaterialTheme.typography.displayMedium)
                Text(
                    text = stringResource(R.string.booking_confirmed_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                val msg = if (selectedTier?.isFree == true) {
                    stringResource(
                        R.string.booking_success_msg_free, 
                        quantity, 
                        selectedTier.label, 
                        if (quantity > 1) stringResource(R.string.tickets_label) else stringResource(R.string.ticket_label),
                        event?.title ?: ""
                    )
                } else {
                    stringResource(
                        R.string.booking_success_msg_paid, 
                        quantity, 
                        selectedTier?.label ?: "", 
                        if (quantity > 1) stringResource(R.string.tickets_label) else stringResource(R.string.ticket_label),
                        event?.title ?: ""
                    )
                }
                
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (selectedTier?.isFree == false) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
                    ) {
                        Row(
                            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(stringResource(R.string.amount_paid_label), style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "${stringResource(R.string.ugx_currency)} ${formatPrice(totalPrice)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_extra_small)))
                Button(
                    onClick = {
                        bookingConfirmed = false
                        navController.navigate(Screen.Profile.route) { popUpTo(Screen.Home.route) }
                    },
                    modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.button_height)),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
                ) { Text(stringResource(R.string.view_my_tickets_button)) }
                OutlinedButton(
                    onClick = {
                        bookingConfirmed = false
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.button_height)),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
                ) { Text(stringResource(R.string.back_to_event_button)) }
            }
        }
    }

    // ── Scaffold ───────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.book_tickets_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, 
                    verticalArrangement = Arrangement.Center // Part B: Centered Layout
                ) {
                    Icon(Icons.Default.EventBusy, contentDescription = null,
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_extra_large)), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(dimensionResource(R.dimen.padding_medium)))
                    Text(stringResource(R.string.event_not_found), style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(dimensionResource(R.dimen.padding_medium)))
                    OutlinedButton(onClick = { navController.popBackStack() }) { Text(stringResource(R.string.go_back_button)) }
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
                    .padding(dimensionResource(R.dimen.padding_medium)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {

                // ── Event header ───────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_extra_small))) {
                    Text(event!!.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (event!!.date.isNotBlank()) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null,
                                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_extra_small)), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(event!!.date, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (event!!.location.isNotBlank()) {
                            Text("·", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Icon(Icons.Default.LocationOn, contentDescription = null,
                                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_extra_small)), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(event!!.location, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                HorizontalDivider()

                // ── Tier selection ─────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
                    Text(stringResource(R.string.select_tier_header), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
                        HorizontalDivider()
                        Text(stringResource(R.string.quantity_label), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
                        ) {
                            FilledIconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                enabled = quantity > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.decrease_quantity))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = quantity.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (quantity > 1) stringResource(R.string.tickets_label) else stringResource(R.string.ticket_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            FilledIconButton(
                                onClick = { if (quantity < maxQuantity) quantity++ },
                                enabled = quantity < maxQuantity
                            ) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.increase_quantity))
                            }
                            Spacer(Modifier.weight(1f))
                            if (quantity == maxQuantity) {
                                Text(
                                    stringResource(R.string.max_booking_limit, maxQuantity),
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
                shadowElevation = dimensionResource(R.dimen.card_elevation),
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_medium)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small) + 4.dp)
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
                                    if (selectedTier.isFree) stringResource(R.string.free_badge) else "${stringResource(R.string.ugx_currency)} ${formatPrice(totalPrice)}",
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
                                    "${stringResource(R.string.ugx_currency)} ${formatPrice(unitPrice)} each",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.button_height)),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                        contentPadding = PaddingValues(vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small) + 2.dp))
                        Spacer(Modifier.width(dimensionResource(R.dimen.padding_small)))
                        Text(
                            if (selectedTier?.isFree == true) stringResource(R.string.reserve_free_button) else stringResource(R.string.confirm_booking_button),
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
        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
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
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small) + 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Column(modifier = Modifier.weight(1f)) {
                Text(tier.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(tier.description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = if (tier.isFree) stringResource(R.string.free_badge) else "${stringResource(R.string.ugx_currency)} ${formatPrice(tier.priceUgx)}",
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
