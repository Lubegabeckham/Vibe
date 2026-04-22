package com.nedejje.vibe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.nedejje.vibe.R
import com.nedejje.vibe.data.DataManager
import com.nedejje.vibe.data.Event
import com.nedejje.vibe.ui.navigation.Screen
import java.util.Locale

// ---------------------------------------------------------------------------
// EventDetailScreen
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(navController: NavController, eventId: String?) {
    val event = remember(eventId) {
        eventId?.takeIf { it.isNotBlank() }?.let { DataManager.getEventById(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share event")
                    }
                    IconButton(onClick = { /* TODO: Add to calendar */ }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Add to calendar")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Profile.route) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        if (event == null) {
            EventNotFound(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onBack = { navController.popBackStack() }
            )
        } else {
            EventDetailContent(
                event = event,
                padding = padding,
                onBookNow = { navController.navigate(Screen.TicketPurchase.createRoute(event.id)) }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Main content — scrollable detail view
// ---------------------------------------------------------------------------
@Composable
private fun EventDetailContent(
    event: Event,
    padding: PaddingValues,
    onBookNow: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ----------------------------------------------------------------
            // Hero header with gradient band
            // ----------------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = if (event.isFree) {
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    // FREE badge
                    if (event.isFree) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.free_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + slideInVertically { it / 2 }
                    ) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Date and location chips on gradient
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HeroChip(icon = Icons.Default.CalendarToday, text = event.date)
                        HeroChip(icon = Icons.Default.LocationOn, text = event.location)
                    }
                }
            }

            // ----------------------------------------------------------------
            // Body
            // ----------------------------------------------------------------
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(24.dp))

                // About section
                SectionLabel("About this event")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Tickets section
                SectionLabel("Tickets")
                Spacer(modifier = Modifier.height(12.dp))

                if (event.isFree) {
                    TicketCard(
                        tier = "General Admission",
                        price = stringResource(R.string.free_badge),
                        isFree = true,
                        description = "Open to all — no payment required"
                    )
                } else {
                    val ugx = stringResource(R.string.ugx_currency)
                    TicketCard(
                        tier = "Ordinary",
                        price = "$ugx ${String.format(Locale.getDefault(), "%,d", event.priceOrdinary)}",
                        description = "Standard access to the event"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TicketCard(
                        tier = "VIP",
                        price = "$ugx ${String.format(Locale.getDefault(), "%,d", event.priceVIP)}",
                        description = "Priority access + exclusive lounge"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TicketCard(
                        tier = "VVIP",
                        price = "$ugx ${String.format(Locale.getDefault(), "%,d", event.priceVVIP)}",
                        description = "Full VIP treatment + meet & greet"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // ----------------------------------------------------------------
        // Sticky bottom CTA
        // ----------------------------------------------------------------
        Surface(
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!event.isFree) {
                    Column {
                        Text(
                            text = "Starting from",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "UGX ${String.format(Locale.getDefault(), "%,d", event.priceOrdinary)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        text = "Free entry — register now",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onBookNow,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (event.isFree) "Register Free" else stringResource(R.string.book_now_button),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

@Composable
private fun HeroChip(icon: ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp
    )
}

@Composable
private fun TicketCard(
    tier: String,
    price: String,
    description: String,
    isFree: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tier, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isFree) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EventNotFound(modifier: Modifier = Modifier, onBack: () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Event not found", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onBack) { Text("Go Back") }
        }
    }
}
