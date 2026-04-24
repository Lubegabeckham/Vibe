package com.nedejje.vibe.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nedejje.vibe.VibeApplication
import com.nedejje.vibe.viewmodel.InvitationViewModel

// ── RSVP state ─────────────────────────────────────────────────────────────────

enum class RsvpStatus { PENDING, ACCEPTED, DECLINED }

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationScreen(
    navController: NavController,
    eventId: String?
) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: InvitationViewModel = viewModel(
        factory = InvitationViewModel.Factory(app.container.eventRepository)
    )

    LaunchedEffect(eventId) {
        eventId?.let { viewModel.setEventId(it) }
    }

    val event by viewModel.event.collectAsStateWithLifecycle()

    // RSVP state
    var rsvpStatus by remember { mutableStateOf(RsvpStatus.PENDING) }
    var showDeclineDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current

    // Show snackbar when message changes
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    // Display values — use real event data or placeholder
    val eventTitle    = event?.title       ?: "Annual Team Gala 2025"
    val eventDate     = event?.date        ?: "October 24th"
    val eventLocation = event?.location    ?: "Serena Hotel, Kampala"
    val eventDesc     = event?.description ?: "Join us for an unforgettable evening of celebration, great food, and wonderful company. Dress code: Smart Casual."
    val isFree        = event?.isFree      ?: true
    val ticketPrice   = event?.priceOrdinary ?: 0L

    // ── Decline confirmation ───────────────────────────────────────────────
    if (showDeclineDialog) {
        AlertDialog(
            onDismissRequest = { showDeclineDialog = false },
            title = { Text("Decline Invitation?") },
            text = { Text("Are you sure you want to decline this invitation?") },
            confirmButton = {
                Button(
                    onClick = {
                        rsvpStatus = RsvpStatus.DECLINED
                        showDeclineDialog = false
                        snackbarMessage = "You've declined the invitation."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Decline") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeclineDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invitation") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Copy invite details to clipboard
                    IconButton(onClick = {
                        val text = "You're invited to $eventTitle!\n$eventDate at $eventLocation\n$eventLocation"
                        clipboard.setText(AnnotatedString(text))
                        snackbarMessage = "Invite details copied!"
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy invite")
                    }
                    IconButton(onClick = {
                        snackbarMessage = "Share sheet opened"
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Hero banner ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎉",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = "You're Invited!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = eventTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            // ── RSVP status banner ─────────────────────────────────────────
            AnimatedVisibility(
                visible = rsvpStatus != RsvpStatus.PENDING,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val (bannerColor, bannerIcon, bannerText) = when (rsvpStatus) {
                    RsvpStatus.ACCEPTED -> Triple(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        Icons.Default.CheckCircle,
                        "You've accepted this invitation 🎊"
                    )
                    RsvpStatus.DECLINED -> Triple(
                        MaterialTheme.colorScheme.errorContainer,
                        Icons.Default.Cancel,
                        "You've declined this invitation"
                    )
                    else -> Triple(MaterialTheme.colorScheme.surface, Icons.Default.Info, "")
                }
                Card(colors = CardDefaults.cardColors(containerColor = bannerColor)) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(bannerIcon, contentDescription = null)
                        Text(bannerText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // ── Event details card ─────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Event Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    DetailRow(icon = Icons.Default.CalendarToday, label = "Date", value = eventDate)
                    DetailRow(icon = Icons.Default.LocationOn,    label = "Venue", value = eventLocation)
                    DetailRow(
                        icon = Icons.Default.ConfirmationNumber,
                        label = "Ticket",
                        value = if (isFree) "Free Entry" else "UGX ${"%,d".format(ticketPrice)}"
                    )
                }
            }

            // ── Description card ───────────────────────────────────────────
            if (eventDesc.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "About this Event",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = eventDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── RSVP actions ───────────────────────────────────────────────
            when (rsvpStatus) {
                RsvpStatus.PENDING -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                rsvpStatus = RsvpStatus.ACCEPTED
                                snackbarMessage = "RSVP confirmed! See you there 🎉"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Accept Invitation", style = MaterialTheme.typography.labelLarge)
                        }
                        OutlinedButton(
                            onClick = { showDeclineDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Decline", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                RsvpStatus.ACCEPTED -> {
                    // Allow changing mind
                    OutlinedButton(
                        onClick = { showDeclineDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Can't make it anymore? Decline")
                    }
                }
                RsvpStatus.DECLINED -> {
                    // Allow changing mind
                    FilledTonalButton(
                        onClick = {
                            rsvpStatus = RsvpStatus.ACCEPTED
                            snackbarMessage = "Welcome back! RSVP confirmed 🎉"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Changed your mind? Accept")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ── Detail row ─────────────────────────────────────────────────────────────────

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
