package com.nedejje.vibe.ui.screens

<<<<<<< HEAD
=======
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
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
<<<<<<< HEAD
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nedejje.vibe.VibeApplication
import com.nedejje.vibe.viewmodel.EventEditorViewModel
=======
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nedejje.vibe.R
import com.nedejje.vibe.data.DataManager
import com.nedejje.vibe.data.Event

// ---------------------------------------------------------------------------
// Validation helpers
// ---------------------------------------------------------------------------
private fun String.isValidPrice()    = this.toLongOrNull()?.let { it >= 0 } ?: false
private fun String.isValidTitle()    = this.trim().length >= 3
private fun String.isValidDate()     = this.trim().isNotBlank()
private fun String.isValidLocation() = this.trim().isNotBlank()
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditorScreen(navController: NavController, eventId: String?) {
<<<<<<< HEAD
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
=======
    val existingEvent = remember(eventId) {
        if (eventId != null && eventId != "new") DataManager.getEventById(eventId) else null
    }
    val isEditing = existingEvent != null

    // Form state
    var title         by remember { mutableStateOf(existingEvent?.title ?: "") }
    var location      by remember { mutableStateOf(existingEvent?.location ?: "") }
    var date          by remember { mutableStateOf(existingEvent?.date ?: "") }
    var time          by remember { mutableStateOf(existingEvent?.time ?: "") }
    var description   by remember { mutableStateOf(existingEvent?.description ?: "") }
    var priceOrdinary by remember { mutableStateOf(existingEvent?.priceOrdinary?.toString() ?: "") }
    var priceVIP      by remember { mutableStateOf(existingEvent?.priceVIP?.toString() ?: "") }
    var priceVVIP     by remember { mutableStateOf(existingEvent?.priceVVIP?.toString() ?: "") }
    var isFree        by remember { mutableStateOf(existingEvent?.isFree ?: false) }

    // Dirty-tracking
    var titleTouched    by remember { mutableStateOf(false) }
    var locationTouched by remember { mutableStateOf(false) }
    var dateTouched     by remember { mutableStateOf(false) }

    var showDiscardDialog by remember { mutableStateOf(false) }

    // Derived validity
    val titleError         = titleTouched    && !title.isValidTitle()
    val locationError      = locationTouched && !location.isValidLocation()
    val dateError          = dateTouched     && !date.isValidDate()
    val priceOrdinaryError = !isFree && priceOrdinary.isNotEmpty() && !priceOrdinary.isValidPrice()
    val priceVIPError      = !isFree && priceVIP.isNotEmpty() && !priceVIP.isValidPrice()
    val priceVVIPError     = !isFree && priceVVIP.isNotEmpty() && !priceVVIP.isValidPrice()

    val isSaveEnabled = title.isValidTitle()
            && location.isValidLocation()
            && date.isValidDate()
            && (isFree || (priceOrdinary.isValidPrice() && priceVIP.isValidPrice() && priceVVIP.isValidPrice()))

    val hasChanges = title         != (existingEvent?.title    ?: "")   ||
            location      != (existingEvent?.location ?: "")   ||
            date          != (existingEvent?.date     ?: "")   ||
            time          != (existingEvent?.time     ?: "")   ||
            description   != (existingEvent?.description ?: "") ||
            isFree        != (existingEvent?.isFree   ?: false)

    val onBackPressed: () -> Unit = {
        if (hasChanges) showDiscardDialog = true else navController.popBackStack()
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Going back will discard them.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    navController.popBackStack()
                }) { Text("Discard", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep editing") }
            }
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
<<<<<<< HEAD
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
=======
                title = {
                    Column {
                        Text(
                            text = if (isEditing) "Edit Event" else "New Event",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEditing) "Update event details" else "Create a new gathering",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
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
<<<<<<< HEAD
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
=======
        ) {
            FormSection(title = "Basic Info", icon = Icons.Default.Info) {
                EditorTextField(
                    value = title,
                    onValueChange = { title = it; titleTouched = true },
                    label = "Event Title",
                    placeholder = "e.g. Nyege Nyege 2025",
                    icon = Icons.Default.Event,
                    isError = titleError,
                    errorMessage = "Title too short"
                )
                EditorTextField(
                    value = location,
                    onValueChange = { location = it; locationTouched = true },
                    label = "Location",
                    placeholder = "e.g. Lugogo Cricket Oval",
                    icon = Icons.Default.LocationOn,
                    isError = locationError,
                    errorMessage = "Location required"
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EditorTextField(
                        value = date,
                        onValueChange = { date = it; dateTouched = true },
                        label = "Date",
                        placeholder = "Oct 24",
                        icon = Icons.Default.CalendarToday,
                        isError = dateError,
                        modifier = Modifier.weight(1f)
                    )
                    EditorTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = "Time",
                        placeholder = "7:00 PM",
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f)
                    )
                }
                EditorTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    placeholder = "Tell guests more...",
                    icon = Icons.Default.Description,
                    singleLine = false,
                    minLines = 3
                )
            }

            FormSection(title = "Tickets & Pricing", icon = Icons.Default.ConfirmationNumber) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFree) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Free Event", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text("Guests won't pay to attend", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = isFree, onCheckedChange = { isFree = it })
                    }
                }

                AnimatedVisibility(visible = !isFree, enter = expandVertically(), exit = shrinkVertically()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Spacer(Modifier.height(4.dp))
                        PriceField(value = priceOrdinary, onValueChange = { priceOrdinary = it }, label = "Ordinary", isError = priceOrdinaryError)
                        PriceField(value = priceVIP, onValueChange = { priceVIP = it }, label = "VIP", isError = priceVIPError)
                        PriceField(value = priceVVIP, onValueChange = { priceVVIP = it }, label = "VVIP", isError = priceVVIPError)
                    }
                }
            }

            Button(
                onClick = {
                    val event = Event(
                        id = existingEvent?.id ?: java.util.UUID.randomUUID().toString(),
                        title = title.trim(),
                        location = location.trim(),
                        date = date.trim(),
                        time = time.trim(),
                        description = description.trim(),
                        priceOrdinary = priceOrdinary.toLongOrNull() ?: 0L,
                        priceVIP = priceVIP.toLongOrNull() ?: 0L,
                        priceVVIP = priceVVIP.toLongOrNull() ?: 0L,
                        isFree = isFree
                    )
                    if (isEditing) DataManager.updateEvent(event) else DataManager.addEvent(event)
                    navController.popBackStack()
                },
                enabled = isSaveEnabled,
                modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_medium)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (isEditing) Icons.Default.Save else Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isEditing) "Save Changes" else "Create Event", fontWeight = FontWeight.Bold)
            }
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
        }
    }
}

@Composable
<<<<<<< HEAD
private fun SectionHeader(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
=======
private fun FormSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
        }
    }
}

@Composable
private fun EditorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    isError: Boolean = false,
    errorMessage: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(20.dp), tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) },
        isError = isError,
        supportingText = if (isError) { { Text(errorMessage) } } else null,
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun PriceField(value: String, onValueChange: (String) -> Unit, label: String, isError: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.all { c -> c.isDigit() }) onValueChange(it) },
        label = { Text("$label (UGX)") },
        leadingIcon = { Text("UGX", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp)) },
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
>>>>>>> 9dbc67af9349f791959aa207369fc9c3a9587faa
    )
}
