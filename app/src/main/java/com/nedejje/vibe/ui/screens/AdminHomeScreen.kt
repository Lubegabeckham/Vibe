package com.nedejje.vibe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.nedejje.vibe.ui.util.eventImageName
import com.nedejje.vibe.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    navController: NavController,
    onThemeToggle: () -> Unit = {},
    isDarkMode: Boolean = true
) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            app.container.eventRepository,
            app.container.favoriteRepository
        )
    )
    val scope = rememberCoroutineScope()

    val events by viewModel.events.collectAsStateWithLifecycle()
    val currentUser by SessionManager.currentUser.collectAsState()
    var eventPendingDelete by remember { mutableStateOf<EventEntity?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    val totalEvents = events.size
    val freeEvents  = events.count { it.isFree }
    val paidEvents  = totalEvents - freeEvents

    // Delete dialog
    eventPendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { eventPendingDelete = null },
            icon  = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Event?") },
            text  = { Text("\"${target.title}\" will be permanently removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        eventPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(onClick = { eventPendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.admin_dashboard_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.email ?: "organiser",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(if (isDarkMode) Icons.Default.WbSunny else Icons.Default.Brightness2, null)
                    }
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Switch to User View") },
                                leadingIcon = { Icon(Icons.Default.People, null) },
                                onClick = { showMenu = false; navController.navigate(Screen.Home.route) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.logout_button), color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    SessionManager.logout()
                                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.EventEditor.createRoute("new")) },
                icon    = { Icon(Icons.Default.Add, null) },
                text    = { Text("New Event") },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.padding_medium), vertical = dimensionResource(R.dimen.padding_small)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
        ) {
            // Welcome banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                            ),
                            RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius))
                        )
                        .padding(dimensionResource(R.dimen.padding_large))
                ) {
                    Column {
                        Text("Welcome back,", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            text = currentUser?.name?.split(" ")?.firstOrNull() ?: "Admin",
                            style = MaterialTheme.typography.headlineSmall, // Part A: Typography
                            color = Color.White, 
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(dimensionResource(R.dimen.padding_extra_small)))
                        Text("Manage your events and guests", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                    }
                }
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_extra_small)))
            }

            // Stats row
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
                    AdminStatCard("Total Events", "$totalEvents", Icons.Default.EventNote, Modifier.weight(1f))
                    AdminStatCard("Free",         "$freeEvents",  Icons.Default.LocalOffer, Modifier.weight(1f))
                    AdminStatCard("Paid",         "$paidEvents",  Icons.Default.ConfirmationNumber, Modifier.weight(1f))
                }
            }

            // Organiser tools header
            item {
                Text(
                    text = stringResource(R.string.organizer_tools_header), 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold
                )
            }

            // Tool grid
            item {
                val firstEventId = events.firstOrNull()?.id ?: "new"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AdminToolIcon("Guests",  Icons.Default.Groups)                  { navController.navigate(Screen.GuestManager.createRoute(firstEventId)) }
                    AdminToolIcon("Budget",  Icons.Default.AccountBalanceWallet)    { navController.navigate(Screen.BudgetTracker.createRoute(firstEventId)) }
                    AdminToolIcon("Potluck", Icons.Default.Restaurant)              { navController.navigate(Screen.Contribution.createRoute(firstEventId)) }
                    AdminToolIcon("Scanner", Icons.Default.QrCodeScanner)            { navController.navigate(Screen.QrScanner.route) }
                }
            }

            // Section header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.manage_events_header), 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold
                    )
                    val countStr = if (events.size == 1) 
                        stringResource(R.string.event_count_singular, events.size)
                    else 
                        stringResource(R.string.events_count, events.size)
                    
                    Text(
                        text = countStr,
                        style = MaterialTheme.typography.bodySmall, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (events.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.spacer_extra_large)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
                    ) {
                        Icon(Icons.Default.EventNote, null, Modifier.size(dimensionResource(R.dimen.icon_size_extra_large) + 8.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("No events yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FilledTonalButton(
                            onClick = { navController.navigate(Screen.EventEditor.createRoute("new")) },
                            shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
                        ) {
                            Icon(Icons.Default.Add, null, Modifier.size(dimensionResource(R.dimen.icon_size_small) + 2.dp))
                            Spacer(Modifier.width(dimensionResource(R.dimen.padding_small)))
                            Text("Create First Event")
                        }
                    }
                }
            }

            items(events, key = { it.id }) { event ->
                AdminEventCard(
                    event          = event,
                    onEdit         = { navController.navigate(Screen.EventEditor.createRoute(event.id)) },
                    onDeleteRequest = { eventPendingDelete = event },
                    onManage       = { navController.navigate(Screen.GuestManager.createRoute(event.id)) },
                    onToggleCancel = { 
                        scope.launch { 
                            app.container.eventRepository.cancelEvent(event.id, !event.isCancelled)
                        }
                    }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun AdminStatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius_small))
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Part B: Centered Layout
        ) {
            Icon(icon, null, Modifier.size(dimensionResource(R.dimen.icon_size_medium) - 4.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_extra_small)))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminToolIcon(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(dimensionResource(R.dimen.padding_small)),
        verticalArrangement = Arrangement.Center // Part B: Centered Layout
    ) {
        Surface(
            shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, label, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(dimensionResource(R.dimen.padding_extra_small) + 2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AdminEventCard(
    event: EventEntity,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
    onManage: () -> Unit,
    onToggleCancel: () -> Unit
) {
    val context = LocalContext.current
    val imageResId: Int? = remember(event.title) {
        eventImageName(event.title)
            ?.let { name -> context.resources.getIdentifier(name, "drawable", context.packageName) }
            ?.takeIf { it != 0 }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    // Event Thumbnail
                    if (imageResId != null) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = event.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Event, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(Modifier.width(dimensionResource(R.dimen.padding_medium)))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (event.isCancelled) {
                                Spacer(Modifier.width(dimensionResource(R.dimen.padding_small)))
                                Surface(color = MaterialTheme.colorScheme.error, shape = RoundedCornerShape(dimensionResource(R.dimen.padding_extra_small))) {
                                    Text("CANCELLED", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(event.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(event.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.badge_corner_radius)),
                    color = if (event.isFree) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        if (event.isFree) stringResource(R.string.free_badge) else "${stringResource(R.string.ugx_currency)} ${String.format(Locale.getDefault(), "%,d", event.priceOrdinary)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (event.isFree) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_small), vertical = dimensionResource(R.dimen.padding_extra_small))
                    )
                }
            }
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_medium)))
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
                FilledTonalButton(
                    onClick = onManage, 
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small))
                ) {
                    Icon(Icons.Default.Groups, null, Modifier.size(dimensionResource(R.dimen.icon_size_small)))
                    Spacer(Modifier.width(dimensionResource(R.dimen.padding_extra_small)))
                    Text("Manage", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onToggleCancel,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small))
                ) {
                    Icon(if (event.isCancelled) Icons.Default.EventAvailable else Icons.Default.EventBusy, 
                        contentDescription = "Toggle Cancel", Modifier.size(dimensionResource(R.dimen.icon_size_small)),
                        tint = if (event.isCancelled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small))
                ) {
                    Icon(Icons.Default.Edit, "Edit", Modifier.size(dimensionResource(R.dimen.icon_size_small)))
                }
                OutlinedButton(
                    onClick = onDeleteRequest,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small))
                ) {
                    Icon(Icons.Default.Delete, "Delete", Modifier.size(dimensionResource(R.dimen.icon_size_small)))
                }
            }
        }
    }
}
