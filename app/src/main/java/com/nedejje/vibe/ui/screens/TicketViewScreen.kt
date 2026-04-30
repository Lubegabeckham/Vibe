package com.nedejje.vibe.ui.screens

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
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
import com.nedejje.vibe.db.TicketEntity
import com.nedejje.vibe.session.SessionManager
import com.nedejje.vibe.ui.util.eventImageName
import com.nedejje.vibe.viewmodel.TicketViewModel
import java.text.SimpleDateFormat
import java.util.*

// ── Brand colours ─────────────────────────────────────────────────────────────
private val VibeDeepViolet    = Color(0xFF0A0714)
private val VibeMidnightBlue  = Color(0xFF120D2B)
private val VibeElectricPlum  = Color(0xFF7C3AED)
private val VibeNeonLilac     = Color(0xFF9D5CF5)
private val VibeGoldAccent    = Color(0xFFE8B84B)
private val VibeTealAccent    = Color(0xFF00D4AA)
private val VibeSoftCream     = Color(0xFFF5F0FF)
private val VibeCardSurface   = Color(0xFF1C1540)
private val VibeDividerGrey   = Color(0xFF2E2560)

private fun getTierColors(tier: String): Pair<Color, Color> = when (tier.uppercase()) {
    "VVIP" -> Pair(VibeGoldAccent, Color(0xFF3D2800))
    "VIP"  -> Pair(VibeNeonLilac,  VibeDeepViolet)
    "FREE" -> Pair(VibeTealAccent, Color(0xFF002620))
    else   -> Pair(VibeElectricPlum, VibeDeepViolet)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketViewScreen(navController: NavController, ticketId: String?) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication

    // FIX: TicketViewModel.Factory requires THREE repos — guestRepository was missing here.
    val viewModel: TicketViewModel = viewModel(
        factory = TicketViewModel.Factory(
            app.container.ticketRepository,
            app.container.eventRepository,
            app.container.guestRepository   // ← was missing, caused "No value passed for parameter"
        )
    )

    LaunchedEffect(Unit) {
        if (SessionManager.isLoggedIn) {
            viewModel.loadForUser(SessionManager.userId)
        }
    }

    val tickets by viewModel.tickets.collectAsStateWithLifecycle()
    val ticket = tickets.find { it.id == ticketId }

    val eventState = remember { mutableStateOf<EventEntity?>(null) }
    LaunchedEffect(ticket) {
        ticket?.eventId?.let { eid ->
            eventState.value = app.container.eventRepository.getById(eid)
        }
    }

    var cardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(ticket) { if (ticket != null) cardVisible = true }

    Box(modifier = Modifier.fillMaxSize().background(VibeDeepViolet)) {
        AmbientGlow(VibeElectricPlum.copy(alpha = 0.2f), Offset(0.1f, 0.1f))
        AmbientGlow(VibeGoldAccent.copy(alpha = 0.1f), Offset(0.9f, 0.7f))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.your_ticket),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = VibeSoftCream
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = VibeNeonLilac
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            val currentEvent = eventState.value
            if (ticket == null || currentEvent == null) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VibeNeonLilac)
                }
            } else {
                TicketViewContent(
                    ticket = ticket,
                    event = currentEvent,
                    visible = cardVisible,
                    padding = padding,
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "I'm attending ${currentEvent.title}! Ticket ID: #${ticket.id.take(8).uppercase()}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Ticket"))
                    },
                    onAddToCalendar = {
                        val calendarIntent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, currentEvent.title)
                            putExtra(CalendarContract.Events.EVENT_LOCATION, currentEvent.location)
                            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                        }
                        context.startActivity(calendarIntent)
                    },
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun TicketViewContent(
    ticket: TicketEntity,
    event: EventEntity,
    visible: Boolean,
    padding: PaddingValues,
    onShare: () -> Unit,
    onAddToCalendar: () -> Unit,
    onDone: () -> Unit
) {
    val (tierFg, tierBg) = getTierColors(ticket.tier)
    val issuedAt = remember(ticket.purchasedAt) {
        SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault()).format(Date(ticket.purchasedAt))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = dimensionResource(R.dimen.padding_large),
                vertical = dimensionResource(R.dimen.padding_medium)
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 }
        ) {
            PremiumTicketCard(ticket, event, tierFg, tierBg, issuedAt)
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.spacer_large)))

        AnimatedVisibility(visible = visible, enter = fadeIn(tween(800, delayMillis = 300))) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
            ) {
                Icon(
                    Icons.Default.Info,
                    null,
                    Modifier.size(dimensionResource(R.dimen.icon_size_small)),
                    tint = VibeSoftCream.copy(alpha = 0.5f)
                )
                Text(
                    text = stringResource(R.string.entry_gate_instruction),
                    style = MaterialTheme.typography.bodySmall,
                    color = VibeSoftCream.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.padding_large)))

        AnimatedVisibility(visible = visible, enter = fadeIn(tween(800, delayMillis = 500))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f).height(dimensionResource(R.dimen.button_height)),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibeNeonLilac.copy(alpha = 0.4f))
                ) {
                    Icon(
                        Icons.Default.Share,
                        null,
                        Modifier.size(dimensionResource(R.dimen.icon_size_small) + 2.dp),
                        tint = VibeNeonLilac
                    )
                    Spacer(Modifier.width(dimensionResource(R.dimen.padding_small)))
                    Text(text = stringResource(R.string.share_ticket), color = VibeNeonLilac)
                }

                Button(
                    onClick = onDone,
                    modifier = Modifier.weight(1f).height(dimensionResource(R.dimen.button_height)),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                    colors = ButtonDefaults.buttonColors(containerColor = VibeElectricPlum)
                ) {
                    Text(text = stringResource(R.string.done_button), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(dimensionResource(R.dimen.padding_small) + 4.dp))

        TextButton(onClick = onAddToCalendar, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.CalendarToday, null, Modifier.size(dimensionResource(R.dimen.icon_size_small)))
            Spacer(Modifier.width(dimensionResource(R.dimen.padding_small)))
            Text(text = stringResource(R.string.add_to_calendar), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun PremiumTicketCard(
    ticket: TicketEntity,
    event: EventEntity,
    tierFg: Color,
    tierBg: Color,
    issuedAt: String
) {
    val context = LocalContext.current
    val imageResId: Int? = remember(event.title) {
        eventImageName(event.title)
            ?.let { name -> context.resources.getIdentifier(name, "drawable", context.packageName) }
            ?.takeIf { it != 0 }
    }

    val isVip = ticket.tier.uppercase() in listOf("VIP", "VVIP")
    val transition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by transition.animateFloat(
        0.4f, 0.8f,
        infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "alpha"
    )
    val borderColor = if (isVip) tierFg.copy(alpha = glowAlpha) else VibeDividerGrey

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius_large)))
            .background(borderColor)
            .padding(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(VibeCardSurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.qr_code_size))
            ) {
                if (imageResId != null) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = event.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(tierFg.copy(alpha = 0.8f), VibeElectricPlum)))
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(dimensionResource(R.dimen.padding_large))
                ) {
                    Surface(
                        shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small)),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = ticket.tier.uppercase(),
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = dimensionResource(R.dimen.padding_extra_small)
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = stringResource(R.string.vibe_logo_text),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(dimensionResource(R.dimen.padding_large)),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.3f)
                )
            }

            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_large)),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TicketDetailItem("DATE", event.date, Icons.Default.CalendarToday, tierFg)
                    TicketDetailItem("QTY", "× ${ticket.quantity}", Icons.Default.ConfirmationNumber, tierFg)
                }
                TicketDetailItem("VENUE", event.location, Icons.Default.LocationOn, tierFg)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TicketDetailItem("ISSUED", issuedAt, Icons.Default.Schedule, tierFg)
                    if (!event.isFree) {
                        val price = if (ticket.tier.uppercase() == "VVIP") event.priceVVIP else if (ticket.tier.uppercase() == "VIP") event.priceVIP else event.priceOrdinary
                        TicketDetailItem(
                            "PAID",
                            "${stringResource(R.string.ugx_currency)} ${String.format(Locale.getDefault(), "%,d", price)}",
                            Icons.Default.CreditCard,
                            VibeGoldAccent
                        )
                    }
                }

                Canvas(Modifier.fillMaxWidth().height(1.dp)) {
                    drawLine(
                        color = VibeDividerGrey,
                        start = Offset.Zero, end = Offset(size.width, 0f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.qr_code_size))
                            .clip(RoundedCornerShape(dimensionResource(R.dimen.qr_corner_radius)))
                            .background(Color.White)
                            .padding(dimensionResource(R.dimen.padding_medium)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCode2,
                            null,
                            Modifier.fillMaxSize(),
                            tint = VibeMidnightBlue
                        )
                    }
                    Spacer(Modifier.height(dimensionResource(R.dimen.padding_medium)))
                    Text(
                        text = "${stringResource(R.string.ticket_id_prefix)}${ticket.id.take(8).uppercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = VibeSoftCream.copy(alpha = 0.4f),
                        letterSpacing = 4.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketDetailItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                icon,
                null,
                Modifier.size(dimensionResource(R.dimen.icon_size_extra_small)),
                tint = tint.copy(alpha = 0.6f)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = VibeSoftCream.copy(alpha = 0.4f),
                letterSpacing = 1.sp
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = VibeSoftCream
        )
    }
}

@Composable
private fun AmbientGlow(color: Color, offset: Offset) {
    Canvas(Modifier.fillMaxSize().blur(100.dp)) {
        drawCircle(
            color,
            radius = size.minDimension * 0.4f,
            center = Offset(size.width * offset.x, size.height * offset.y)
        )
    }
}