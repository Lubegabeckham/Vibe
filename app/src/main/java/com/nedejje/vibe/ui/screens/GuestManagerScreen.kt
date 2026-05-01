package com.nedejje.vibe.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nedejje.vibe.R
import com.nedejje.vibe.VibeApplication
import com.nedejje.vibe.db.GuestEntity
import com.nedejje.vibe.viewmodel.GuestManagerViewModel
import java.io.File

private fun exportGuestsToCsv(context: android.content.Context, guests: List<GuestEntity>, eventId: String) {
    val csv = buildString {
        appendLine("Name,Email,Phone,Tag,Status,Checked In,Dietary Restrictions")
        guests.forEach { g ->
            fun String.csvEscape() = "\"${replace("\"", "\"\"")}\""
            appendLine("${g.name.csvEscape()},${g.email.csvEscape()},${g.phone.csvEscape()},${g.tag.csvEscape()},${g.status.csvEscape()},${if (g.checkedIn) "Yes" else "No"},${g.dietaryRestrictions.csvEscape()}")
        }
    }
    val file = File(context.cacheDir, "guests_${eventId.take(8)}.csv")
    file.writeText(csv)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Guest List Export")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Export Guest List"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestManagerScreen(
    navController: NavController,
    eventId: String?
) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: GuestManagerViewModel = viewModel(
        factory = GuestManagerViewModel.Factory(app.container.guestRepository)
    )

    LaunchedEffect(eventId) {
        eventId?.let { viewModel.setEventId(it) }
    }

    val guests by viewModel.guests.collectAsStateWithLifecycle()
    val guestCount by viewModel.guestCount.collectAsStateWithLifecycle()
    val checkedInCount by viewModel.checkedInCount.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    
    val catAll = stringResource(R.string.cat_all)
    val categories = listOf(catAll, "Regular", "VIP", "Staff")
    var selectedFilter by remember { mutableStateOf(categories[0]) }

    val filteredGuests = remember(guests, selectedFilter, catAll) {
        if (selectedFilter == catAll) guests
        else guests.filter { it.tag == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.guest_manager_title), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.guest_manager_subtitle), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { exportGuestsToCsv(context, guests, eventId ?: "") }) {
                        Icon(Icons.Default.Share, stringResource(R.string.export_csv_desc))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
            ) {
                Icon(Icons.Default.PersonAdd, stringResource(R.string.add_guest_desc))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.padding_medium), vertical = dimensionResource(R.dimen.padding_small) + dimensionResource(R.dimen.padding_extra_small)),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small) + dimensionResource(R.dimen.padding_extra_small))
            ) {
                StatBox(stringResource(R.string.total_guests), "$guestCount", Icons.Default.Groups, Modifier.weight(1f))
                StatBox(stringResource(R.string.checked_in), "$checkedInCount", Icons.Default.CheckCircle, Modifier.weight(1f),
                    color = Color(0xFF4CAF50))
            }

            Column(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium))) {
                val progress = if (guestCount > 0) checkedInCount.toFloat() / guestCount else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.padding_small)).clip(RoundedCornerShape(dimensionResource(R.dimen.padding_extra_small))),
                    color = Color(0xFF4CAF50),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(Modifier.height(dimensionResource(R.dimen.padding_medium)))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_guest_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius_small)),
                    singleLine = true,
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, null)
                            }
                        }
                    }
                )

                Spacer(Modifier.height(dimensionResource(R.dimen.padding_small) + dimensionResource(R.dimen.padding_extra_small)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    categories.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            shape = RoundedCornerShape(dimensionResource(R.dimen.chip_corner_radius))
                        )
                    }
                }
            }

            Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))

            if (filteredGuests.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonSearch, null, Modifier.size(dimensionResource(R.dimen.icon_size_huge)), tint = MaterialTheme.colorScheme.outline)
                        Text(stringResource(R.string.no_events_found), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = dimensionResource(R.dimen.padding_medium)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_10dp)),
                    contentPadding = PaddingValues(bottom = dimensionResource(R.dimen.profile_image_size))
                ) {
                    items(filteredGuests, key = { it.id }) { guest ->
                        EnhancedGuestCard(
                            guest = guest,
                            onCheckInToggle = {
                                if (guest.checkedIn) viewModel.checkOut(guest.id)
                                else viewModel.checkIn(guest.id)
                            },
                            onDelete = { viewModel.deleteGuest(guest) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGuestDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, email, phone, tag ->
                eventId?.let { viewModel.addGuest(it, name, email, phone, tag, "") }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun StatBox(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
    ) {
        Row(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(dimensionResource(R.dimen.icon_size_large) - 4.dp), tint = color)
            Spacer(Modifier.width(dimensionResource(R.dimen.padding_small) + dimensionResource(R.dimen.padding_extra_small)))
            Column {
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = color)
                Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun EnhancedGuestCard(
    guest: GuestEntity,
    onCheckInToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
        onClick = { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (guest.checkedIn) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        border = if (guest.checkedIn) BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f)) else null
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(guest.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
                        Surface(
                            shape = RoundedCornerShape(dimensionResource(R.dimen.padding_6dp)),
                            color = when(guest.tag) {
                                "VIP" -> Color(0xFFE8B84B).copy(alpha = 0.2f)
                                "Staff" -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        ) {
                            Text(
                                text = guest.tag.uppercase(),
                                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_6dp), vertical = dimensionResource(R.dimen.padding_tiny)),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when(guest.tag) {
                                    "VIP" -> Color(0xFFB8860B)
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        }
                        if (guest.checkedIn) {
                            Text(stringResource(R.string.guest_arrived), style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCheckInToggle) {
                        Icon(
                            imageVector = if (guest.checkedIn) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = stringResource(R.string.checked_in),
                            tint = if (guest.checkedIn) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_large) - 4.dp)
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_small) + dimensionResource(R.dimen.padding_extra_small)), verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_extra_small))) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_small)), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    GuestInfoRow(Icons.Default.Email, guest.email)
                    GuestInfoRow(Icons.Default.Phone, guest.phone)
                    if (guest.dietaryRestrictions.isNotBlank()) {
                        GuestInfoRow(Icons.Default.Restaurant, "Diet: ${guest.dietaryRestrictions}")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = dimensionResource(R.dimen.padding_small)),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, null, Modifier.size(dimensionResource(R.dimen.icon_size_small)))
                            Spacer(Modifier.width(dimensionResource(R.dimen.padding_extra_small)))
                            Text(stringResource(R.string.remove_guest))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuestInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
        Icon(icon, null, Modifier.size(dimensionResource(R.dimen.icon_size_extra_small)), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AddGuestDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("Regular") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_manual_guest), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small) + dimensionResource(R.dimen.padding_extra_small))) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.full_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius_small))
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius_small))
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.phone_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius_small))
                )

                Text(stringResource(R.string.guest_category), style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
                    listOf("Regular", "VIP", "Staff").forEach { t ->
                        FilterChip(
                            selected = tag == t,
                            onClick = { tag = t },
                            label = { Text(t) },
                            shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius_small))
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, email, phone, tag) },
                shape = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius_small))
            ) { Text(stringResource(R.string.add_guest_desc)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_button)) }
        }
    )
}