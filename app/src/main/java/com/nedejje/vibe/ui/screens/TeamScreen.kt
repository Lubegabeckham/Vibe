package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.nedejje.vibe.R

data class TeamMember(val name: String, val role: String, val id: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(navController: NavController) {
    val team = listOf(
        TeamMember("LUBEGA BECKHAM JUSPER", "Project Manager", "24/2/306/D/184"),
        TeamMember("KWAGALA DEBORAH", "System Analyst", "24/2/314/D/716"),
        TeamMember("WASSWA CALVIN", "Backend Developer", "24/2/306/W/180"),
        TeamMember("NYOMBI ABUBAKER", "Frontend Developer", "24/2/314/D/002"),
        TeamMember("TAYEBWA RONALD", "UI/UX Designer & Tester", "25/2/314/D/3263")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Team", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(dimensionResource(R.dimen.padding_medium))
        ) {
            item {
                Text(
                    text = "Vibe Development Team",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacer_medium)))
            }
            items(team) { member ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.padding_extra_small)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius))
                ) {
                    Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))) {
                        Text(
                            text = member.name, 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = member.role, 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Reg No: ${member.id}", 
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
