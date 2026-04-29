package com.nedejje.vibe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onThemeToggle: () -> Unit,
    isDarkMode: Boolean
) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(app.container.eventRepository, app.container.favoriteRepository)
    )

    val events by viewModel.events.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val currentUser by SessionManager.currentUser.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    val categories = listOf("All", "Favorites", "Free", "Music", "Tech")
    var selectedCategory by remember { mutableStateOf("All") }

    val favorites by app.container.favoriteRepository
        .observeFavorites(SessionManager.userId)
        .collectAsState(initial = emptyList())

    val filteredEvents = when (selectedCategory) {
        "Favorites" -> events.filter { it.id in favorites }
        "Free"      -> events.filter { it.isFree }
        "Music"     -> events.filter {
            it.title.contains("Jazz", true) || it.title.contains("Music", true) || it.title.contains("Concert", true)
        }
        "Tech"      -> events.filter {
            it.title.contains("Tech", true) || it.title.contains("Startup", true) || it.title.contains("Innovation", true)
        }
        else        -> events
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.hey_user, currentUser?.name?.split(" ")?.firstOrNull() ?: "there"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.discovery_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.Brightness2,
                            contentDescription = "Toggle Theme"
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            if (currentUser?.isAdmin == true) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.admin_dashboard_title)) },
                                    leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null) },
                                    onClick = { showMenu = false; navController.navigate(Screen.AdminHome.route) }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings_title)) },
                                leadingIcon = { Icon(Icons.Default.Settings, null) },
                                onClick = { showMenu = false; navController.navigate(Screen.Settings.route) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Discover") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Profile.route) },
                    icon = { Icon(Icons.Default.Person, "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = dimensionResource(R.dimen.padding_medium))
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(R.dimen.padding_medium), vertical = dimensionResource(R.dimen.padding_small)),
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) { Icon(Icons.Default.Close, null) }
                        }
                    },
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                    singleLine = true
                )
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.padding_medium)),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            leadingIcon = if (selectedCategory == cat) {
                                { Icon(Icons.Default.Check, null, Modifier.size(dimensionResource(R.dimen.icon_size_small))) }
                            } else if (cat == "Favorites") {
                                { Icon(Icons.Default.Favorite, null, Modifier.size(dimensionResource(R.dimen.icon_size_small)), tint = Color.Red) }
                            } else null
                        )
                    }
                }
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.padding_medium)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val countStr = if (filteredEvents.size == 1)
                        stringResource(R.string.event_count_singular, filteredEvents.size)
                    else
                        stringResource(R.string.events_count, filteredEvents.size)
                    Text(countStr, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (selectedCategory != "All") {
                        TextButton(onClick = { selectedCategory = "All" }) {
                            Text(stringResource(R.string.clear_filter), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            if (filteredEvents.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.spacer_extra_large)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                    ) {
                        Icon(Icons.Default.EventBusy, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.no_events_found), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.no_events_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(filteredEvents, key = { it.id }) { event ->
                UserEventCard(
                    event = event,
                    isFavorite = event.id in favorites,
                    onFavoriteToggle = { viewModel.toggleFavorite(event.id, it) },
                    onClick = { navController.navigate(Screen.EventDetail.createRoute(event.id)) }
                )
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_extra_small)))
            }
        }
    }
}

@Composable
fun UserEventCard(
    event: EventEntity,
    isFavorite: Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    // Look up drawable by name; falls back to null if no match
    val imageResId: Int? = remember(event.title) {
        eventImageName(event.title)
            ?.let { name -> context.resources.getIdentifier(name, "drawable", context.packageName) }
            ?.takeIf { it != 0 }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.padding_medium), vertical = dimensionResource(R.dimen.padding_extra_small))
            .clickable { onClick() },
        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            // ── 1. Background: drawable image or gradient fallback ──────────
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
                            Brush.linearGradient(
                                if (event.isFree)
                                    listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.secondary)
                                else
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                            )
                        )
                )
            }

            // ── 2. Transparent dark overlay ─────────────────────────────────
            // Adjust the alpha values below to change how dark the overlay is:
            //   0x44 = 27%  |  0x88 = 53%  |  0xCC = 80%  |  0xFF = 100%
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x44000000), // top — lighter so image shows
                                Color(0xCC000000)  // bottom — darker for text readability
                            )
                        )
                    )
            )

            // ── 3. Coloured accent bar at top ───────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.TopStart)
                    .background(
                        Brush.horizontalGradient(
                            if (event.isFree)
                                listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.secondary)
                            else
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                        )
                    )
            )

            // ── 4. Text content ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.padding_medium)),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title + favourite button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        modifier = Modifier.weight(1f).padding(end = dimensionResource(R.dimen.padding_small))
                    )
                    IconButton(
                        onClick = { onFavoriteToggle(!isFavorite) },
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium))
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else Color.White,
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium) - 4.dp)
                        )
                    }
                }

                // Date, location, price / free badge
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday, null,
                            Modifier.size(dimensionResource(R.dimen.icon_size_extra_small) + 1.dp),
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.width(dimensionResource(R.dimen.padding_extra_small) + 1.dp))
                        Text(event.date, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn, null,
                            Modifier.size(dimensionResource(R.dimen.icon_size_extra_small) + 1.dp),
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.width(dimensionResource(R.dimen.padding_extra_small) + 1.dp))
                        Text(
                            event.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!event.isFree) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("From", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                Text(
                                    "UGX ${String.format(Locale.getDefault(), "%,d", event.priceOrdinary)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward, null,
                                    Modifier
                                        .padding(dimensionResource(R.dimen.padding_small))
                                        .size(dimensionResource(R.dimen.icon_size_small)),
                                    tint = Color.White
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(dimensionResource(R.dimen.badge_corner_radius)),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                stringResource(R.string.free_entry),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(
                                    horizontal = dimensionResource(R.dimen.padding_small) + 2.dp,
                                    vertical = dimensionResource(R.dimen.padding_extra_small)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}