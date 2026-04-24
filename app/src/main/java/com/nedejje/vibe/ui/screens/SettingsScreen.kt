package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nedejje.vibe.session.SessionManager
import com.nedejje.vibe.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onThemeToggle: () -> Unit = {},
    isDarkMode: Boolean = true
) {
    val currentUser by SessionManager.currentUser.collectAsState()
    var notificationsEnabled by remember { mutableStateOf(true) }
    var emailUpdates         by remember { mutableStateOf(true) }
    var locationServices     by remember { mutableStateOf(false) }
    var showLogoutDialog     by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon  = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Log Out?") },
            text  = { Text("You will be signed out of your Vibe account.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        SessionManager.logout()
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Log Out") }
            },
            dismissButton = { OutlinedButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Account section
            item {
                Spacer(Modifier.height(8.dp))
                SettingsSectionHeader("Account")
            }
            item {
                SettingsInfoRow(Icons.Default.Person,  "Name",  currentUser?.name  ?: "—")
                SettingsInfoRow(Icons.Default.Email,   "Email", currentUser?.email ?: "—")
                SettingsInfoRow(Icons.Default.Phone,   "Phone", currentUser?.phone?.ifBlank { "Not set" } ?: "Not set")
                SettingsInfoRow(
                    Icons.Default.AdminPanelSettings, "Role",
                    if (currentUser?.isAdmin == true) "Event Organiser" else "Guest / Attendee"
                )
            }

            // Appearance
            item {
                Spacer(Modifier.height(8.dp))
                SettingsSectionHeader("Appearance")
            }
            item {
                SettingsToggleRow(
                    icon    = if (isDarkMode) Icons.Default.Brightness2 else Icons.Default.WbSunny,
                    label   = "Dark Mode",
                    checked = isDarkMode,
                    onCheckedChange = { onThemeToggle() }
                )
            }

            // Notifications
            item {
                Spacer(Modifier.height(8.dp))
                SettingsSectionHeader("Notifications")
            }
            item {
                SettingsToggleRow(
                    icon    = Icons.Default.Notifications,
                    label   = "Push Notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                SettingsToggleRow(
                    icon    = Icons.Default.Email,
                    label   = "Email Updates",
                    checked = emailUpdates,
                    onCheckedChange = { emailUpdates = it }
                )
            }

            // Privacy
            item {
                Spacer(Modifier.height(8.dp))
                SettingsSectionHeader("Privacy")
            }
            item {
                SettingsToggleRow(
                    icon    = Icons.Default.LocationOn,
                    label   = "Location Services",
                    checked = locationServices,
                    onCheckedChange = { locationServices = it }
                )
            }

            // About
            item {
                Spacer(Modifier.height(8.dp))
                SettingsSectionHeader("About")
            }
            item {
                SettingsInfoRow(Icons.Default.Info,      "Version",   "1.0.0")
                SettingsInfoRow(Icons.Default.Copyright, "Developer", "Nedejje Studio")
                SettingsInfoRow(Icons.Default.Public,    "Platform",  "Uganda 🇺🇬")
            }

            // Logout
            item {
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Log Out", fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text  = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, label: String, value: String) {
    Surface(
        shape  = RoundedCornerShape(12.dp),
        color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape  = RoundedCornerShape(12.dp),
        color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
