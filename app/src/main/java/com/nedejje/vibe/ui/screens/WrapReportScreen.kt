package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nedejje.vibe.R
import com.nedejje.vibe.data.DataManager
import com.nedejje.vibe.data.Event
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrapReportScreen(
    navController: NavController,
    eventId: String? = null
) {
    val event: Event? = remember(eventId) {
        eventId?.let { DataManager.events.find { e -> e.id == it } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post-Event Wrap Report") },
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
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacer_medium))
        ) {
            Text(text = "Summary", style = MaterialTheme.typography.headlineSmall)
            
            ReportStat("Total Attendees", "42 / 50")
            ReportStat("Total Spend", "UGX 1,050,000")
            ReportStat("Budget Variance", "UGX 50,000 (Under)")
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacer_medium)))
            Text(text = "Vendor Summary", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            
            VendorItem("Catering", "UGX 300,000", "Delivered")
            VendorItem("Venue", "UGX 500,000", "Paid")
            VendorItem("Decor", "UGX 100,000", "Paid")
            
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close Report")
            }
        }
    }
}

@Composable
fun ReportStat(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun VendorItem(name: String, amount: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
        Text(text = amount, style = MaterialTheme.typography.bodyMedium)
    }
    HorizontalDivider(thickness = 0.5.dp)
}
