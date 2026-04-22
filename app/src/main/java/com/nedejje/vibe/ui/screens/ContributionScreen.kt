package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nedejje.vibe.R
import java.util.UUID

// ── Model ──────────────────────────────────────────────────────────────────────

enum class ContributionCategory(val label: String, val emoji: String) {
    FOOD("Food", "🍱"),
    DRINKS("Drinks", "🥤"),
    EQUIPMENT("Equipment", "🔊"),
    SUPPLIES("Supplies", "🪣"),
    DESSERT("Dessert", "🎂"),
    OTHER("Other", "📦")
}

data class ContributionItem(
    val id: String = UUID.randomUUID().toString(),
    val itemName: String,
    val category: ContributionCategory = ContributionCategory.OTHER,
    val personClaimed: String? = null
)

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionScreen(navController: NavController) {

    var items by remember {
        mutableStateOf(
            listOf(
                ContributionItem(itemName = "Plates",       category = ContributionCategory.SUPPLIES,  personClaimed = "John"),
                ContributionItem(itemName = "Cups",         category = ContributionCategory.SUPPLIES,  personClaimed = null),
                ContributionItem(itemName = "Soda",         category = ContributionCategory.DRINKS,    personClaimed = "Jane"),
                ContributionItem(itemName = "Cake",         category = ContributionCategory.DESSERT,   personClaimed = null),
                ContributionItem(itemName = "Music System", category = ContributionCategory.EQUIPMENT, personClaimed = "Calvin")
            )
        )
    }

    var claimTarget by remember { mutableStateOf<ContributionItem?>(null) }
    var claimName by remember { mutableStateOf("") }
    var claimNameError by remember { mutableStateOf(false) }

    var unclaimTarget by remember { mutableStateOf<ContributionItem?>(null) }

    var showAddSheet by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemCategory by remember { mutableStateOf(ContributionCategory.OTHER) }
    var newItemNameError by remember { mutableStateOf(false) }

    var deleteTarget by remember { mutableStateOf<ContributionItem?>(null) }

    val claimed = items.count { it.personClaimed != null }
    val total = items.size
    val progress = if (total > 0) claimed.toFloat() / total else 0f
    val allClaimed = claimed == total && total > 0

    // ── Dialogs ────────────────────────────────────────────────────────────

    claimTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { claimTarget = null; claimName = ""; claimNameError = false },
            icon = { Text(target.category.emoji, style = MaterialTheme.typography.headlineMedium) },
            title = { Text("Claim \"${target.itemName}\"") },
            text = {
                OutlinedTextField(
                    value = claimName,
                    onValueChange = { claimName = it; claimNameError = false },
                    label = { Text("Your name") },
                    isError = claimNameError,
                    supportingText = { if (claimNameError) Text("Please enter your name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (claimName.isBlank()) {
                        claimNameError = true
                    } else {
                        items = items.map {
                            if (it.id == target.id) it.copy(personClaimed = claimName.trim()) else it
                        }
                        claimTarget = null
                        claimName = ""
                    }
                }) { Text("Confirm") }
            },
            dismissButton = {
                OutlinedButton(onClick = { claimTarget = null; claimName = ""; claimNameError = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    unclaimTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { unclaimTarget = null },
            title = { Text("Release Claim?") },
            text = { Text("Remove ${target.personClaimed}'s claim on \"${target.itemName}\"? It will become available again.") },
            confirmButton = {
                Button(onClick = {
                    items = items.map { if (it.id == target.id) it.copy(personClaimed = null) else it }
                    unclaimTarget = null
                }) { Text("Release") }
            },
            dismissButton = {
                OutlinedButton(onClick = { unclaimTarget = null }) { Text("Cancel") }
            }
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Remove Item?") },
            text = { Text("\"${target.itemName}\" will be removed from the list.") },
            confirmButton = {
                Button(
                    onClick = { items = items.filterNot { it.id == target.id }; deleteTarget = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Remove") }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = {
            showAddSheet = false
            newItemName = ""
            newItemCategory = ContributionCategory.OTHER
            newItemNameError = false
        }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.padding_large))
                    .padding(bottom = dimensionResource(R.dimen.spacer_large)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacer_small))
            ) {
                Text("Add Item to Potluck", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it; newItemNameError = false },
                    label = { Text("Item name") },
                    isError = newItemNameError,
                    supportingText = { if (newItemNameError) Text("Name is required") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category", style = MaterialTheme.typography.labelMedium)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ContributionCategory.entries.take(3).forEach { cat ->
                        FilterChip(
                            selected = newItemCategory == cat,
                            onClick = { newItemCategory = cat },
                            label = { Text("${cat.emoji} ${cat.label}", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ContributionCategory.entries.drop(3).forEach { cat ->
                        FilterChip(
                            selected = newItemCategory == cat,
                            onClick = { newItemCategory = cat },
                            label = { Text("${cat.emoji} ${cat.label}", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (newItemName.isBlank()) {
                            newItemNameError = true
                        } else {
                            items = items + ContributionItem(
                                itemName = newItemName.trim(),
                                category = newItemCategory
                            )
                            newItemName = ""
                            newItemCategory = ContributionCategory.OTHER
                            showAddSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add to List")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Potluck Contributions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (allClaimed)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (allClaimed) "Everything's covered! 🎉" else "Contributions",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$claimed of $total items claimed",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (allClaimed)
                                    MaterialTheme.colorScheme.tertiary
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = if (allClaimed)
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            if (items.isEmpty()) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🍽", style = MaterialTheme.typography.displayMedium)
                        Text("No items yet", style = MaterialTheme.typography.titleMedium)
                        Text("Tap + to add items guests can claim.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            items(items, key = { it.id }) { item ->
                ContributionCard(
                    item = item,
                    onClaim = { claimTarget = item },
                    onUnclaim = { unclaimTarget = item },
                    onDelete = { deleteTarget = item }
                )
            }

            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun ContributionCard(
    item: ContributionItem,
    onClaim: () -> Unit,
    onUnclaim: () -> Unit,
    onDelete: () -> Unit
) {
    val isClaimed = item.personClaimed != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isClaimed)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = item.category.emoji, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                }
            }

            Text(text = item.itemName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text = item.category.label, style = MaterialTheme.typography.labelSmall)

            if (isClaimed) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(text = item.personClaimed!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                TextButton(onClick = onUnclaim) { Text("Release", style = MaterialTheme.typography.labelSmall) }
            } else {
                Text(text = "Unclaimed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                FilledTonalButton(onClick = onClaim, modifier = Modifier.fillMaxWidth()) {
                    Text("Claim", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
