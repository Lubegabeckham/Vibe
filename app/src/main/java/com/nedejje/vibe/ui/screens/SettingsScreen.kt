package com.nedejje.vibe.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// ── Supported languages ────────────────────────────────────────────────────────

private val supportedLanguages = listOf("English", "Luganda", "Swahili", "French")

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {

    // ── Preferences state ──────────────────────────────────────────────────
    var notificationsEnabled    by remember { mutableStateOf(true) }
    var eventReminders          by remember { mutableStateOf(true) }
    var promoNotifications      by remember { mutableStateOf(false) }
    var darkTheme               by remember { mutableStateOf(false) }
    var language                by remember { mutableStateOf("English") }

    // ── Language picker dialog ─────────────────────────────────────────────
    var showLanguagePicker by remember { mutableStateOf(false) }
    if (showLanguagePicker) {
        AlertDialog(
            onDismissRequest = { showLanguagePicker = false },
            title = { Text("Select Language") },
            text = {
                Column {
                    supportedLanguages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { language = lang; showLanguagePicker = false }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(lang, style = MaterialTheme.typography.bodyLarge)
                            if (language == lang) {
                                Icon(Icons.Default.Check, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguagePicker = false }) { Text("Close") }
            }
        )
    }

    // ── Clear cache confirmation ───────────────────────────────────────────
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var cacheCleared by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(cacheCleared) {
        if (cacheCleared) {
            snackbarHostState.showSnackbar("Cache cleared successfully")
            cacheCleared = false
        }
    }

    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Cache?") },
            text = { Text("This will remove all locally cached data. The app may load slightly slower on next launch.") },
            confirmButton = {
                Button(onClick = { showClearCacheDialog = false; cacheCleared = true }) { Text("Clear") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearCacheDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ── Scaffold ───────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Notifications ──────────────────────────────────────────────
            SettingsSection("Notifications", Icons.Default.Notifications) {
                SwitchSettingRow(
                    icon = Icons.Default.NotificationsActive,
                    title = "Push Notifications",
                    subtitle = "Receive alerts from Vibe",
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        notificationsEnabled = it
                        if (!it) { eventReminders = false; promoNotifications = false }
                    }
                )
                SwitchSettingRow(
                    icon = Icons.Default.CalendarToday,
                    title = "Event Reminders",
                    subtitle = "Get notified before your events",
                    checked = eventReminders && notificationsEnabled,
                    enabled = notificationsEnabled,
                    onCheckedChange = { eventReminders = it }
                )
                SwitchSettingRow(
                    icon = Icons.Default.LocalOffer,
                    title = "Promotions & Deals",
                    subtitle = "Offers and featured events",
                    checked = promoNotifications && notificationsEnabled,
                    enabled = notificationsEnabled,
                    onCheckedChange = { promoNotifications = it }
                )
            }

            // ── Appearance ─────────────────────────────────────────────────
            SettingsSection("Appearance", Icons.Default.Palette) {
                SwitchSettingRow(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = if (darkTheme) "Dark theme active" else "Light theme active",
                    checked = darkTheme,
                    onCheckedChange = { darkTheme = it }
                )
                NavigationSettingRow(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = language,
                    onClick = { showLanguagePicker = true }
                )
            }

            // ── Storage ────────────────────────────────────────────────────
            SettingsSection("Storage", Icons.Default.Storage) {
                NavigationSettingRow(
                    icon = Icons.Default.Delete,
                    title = "Clear Cache",
                    subtitle = "Free up local storage",
                    onClick = { showClearCacheDialog = true },
                    tintError = true
                )
            }

            // ── Legal ──────────────────────────────────────────────────────
            SettingsSection("Legal", Icons.Default.Gavel) {
                NavigationSettingRow(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "How we handle your data",
                    onClick = { /* open URL */ },
                    trailingIcon = Icons.AutoMirrored.Filled.OpenInNew
                )
                NavigationSettingRow(
                    icon = Icons.Default.Description,
                    title = "Terms of Service",
                    subtitle = "Rules governing app usage",
                    onClick = { /* open URL */ },
                    trailingIcon = Icons.AutoMirrored.Filled.OpenInNew
                )
                NavigationSettingRow(
                    icon = Icons.Default.Info,
                    title = "Open Source Licenses",
                    subtitle = "Third-party libraries used",
                    onClick = { /* open licenses */ }
                )
            }

            // ── About ──────────────────────────────────────────────────────
            SettingsSection("About", Icons.Default.Info) {
                InfoSettingRow(
                    icon = Icons.Default.Tag,
                    title = "App Version",
                    value = "1.0.0 (build 42)"
                )
                InfoSettingRow(
                    icon = Icons.Default.Business,
                    title = "Developer",
                    value = "Nedejje Vibe Ltd"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Section wrapper ────────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column { content() }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// ── Row types ──────────────────────────────────────────────────────────────────

@Composable
private fun SwitchSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp),
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.let {
                    if (enabled) it else it.copy(alpha = 0.4f)
                })
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun NavigationSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tintError: Boolean = false,
    trailingIcon: ImageVector = Icons.Default.ChevronRight
) {
    val tint = if (tintError) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = tint)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (tintError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InfoSettingRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(title, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}