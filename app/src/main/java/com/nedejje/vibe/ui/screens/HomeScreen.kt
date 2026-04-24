package com.nedejje.vibe.ui.screens

<<<<<<< HEAD
import androidx.compose.animation.*
=======
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
<<<<<<< HEAD
import androidx.compose.foundation.lazy.LazyRow
=======
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
<<<<<<< HEAD
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
import com.nedejje.vibe.session.SessionManager
import com.nedejje.vibe.ui.navigation.Screen
import com.nedejje.vibe.viewmodel.HomeViewModel
=======
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nedejje.vibe.R
import com.nedejje.vibe.data.DataManager
import com.nedejje.vibe.data.Event
import com.nedejje.vibe.ui.navigation.Screen
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onThemeToggle: () -> Unit,
    isDarkMode: Boolean
) {
<<<<<<< HEAD
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(app.container.eventRepository))

    val events by viewModel.events.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val currentUser by SessionManager.currentUser.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    // Category filter
    val categories = listOf("All", "Free", "Music", "Sports", "Food", "Tech")
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredEvents = when (selectedCategory) {
        "Free"  -> events.filter { it.isFree }
        "Music" -> events.filter { it.title.contains("Jazz", true) || it.title.contains("Music", true) || it.title.contains("Concert", true) }
        "Tech"  -> events.filter { it.title.contains("Tech", true) || it.title.contains("Startup", true) || it.title.contains("Innovation", true) }
        else    -> events
    }
=======
    var showMenu by remember { mutableStateOf(false) }
    val events = DataManager.events
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa

    Scaffold(
        topBar = {
            TopAppBar(
<<<<<<< HEAD
                title = {
                    Column {
                        Text(
                            text = "Hey, ${currentUser?.name?.split(" ")?.firstOrNull() ?: "there"} 👋",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Discover Events",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
=======
                title = { Text(stringResource(R.string.discovery_title)) },
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.Brightness2,
                            contentDescription = "Toggle Theme"
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
<<<<<<< HEAD
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            if (currentUser?.isAdmin == true) {
                                DropdownMenuItem(
                                    text = { Text("Admin Dashboard") },
                                    leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null) },
                                    onClick = { showMenu = false; navController.navigate(Screen.AdminHome.route) }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                leadingIcon = { Icon(Icons.Default.Settings, null) },
                                onClick = { showMenu = false; navController.navigate(Screen.Settings.route) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
=======
                            Icon(Icons.Default.MoreVert, contentDescription = "Quick Access")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.admin_dashboard_title)) },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(Screen.AdminHome.route)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings_title)) },
                                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    navController.navigate(Screen.Settings.route)
                                }
                            )
                        }
                    }
                }
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
<<<<<<< HEAD
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Discover") }
=======
                    onClick = { /* already home */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Profile.route) },
<<<<<<< HEAD
                    icon = { Icon(Icons.Default.Person, "Profile") },
=======
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
<<<<<<< HEAD
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search events, locations...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            // Category chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            leadingIcon = if (selectedCategory == cat) {
                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Event count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${filteredEvents.size} event${if (filteredEvents.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedCategory != "All") {
                        TextButton(onClick = { selectedCategory = "All" }) {
                            Text("Clear filter", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Empty state
            if (filteredEvents.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.EventBusy, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("No events found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Try a different category or search term", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Events list
            items(filteredEvents, key = { it.id }) { event ->
                UserEventCard(event = event, onClick = {
                    navController.navigate(Screen.EventDetail.createRoute(event.id))
                })
                Spacer(Modifier.height(4.dp))
=======
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(dimensionResource(R.dimen.padding_medium))
        ) {
            item {
                Text(
                    text = stringResource(R.string.discover_events_header),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacer_medium)))
            }
            items(events) { event ->
                UserEventCard(event) {
                    navController.navigate(Screen.EventDetail.createRoute(event.id))
                }
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacer_small)))
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
            }
        }
    }
}

@Composable
<<<<<<< HEAD
fun UserEventCard(event: EventEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Gradient accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(
                        Brush.horizontalGradient(
                            if (event.isFree)
                                listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.secondary)
                            else
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                        )
                    )
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
=======
fun UserEventCard(event: Event, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        brush = if (event.isFree) {
                            Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary))
                        } else {
                            Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer))
                        }
                    )
            )
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
<<<<<<< HEAD
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    if (event.isFree) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(
                                "FREE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
=======
                        modifier = Modifier.weight(1f)
                    )
                    if (event.isFree) {
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(
                                text = stringResource(R.string.free_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
                            )
                        }
                    }
                }
<<<<<<< HEAD

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(5.dp))
                    Text(event.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(5.dp))
                    Text(event.location, style = MaterialTheme.typography.bodySmall,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (!event.isFree) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("From", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "UGX ${String.format(Locale.getDefault(), "%,d", event.priceOrdinary)}",
=======
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = event.date, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = event.location, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (!event.isFree) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(text = "From", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "UGX ${String.format(Locale.getDefault(), "%,d", event.priceOrdinary)}",
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
<<<<<<< HEAD
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward, null,
                                Modifier.padding(8.dp).size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
=======
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.padding(8.dp).size(16.dp))
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
                        }
                    }
                }
            }
        }
    }
}
