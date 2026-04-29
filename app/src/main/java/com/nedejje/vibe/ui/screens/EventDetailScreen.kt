package com.nedejje.vibe.ui.screens

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nedejje.vibe.VibeApplication
import com.nedejje.vibe.db.EventEntity
import com.nedejje.vibe.ui.navigation.Screen
import com.nedejje.vibe.viewmodel.EventDetailViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(navController: NavController, eventId: String?) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: EventDetailViewModel = viewModel(
        factory = EventDetailViewModel.Factory(
            app.container.eventRepository, 
            app.container.ticketRepository,
            app.container.favoriteRepository
        )
    )

    LaunchedEffect(eventId) { eventId?.let { viewModel.load(it) } }

    val event       by viewModel.event.collectAsStateWithLifecycle()
    val ticketCount by viewModel.ticketCount.collectAsStateWithLifecycle()
    val revenue     by viewModel.revenue.collectAsStateWithLifecycle()
    val isFavorite  by viewModel.isFavorite.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.title ?: "Event", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { 
                        event?.let { e ->
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Check out this event on Vibe: ${e.title} at ${e.location} on ${e.date}")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Event"))
                        }
                    }) { Icon(Icons.Default.Share, "Share") }
                    
                    IconButton(onClick = { 
                        event?.let { e ->
                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                data = CalendarContract.Events.CONTENT_URI
                                putExtra(CalendarContract.Events.TITLE, e.title)
                                putExtra(CalendarContract.Events.EVENT_LOCATION, e.location)
                                putExtra(CalendarContract.Events.DESCRIPTION, e.description)
                                // Standard dates are harder to parse without a proper format, 
                                // but this opens the dialog
                                putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                            }
                            context.startActivity(intent)
                        }
                    }) { Icon(Icons.Default.CalendarMonth, "Add to calendar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (event != null) {
                Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            if (event!!.isFree) {
                                Text("Free entry", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Register now", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                            } else {
                                Text("Starting from", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "UGX ${String.format(Locale.getDefault(), "%,d", event!!.priceOrdinary)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Button(
                            onClick = { navController.navigate(Screen.TicketPurchase.createRoute(event!!.id)) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text(
                                if (event!!.isFree) "Register Free" else "Book Now",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (event == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            EventDetailBody(event = event!!, padding = padding, ticketCount = ticketCount, revenue = revenue)
        }
    }
}

@Composable
private fun EventDetailBody(
    event: EventEntity,
    padding: PaddingValues,
    ticketCount: Int,
    revenue: Long
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        if (event.isFree)
                            listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.secondary)
                        else
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Column {
                if (event.isFree) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.25f), modifier = Modifier.padding(bottom = 10.dp)) {
                        Text("FREE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                }
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { it / 2 }) {
                    Text(
                        event.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 3, overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroChip(Icons.Default.CalendarToday, event.date)
                    HeroChip(Icons.Default.LocationOn, event.location)
                }
            }
        }

        // Live stats (from DB)
        if (ticketCount > 0 || revenue > 0) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$ticketCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Tickets Sold", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                if (revenue > 0) {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("UGX ${revenue / 1000}K", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Text("Revenue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
        }

        // Body
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            SectionLabel("About this event")
            Spacer(Modifier.height(8.dp))
            Text(
                event.description.ifBlank { "No description provided." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(28.dp))
            SectionLabel("Tickets")
            Spacer(Modifier.height(12.dp))

            if (event.isFree) {
                TicketCard("General Admission", "FREE", isFree = true, description = "Open to all — no payment required")
            } else {
                TicketCard("Ordinary", "UGX ${String.format(Locale.getDefault(), "%,d", event.priceOrdinary)}", description = "Standard access to the event")
                Spacer(Modifier.height(8.dp))
                TicketCard("VIP", "UGX ${String.format(Locale.getDefault(), "%,d", event.priceVIP)}", description = "Priority access + exclusive lounge")
                Spacer(Modifier.height(8.dp))
                TicketCard("VVIP", "UGX ${String.format(Locale.getDefault(), "%,d", event.priceVVIP)}", description = "Full VIP treatment + meet & greet")
            }

            Spacer(Modifier.height(100.dp)) // bottom bar clearance
        }
    }
}

@Composable
private fun HeroChip(icon: ImageVector, text: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(12.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp
    )
}

@Composable
private fun TicketCard(tier: String, price: String, description: String, isFree: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tier, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isFree) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
        }
    }
}
