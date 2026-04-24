package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nedejje.vibe.VibeApplication
import com.nedejje.vibe.viewmodel.EventEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditorScreen(navController: NavController, eventId: String?) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: EventEditorViewModel = viewModel(
        factory = EventEditorViewModel.Factory(app.container.eventRepository)
    )

    val isNew = eventId == null || eventId == "new"
    val existingEvent by viewModel.event.collectAsStateWithLifecycle()

    LaunchedEffect(eventId) { if (!isNew && eventId != null) viewModel.load(eventId) }

    var title        by remember { mutableStateOf("") }
    var location     by remember { mutableStateOf("") }
    var date         by remember { mutableStateOf("") }
    var description  by remember { mutableStateOf("") }
    var isFree       by remember { mutableStateOf(false) }
    var priceOrd     by remember { mutableStateOf("") }
    var priceVIP     by remember { mutableStateOf("") }
    var priceVVIP    by remember { mutableStateOf("") }
    var showDelete   by remember { mutableStateOf(false) }
    var saving       by remember { mutableStateOf(false) }

    // Pre-fill when editing
    LaunchedEffect(existingEvent) {
        existingEvent?.let { e ->
            title       = e.title
            location    = e.location
            date        = e.date
            description = e.description
            isFree      = e.isFree
            priceOrd    = if (e.priceOrdinary > 0) e.priceOrdinary.toString() else ""
            priceVIP    = if (e.priceVIP > 0)      e.priceVIP.toString()      else ""
            priceVVIP   = if (e.priceVVIP > 0)     e.priceVVIP.toString()     else ""
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            icon  = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Event?") },
            text  = { Text("This will permanently remove \"$title\". This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDelete = false
                        viewModel.delete(eventId!!) { navController.popBackStack() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { OutlinedButton(onClick = { showDelete = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New Event" else "Edit Event") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!isNew) {
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Default.DeleteForever, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Basic info
            SectionHeader("Event Details")

            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Event Title *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Event, null) },
                singleLine = true
            )
            OutlinedTextField(
                value = location, onValueChange = { location = it },
                label = { Text("Location *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                singleLine = true
            )
            OutlinedTextField(
                value = date, onValueChange = { date = it },
                label = { Text("Date & Time *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                placeholder = { Text("e.g. Sat, 14 Jun 2025 · 7:00 PM") },
                singleLine = true
            )
            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 100.dp),
                leadingIcon = { Icon(Icons.Default.Description, null) },
                maxLines = 5
            )

            // Pricing
            Spacer(Modifier.height(4.dp))
            SectionHeader("Pricing")

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = isFree, onCheckedChange = { isFree = it })
                Spacer(Modifier.width(10.dp))
                Text(if (isFree) "Free Event" else "Paid Event", style = MaterialTheme.typography.bodyLarge)
            }

            if (!isFree) {
                OutlinedTextField(
                    value = priceOrd, onValueChange = { priceOrd = it.filter { c -> c.isDigit() } },
                    label = { Text("Ordinary Ticket (UGX)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.ConfirmationNumber, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = priceVIP, onValueChange = { priceVIP = it.filter { c -> c.isDigit() } },
                    label = { Text("VIP Ticket (UGX)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Star, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = priceVVIP, onValueChange = { priceVVIP = it.filter { c -> c.isDigit() } },
                    label = { Text("VVIP Ticket (UGX)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Stars, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    saving = true
                    viewModel.save(
                        id           = if (isNew) null else eventId,
                        title        = title,
                        location     = location,
                        date         = date,
                        description  = description,
                        isFree       = isFree,
                        priceOrdinary= priceOrd.toLongOrNull() ?: 0L,
                        priceVIP     = priceVIP.toLongOrNull() ?: 0L,
                        priceVVIP    = priceVVIP.toLongOrNull() ?: 0L,
                        onDone       = { navController.popBackStack() }
                    )
                },
                enabled = title.isNotBlank() && location.isNotBlank() && date.isNotBlank() && !saving,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isNew) "Create Event" else "Save Changes", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}
