package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nedejje.vibe.R
import java.util.Locale

data class BudgetItem(val name: String, val amount: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTrackerScreen(navController: NavController) {
    val items = listOf(
        BudgetItem("Venue Rental", 500000.0),
        BudgetItem("Catering", 300000.0),
        BudgetItem("Decorations", 100000.0),
        BudgetItem("Drinks", 150000.0)
    )
    val total = items.sumOf { it.amount }

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
                    Text(text = "UGX ${formatBudgetUgx(total)}", style = MaterialTheme.typography.headlineMedium)
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacer_medium)))
            LazyColumn {
                items(items) { item ->
                    ListItem(
                        headlineContent = { Text(item.name) },
                        trailingContent = { Text("UGX ${formatBudgetUgx(item.amount)}") }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun formatBudgetUgx(amount: Double): String =
    String.format(Locale.getDefault(), "%,.0f", amount)
