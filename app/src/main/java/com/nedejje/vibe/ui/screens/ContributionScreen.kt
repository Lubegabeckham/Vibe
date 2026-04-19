package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class ContributionItem(val itemName: String, val personClaimed: String?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionScreen(navController: NavController) {
    val contributionList = listOf(
        ContributionItem("Plates", "John"),
        ContributionItem("Cups", null),
        ContributionItem("Soda", "Jane"),
        ContributionItem("Cake", null),
        ContributionItem("Music System", "Calvin")
    )

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
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            items(contributionList) { item ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = item.itemName, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.personClaimed ?: "Unclaimed",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.personClaimed == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        if (item.personClaimed == null) {
                            Button(onClick = { }, modifier = Modifier.padding(top = 8.dp)) {
                                Text("Claim")
                            }
                        }
                    }
                }
            }
        }
    }
}
