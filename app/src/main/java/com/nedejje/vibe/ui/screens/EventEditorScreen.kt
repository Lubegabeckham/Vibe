package com.nedejje.vibe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditorScreen(navController: NavController, eventId: String?) {
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
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
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
        }
    }
}

@Composable
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
    )
}
