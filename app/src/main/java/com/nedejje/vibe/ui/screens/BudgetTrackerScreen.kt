package com.nedejje.vibe.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nedejje.vibe.VibeApplication
import com.nedejje.vibe.db.BudgetItemEntity
import com.nedejje.vibe.viewmodel.BudgetViewModel
import java.util.Locale

// ── Category definition ────────────────────────────────────────────────────────
private enum class BudgetCategory(val label: String, val icon: ImageVector, val color: Color) {
    VENUE("Venue",       Icons.Default.LocationOn,          Color(0xFF5C6BC0)),
    CATERING("Catering", Icons.Default.Restaurant,          Color(0xFF26A69A)),
    MARKETING("Marketing", Icons.Default.Campaign,          Color(0xFFEF5350)),
    STAFF("Staff",       Icons.Default.Groups,              Color(0xFFFF7043)),
    EQUIPMENT("Equipment", Icons.Default.SpeakerGroup,      Color(0xFF8D6E63)),
    OTHER("Other",       Icons.Default.Category,            Color(0xFF78909C))
}

private fun categoryFor(label: String) =
    BudgetCategory.entries.firstOrNull { it.label == label } ?: BudgetCategory.OTHER

private fun formatUgx(amount: Double): String =
    String.format(Locale.getDefault(), "%,.0f", amount)

// ── Screen ─────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTrackerScreen(
    navController: NavController,
    eventId: String?
) {
    val context = LocalContext.current
    val app     = context.applicationContext as VibeApplication
    val viewModel: BudgetViewModel = viewModel(
        factory = BudgetViewModel.Factory(app.container.budgetRepository)
    )

    LaunchedEffect(eventId) { eventId?.let { viewModel.setEventId(it) } }

    val items      by viewModel.items.collectAsStateWithLifecycle()
    val totalSpend by viewModel.totalSpend.collectAsStateWithLifecycle()
    val paidTotal  by viewModel.paidTotal.collectAsStateWithLifecycle()

    // UI state
    var budgetLimit      by remember { mutableStateOf(5_000_000.0) }
    var showAddDialog    by remember { mutableStateOf(false) }
    var editingItem      by remember { mutableStateOf<BudgetItemEntity?>(null) }
    var showLimitDialog  by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<BudgetCategory?>(null) }
    var itemToDelete     by remember { mutableStateOf<BudgetItemEntity?>(null) }

    val filteredItems = if (selectedCategory == null) items
    else items.filter { it.category == selectedCategory!!.label }

    val remaining = (budgetLimit - totalSpend).coerceAtLeast(0.0)
    val progress  = (totalSpend / budgetLimit).coerceIn(0.0, 1.0).toFloat()
    val isOver    = totalSpend > budgetLimit

    // Category breakdown for mini-chart
    val categoryTotals = BudgetCategory.entries.mapNotNull { cat ->
        val sum = items.filter { it.category == cat.label }.sumOf { it.amount }
        if (sum > 0) cat to sum else null
    }

    // ── Delete confirmation ────────────────────────────────────────────────────
    itemToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            icon  = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Remove Item?") },
            text  = { Text("\"${target.name}\" will be removed from the budget.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteItem(target); itemToDelete = null },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Remove") }
            },
            dismissButton = {
                OutlinedButton(onClick = { itemToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Budget Tracker", fontWeight = FontWeight.Bold)
                        Text(
                            "${items.size} items · UGX ${formatUgx(totalSpend)} spent",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showLimitDialog = true }) {
                        Icon(Icons.Default.Tune, "Set Budget Limit")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick    = { showAddDialog = true },
                icon       = { Icon(Icons.Default.Add, null) },
                text       = { Text("Add Expense") },
                containerColor = MaterialTheme.colorScheme.primary,
                shape      = RoundedCornerShape(16.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {

            // ── Budget Header ──────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    if (isOver) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary,
                                    if (isOver) MaterialTheme.colorScheme.error.copy(alpha = 0.75f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("Total Spend", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
                                Text(
                                    "UGX ${formatUgx(totalSpend)}",
                                    style     = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color     = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Budget Limit", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "UGX ${formatUgx(budgetLimit)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress  = { progress },
                            modifier  = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                            color     = if (isOver) Color(0xFFFFEB3B) else Color(0xFF69F0AE),
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "${(progress * 100).toInt()}% used",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            if (isOver) {
                                Text(
                                    "OVER BUDGET by UGX ${formatUgx(totalSpend - budgetLimit)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFFEB3B),
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    "UGX ${formatUgx(remaining)} remaining",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Quick Stats ────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BudgetStatCard("Items",   "${items.size}",             Icons.Default.List,                 Modifier.weight(1f))
                    BudgetStatCard("Paid",    "UGX ${formatUgx(paidTotal)}", Icons.Default.CheckCircle,        Modifier.weight(1.5f),
                        tint = MaterialTheme.colorScheme.tertiary)
                    BudgetStatCard("Unpaid",  "UGX ${formatUgx(totalSpend - paidTotal)}", Icons.Default.PendingActions, Modifier.weight(1.5f),
                        tint = MaterialTheme.colorScheme.error)
                }
            }

            // ── Spending Breakdown Chart ───────────────────────────────────
            if (categoryTotals.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Spending by Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                            categoryTotals.forEach { (cat, amount) ->
                                val catProgress = (amount / totalSpend).toFloat().coerceIn(0f, 1f)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Surface(shape = CircleShape, color = cat.color.copy(alpha = 0.15f), modifier = Modifier.size(32.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(cat.icon, null, Modifier.size(16.dp), tint = cat.color)
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(cat.label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                            Text("UGX ${formatUgx(amount)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Spacer(Modifier.height(3.dp))
                                        LinearProgressIndicator(
                                            progress  = { catProgress },
                                            modifier  = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                            color     = cat.color,
                                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Category Filter ────────────────────────────────────────────
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick  = { selectedCategory = null },
                            label    = { Text("All") },
                            leadingIcon = if (selectedCategory == null) {{ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }} else null
                        )
                    }
                    items(BudgetCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick  = { selectedCategory = if (selectedCategory == cat) null else cat },
                            label    = { Text(cat.label) },
                            leadingIcon = {
                                Icon(cat.icon, null, Modifier.size(16.dp),
                                    tint = if (selectedCategory == cat) MaterialTheme.colorScheme.onSecondaryContainer else cat.color)
                            }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Section Header ─────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (selectedCategory != null) "${selectedCategory!!.label} Expenses" else "All Expenses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${filteredItems.size} item${if (filteredItems.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Empty state ────────────────────────────────────────────────
            if (filteredItems.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outline)
                        Text("No expenses yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (selectedCategory != null) "No ${selectedCategory!!.label} items found" else "Tap + to add your first expense",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Expense Items ──────────────────────────────────────────────
            items(filteredItems, key = { it.id }) { item ->
                ExpenseCard(
                    item     = item,
                    onEdit   = { editingItem = item },
                    onDelete = { itemToDelete = item },
                    onTogglePaid = { viewModel.togglePaid(item) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }

    // ── Add / Edit Dialog ──────────────────────────────────────────────────────
    if (showAddDialog || editingItem != null) {
        val existing = editingItem
        AddEditExpenseDialog(
            existing  = existing,
            onDismiss = { showAddDialog = false; editingItem = null },
            onConfirm = { name, amount, category, notes ->
                if (existing != null) {
                    viewModel.updateItem(existing.copy(name = name, amount = amount, category = category, notes = notes))
                } else {
                    eventId?.let { viewModel.addItem(it, name, amount, category, notes) }
                }
                showAddDialog = false
                editingItem   = null
            }
        )
    }

    // ── Budget Limit Dialog ────────────────────────────────────────────────────
    if (showLimitDialog) {
        var limitText by remember { mutableStateOf(budgetLimit.toLong().toString()) }
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            icon  = { Icon(Icons.Default.Tune, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Set Budget Limit", fontWeight = FontWeight.Bold) },
            text  = {
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { if (it.all { c -> c.isDigit() } || it.isEmpty()) limitText = it },
                    label   = { Text("Budget Limit (UGX)") },
                    prefix  = { Text("UGX ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        limitText.toDoubleOrNull()?.let { budgetLimit = it }
                        showLimitDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Apply") }
            },
            dismissButton = { TextButton(onClick = { showLimitDialog = false }) { Text("Cancel") } }
        )
    }
}

// ── Expense Card ───────────────────────────────────────────────────────────────
@Composable
private fun ExpenseCard(
    item: BudgetItemEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePaid: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cat = categoryFor(item.category)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category icon bubble
            Surface(shape = CircleShape, color = cat.color.copy(alpha = 0.15f), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(cat.icon, null, Modifier.size(20.dp), tint = cat.color)
                }
            }

            // Name, category, amount
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style    = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (item.isPaid) TextDecoration.LineThrough else null,
                    color = if (item.isPaid) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    cat.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = cat.color
                )
                if (item.notes.isNotBlank()) {
                    Text(item.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    "UGX ${formatUgx(item.amount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isPaid) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
            }

            // Actions column
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // Paid badge / toggle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (item.isPaid) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.clickable { onTogglePaid() }
                ) {
                    Text(
                        if (item.isPaid) "✓ Paid" else "Unpaid",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isPaid) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                    )
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Edit", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

// ── Add/Edit Dialog ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditExpenseDialog(
    existing: BudgetItemEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String) -> Unit
) {
    val isEdit = existing != null

    var name     by remember { mutableStateOf(existing?.name     ?: "") }
    var amount   by remember { mutableStateOf(existing?.amount?.toLong()?.toString() ?: "") }
    var notes    by remember { mutableStateOf(existing?.notes    ?: "") }
    var category by remember { mutableStateOf(
        BudgetCategory.entries.firstOrNull { it.label == existing?.category } ?: BudgetCategory.OTHER
    ) }
    var catExpanded by remember { mutableStateOf(false) }
    var nameError   by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Expense" else "Add Expense", fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Item Name *") },
                    placeholder = { Text("e.g. Venue Deposit, DJ Fee") },
                    isError = nameError,
                    supportingText = { if (nameError) Text("Name is required") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Label, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() } || it.isEmpty()) { amount = it; amountError = false } },
                    label  = { Text("Amount (UGX) *") },
                    prefix = { Text("UGX ") },
                    isError = amountError,
                    supportingText = { if (amountError) Text("Enter a valid amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category picker
                ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                    OutlinedTextField(
                        value = category.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        leadingIcon = { Icon(category.icon, null, tint = category.color) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                        BudgetCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.label) },
                                leadingIcon = { Icon(cat.icon, null, tint = cat.color) },
                                onClick = { category = cat; catExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("e.g. Paid via MTN MoMo") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Notes, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    nameError   = name.isBlank()
                    amountError = amount.isBlank() || amount.toDoubleOrNull() == null
                    if (!nameError && !amountError) {
                        onConfirm(name.trim(), amount.toDouble(), category.label, notes.trim())
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text(if (isEdit) "Save Changes" else "Add Expense") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Stat Card ──────────────────────────────────────────────────────────────────
@Composable
private fun BudgetStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier,
    tint: Color = Color.Unspecified
) {
    Card(
        modifier = modifier,
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, null, Modifier.size(18.dp), tint = if (tint == Color.Unspecified) MaterialTheme.colorScheme.primary else tint)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}