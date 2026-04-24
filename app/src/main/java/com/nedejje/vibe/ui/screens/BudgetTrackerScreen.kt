package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.nedejje.vibe.db.BudgetItemEntity
import com.nedejje.vibe.viewmodel.BudgetViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTrackerScreen(
    navController: NavController,
    eventId: String?
) {
    val context = LocalContext.current
    val app = context.applicationContext as VibeApplication
    val viewModel: BudgetViewModel = viewModel(
        factory = BudgetViewModel.Factory(app.container.budgetRepository)
    )

    LaunchedEffect(eventId) {
        eventId?.let { viewModel.setEventId(it) }
    }

    val items by viewModel.items.collectAsStateWithLifecycle()
    val totalSpend by viewModel.totalSpend.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemAmount by remember { mutableStateOf("") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Item Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newItemAmount,
                        onValueChange = { newItemAmount = it.filter { char -> char.isDigit() } },
                        label = { Text("Amount (UGX)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newItemName.isNotBlank() && newItemAmount.isNotBlank() && eventId != null) {
                        viewModel.addItem(eventId, newItemName, newItemAmount.toDoubleOrNull() ?: 0.0)
                        newItemName = ""
                        newItemAmount = ""
                        showAddDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Tracker") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(dimensionResource(R.dimen.padding_medium))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
                    Text(text = "Total Estimated Spend", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "UGX ${formatBudgetUgx(totalSpend)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacer_medium)))
            
            Text(
                text = "Expenses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn {
                items(items, key = { it.id }) { item ->
                    ListItem(
                        headlineContent = { Text(item.name) },
                        trailingContent = {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text("UGX ${formatBudgetUgx(item.amount)}")
                                IconButton(onClick = { viewModel.deleteItem(item) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun formatBudgetUgx(amount: Double): String =
    String.format(Locale.getDefault(), "%,.0f", amount)
