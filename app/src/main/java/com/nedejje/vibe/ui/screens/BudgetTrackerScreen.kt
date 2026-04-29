package com.nedejje.vibe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Budget Tracker", fontWeight = FontWeight.Bold)
                        Text("Financial planning for event", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Budget Analytics */ }) {
                        Icon(Icons.Default.PieChart, "Analytics")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Add Expense")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Financial Header ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text("Total Estimated Spend", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "UGX ${formatBudgetUgx(totalSpend)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(Modifier.height(12.dp))
                    Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.15f)) {
                        Text(
                            "${items.size} line items recorded", 
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall, color = Color.White
                        )
                    }
                }
            }

            // ── Expense List ──────────────────────────────────────────────
            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Text("No expenses tracked yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        ExpenseItemCard(
                            item = item,
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amount ->
                eventId?.let { viewModel.addItem(it, name, amount) }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ExpenseItemCard(item: BudgetItemEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("UGX ${formatBudgetUgx(item.amount)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun AddExpenseDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, 
                    label = { Text("Item Name") }, placeholder = { Text("e.g. Venue, DJ, Catering") }
                )
                OutlinedTextField(
                    value = amount, onValueChange = { if (it.all { c -> c.isDigit() } || it.isEmpty()) amount = it }, 
                    label = { Text("Amount (UGX)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank() && amount.isNotBlank()) onConfirm(name, amount.toDoubleOrNull() ?: 0.0) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatBudgetUgx(amount: Double): String =
    String.format(Locale.getDefault(), "%,.0f", amount)
