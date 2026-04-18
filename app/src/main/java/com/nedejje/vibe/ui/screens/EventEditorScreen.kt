package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nedejje.vibe.data.DataManager
import com.nedejje.vibe.data.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditorScreen(navController: NavController, eventId: String?) {
    val existingEvent = if (eventId != "new") DataManager.getEventById(eventId ?: "") else null

    var title by remember { mutableStateOf(existingEvent?.title ?: "") }
    var location by remember { mutableStateOf(existingEvent?.location ?: "") }
    var date by remember { mutableStateOf(existingEvent?.date ?: "") }
    var description by remember { mutableStateOf(existingEvent?.description ?: "") }
    var priceOrdinary by remember { mutableStateOf(existingEvent?.priceOrdinary?.toString() ?: "0") }
    var priceVIP by remember { mutableStateOf(existingEvent?.priceVIP?.toString() ?: "0") }
    var priceVVIP by remember { mutableStateOf(existingEvent?.priceVVIP?.toString() ?: "0") }
    var isFree by remember { mutableStateOf(existingEvent?.isFree ?: false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingEvent == null) "Add Event" else "Edit Event") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = isFree, onCheckedChange = { isFree = it })
                Text("This is a Free Event")
            }

            if (!isFree) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceOrdinary, 
                    onValueChange = { priceOrdinary = it }, 
                    label = { Text("Price Ordinary (UGX)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceVIP, 
                    onValueChange = { priceVIP = it }, 
                    label = { Text("Price VIP (UGX)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceVVIP, 
                    onValueChange = { priceVVIP = it }, 
                    label = { Text("Price VVIP (UGX)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val event = Event(
                        id = existingEvent?.id ?: java.util.UUID.randomUUID().toString(),
                        title = title,
                        location = location,
                        date = date,
                        description = description,
                        priceOrdinary = priceOrdinary.toLongOrNull() ?: 0L,
                        priceVIP = priceVIP.toLongOrNull() ?: 0L,
                        priceVVIP = priceVVIP.toLongOrNull() ?: 0L,
                        isFree = isFree
                    )
                    if (existingEvent == null) DataManager.addEvent(event) else DataManager.updateEvent(event)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Event")
            }
        }
    }
}
