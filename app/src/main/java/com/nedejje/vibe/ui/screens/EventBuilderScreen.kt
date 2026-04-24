package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nedejje.vibe.R
import com.nedejje.vibe.data.DataManager
import com.nedejje.vibe.data.Event
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventBuilderScreen(
    navController: NavController,
    eventId: String? = null
) {
    val existingEvent = remember(eventId) {
        if (eventId == null || eventId == "new") null
        else DataManager.events.find { it.id == eventId }
    }
    val isEditMode = existingEvent != null

    var title by remember { mutableStateOf(existingEvent?.title ?: "") }
    var description by remember { mutableStateOf(existingEvent?.description ?: "") }
    var date by remember { mutableStateOf(existingEvent?.date ?: "") }
    var venue by remember { mutableStateOf(existingEvent?.location ?: "") }
    var isFree by remember { mutableStateOf(existingEvent?.isFree ?: true) }
    var priceOrdinary by remember { mutableStateOf(existingEvent?.priceOrdinary?.toString() ?: "") }
    var priceVIP by remember { mutableStateOf(existingEvent?.priceVIP?.toString() ?: "") }

    var titleError by remember { mutableStateOf(false) }

    fun validateAndSave() {
        titleError = title.isBlank()
        if (titleError) return

        val event = Event(
            id = existingEvent?.id ?: UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            date = date.trim(),
            location = venue.trim(),
            isFree = isFree,
            priceOrdinary = if (isFree) 0L else priceOrdinary.toLongOrNull() ?: 0L,
            priceVIP = if (isFree) 0L else priceVIP.toLongOrNull() ?: 0L,
            priceVVIP = 0L // Default for builder
        )

        if (isEditMode) DataManager.updateEvent(event)
        else DataManager.addEvent(event)

        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Event" else "Create Event") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                .padding(dimensionResource(R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacer_small))
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text(stringResource(R.string.full_name_label)) },
                isError = titleError,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = venue,
                onValueChange = { venue = it },
                label = { Text("Venue") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isFree, onCheckedChange = { isFree = it })
                Text("Free Event")
            }

            if (!isFree) {
                OutlinedTextField(
                    value = priceOrdinary,
                    onValueChange = { priceOrdinary = it },
                    label = { Text("Ordinary Price (UGX)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceVIP,
                    onValueChange = { priceVIP = it },
                    label = { Text("VIP Price (UGX)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacer_medium)))

            Button(
                onClick = { validateAndSave() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "Save Changes" else "Create Event")
            }
        }
    }
}
