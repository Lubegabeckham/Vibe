package com.nedejje.vibe.ui.screens

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nedejje.vibe.R
import com.nedejje.vibe.VibeApplication
import com.nedejje.vibe.db.EventEntity
import com.nedejje.vibe.ui.navigation.Screen
import com.nedejje.vibe.ui.util.eventImageName
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
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
                Surface(shadowElevation = dimensionResource(R.dimen.card_elevation), color = MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.padding_large), vertical = dimensionResource(R.dimen.padding_medium)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            if (event!!.isFree) {
                                Text(
                                    text = stringResource(R.string.free_entry),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text("Register now", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                            } else {
                                Text("Starting from", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "${stringResource(R.string.ugx_currency)} ${String.format(Locale.getDefault(), "%,d", event!!.priceOrdinary)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Button(
                            onClick = { navController.navigate(Screen.TicketPurchase.createRoute(event!!.id)) },
                            shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                            modifier = Modifier.height(dimensionResource(R.dimen.button_height))
                        ) {
                            Text(
                                if (event!!.isFree) "Register Free" else stringResource(R.string.book_now_button),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
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
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Resolve drawable resource for this event
    val imageResId: Int? = remember(event.title) {
        eventImageName(event.title)
            ?.let { name -> context.resources.getIdentifier(name, "drawable", context.packageName) }
            ?.takeIf { it != 0 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Hero banner ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            // Background: event image or gradient fallback
            if (imageResId != null) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                if (event.isFree)
                                    listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.secondary)
                                else
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                            )
                        )
                )
            }

            // Dark overlay so text is always readable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x33000000), // top  ~20%
                                Color(0xDD000000)  // bottom ~87%
                            )
                        )
                    )
            )

            // Content inside hero
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = dimensionResource(R.dimen.padding_large), vertical = dimensionResource(R.dimen.spacer_large))
            ) {
                if (event.isFree) {
                    Surface(
                        shape = RoundedCornerShape(dimensionResource(R.dimen.badge_corner_radius)),
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_small))
                    ) {
                        Text(
                            stringResource(R.string.free_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                AnimatedVisibility(visible = visible, enter = fadeIn() + slideInVertically { it / 2 }) {
                    Text(
                        event.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 3, overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_medium)))
                Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
                    HeroChip(Icons.Default.CalendarToday, event.date)
                    HeroChip(Icons.Default.LocationOn, event.location)
                }
            }
        }

        // ── Live stats ───────────────────────────────────────────────────────
        if (ticketCount > 0 || revenue > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.padding_large), vertical = dimensionResource(R.dimen.padding_medium)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                Surface(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$ticketCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Tickets Sold", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                if (revenue > 0) {
                    Surface(
                        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("UGX ${revenue / 1000}K", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Text("Revenue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
        }

        // ── Body ─────────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_large))) {
            SectionLabel("About this event")
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
            Text(
                event.description.ifBlank { "No description provided." },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(dimensionResource(R.dimen.padding_large)))
            SectionLabel("Tickets")
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_medium)))

            if (event.isFree) {
                TicketCard("General Admission", stringResource(R.string.free_badge), isFree = true, description = "Open to all — no payment required")
            } else {
                TicketCard("Ordinary", "${stringResource(R.string.ugx_currency)} ${String.format(Locale.getDefault(), "%,d", event.priceOrdinary)}", description = "Standard access to the event")
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
                TicketCard("VIP", "${stringResource(R.string.ugx_currency)} ${String.format(Locale.getDefault(), "%,d", event.priceVIP)}", description = "Priority access + exclusive lounge")
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
                TicketCard("VVIP", "${stringResource(R.string.ugx_currency)} ${String.format(Locale.getDefault(), "%,d", event.priceVVIP)}", description = "Full VIP treatment + meet & greet")
            }

            Spacer(Modifier.height(100.dp))
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
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(dimensionResource(R.dimen.icon_size_extra_small)))
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
        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)), verticalAlignment = Alignment.CenterVertically) {
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