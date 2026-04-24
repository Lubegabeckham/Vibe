package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nedejje.vibe.R
import com.nedejje.vibe.VibeApplication
import com.nedejje.vibe.viewmodel.WrapReportViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrapReportScreen(
    navController: NavController,
    eventId: String?
) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: WrapReportViewModel = viewModel(
        factory = WrapReportViewModel.Factory(
            app.container.eventRepository,
            app.container.guestRepository,
            app.container.budgetRepository,
            app.container.ticketRepository
        )
    )

    LaunchedEffect(eventId) {
        eventId?.let { viewModel.setEventId(it) }
    }

    val event by viewModel.event.collectAsStateWithLifecycle()
    val guestCount by viewModel.guestCount.collectAsStateWithLifecycle()
    val checkedInCount by viewModel.checkedInCount.collectAsStateWithLifecycle()
    val totalSpend by viewModel.totalSpend.collectAsStateWithLifecycle()
    val ticketRevenue by viewModel.ticketRevenue.collectAsStateWithLifecycle()
    val budgetItems by viewModel.budgetItems.collectAsStateWithLifecycle()

    val profitOrLoss = ticketRevenue - totalSpend

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
            event?.let {
                Text(
                    text = it.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(text = "Financial Summary", style = MaterialTheme.typography.titleMedium)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (profitOrLoss >= 0) 
                        MaterialTheme.colorScheme.tertiaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (profitOrLoss >= 0) "Net Profit" else "Net Loss",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "UGX ${formatAmount(profitOrLoss.toDouble())}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Revenue", "UGX ${formatAmount(ticketRevenue.toDouble())}", Modifier.weight(1f))
                StatCard("Expenses", "UGX ${formatAmount(totalSpend)}", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Attendance Summary", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            ReportStat("Total Guests", "$guestCount")
            ReportStat("Checked In", "$checkedInCount")
            ReportStat("Show-up Rate", "${if (guestCount > 0) (checkedInCount * 100 / guestCount) else 0}%")

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Expense Breakdown", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            
            if (budgetItems.isEmpty()) {
                Text("No expenses recorded", style = MaterialTheme.typography.bodySmall)
            } else {
                budgetItems.forEach { item ->
                    VendorItem(item.name, "UGX ${formatAmount(item.amount)}", "Paid")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
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
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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

private fun formatAmount(amount: Double): String =
    String.format(Locale.getDefault(), "%,.0f", amount)
