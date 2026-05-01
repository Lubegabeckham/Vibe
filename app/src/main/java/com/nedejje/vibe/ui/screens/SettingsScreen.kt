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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.nedejje.vibe.R
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
    var showLogoutDialog     by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon  = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.logout_confirm_title)) },
            text  = { Text(stringResource(R.string.logout_confirm_msg)) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        SessionManager.logout()
                        navController.navigate(Screen.Login.route) { 
                            popUpTo(0) { inclusive = true } 
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.logout_button)) }
            },
            dismissButton = { 
                OutlinedButton(onClick = { showLogoutDialog = false }) { 
                    Text(stringResource(R.string.cancel_button)) 
                } 
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = dimensionResource(R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_extra_small))
        ) {
            item {
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
                SettingsSectionHeader(stringResource(R.string.section_account))
            }
            item {
                SettingsInfoRow(Icons.Default.Person,  stringResource(R.string.label_name),  currentUser?.name  ?: "—")
                SettingsInfoRow(Icons.Default.Email,   stringResource(R.string.label_email), currentUser?.email ?: "—")
                SettingsInfoRow(Icons.Default.Phone,   stringResource(R.string.label_phone), currentUser?.phone?.ifBlank { stringResource(R.string.not_set) } ?: stringResource(R.string.not_set))
                SettingsInfoRow(
                    Icons.Default.AdminPanelSettings, stringResource(R.string.label_role),
                    if (currentUser?.isAdmin == true) stringResource(R.string.role_organizer) else stringResource(R.string.role_guest)
                )
            }

            item {
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
                SettingsSectionHeader(stringResource(R.string.section_appearance))
            }
            item {
                SettingsToggleRow(
                    icon    = if (isDarkMode) Icons.Default.Brightness2 else Icons.Default.WbSunny,
                    label   = stringResource(R.string.label_dark_mode),
                    checked = isDarkMode,
                    onCheckedChange = { onThemeToggle() }
                )
            }

            item {
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
                SettingsSectionHeader(stringResource(R.string.section_notifications))
            }
            item {
                SettingsToggleRow(
                    icon    = Icons.Default.Notifications,
                    label   = stringResource(R.string.label_push_notifications),
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
                SettingsToggleRow(
                    icon    = Icons.Default.Email,
                    label   = stringResource(R.string.label_email_updates),
                    checked = emailUpdates,
                    onCheckedChange = { emailUpdates = it }
                )
            }

            item {
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_small)))
                SettingsSectionHeader(stringResource(R.string.section_about))
            }
            item {
                SettingsInfoRow(Icons.Default.Info,      stringResource(R.string.label_version),   "1.0.0")
                SettingsInfoRow(Icons.Default.Public,    stringResource(R.string.label_platform),  stringResource(R.string.value_platform))
            }

            item {
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_medium)))
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.padding_medium)).height(dimensionResource(R.dimen.button_height)),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.button_corner_radius)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, null, Modifier.size(dimensionResource(R.dimen.icon_size_small) + dimensionResource(R.dimen.padding_tiny)))
                    Spacer(Modifier.width(dimensionResource(R.dimen.padding_small)))
                    Text(stringResource(R.string.logout_button), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(dimensionResource(R.dimen.spacer_large)))
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
        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.padding_extra_small))
    )
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, label: String, value: String) {
    Surface(
        shape  = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius_small)),
        color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.padding_tiny))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium), vertical = dimensionResource(R.dimen.padding_medium) - dimensionResource(R.dimen.padding_tiny)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(dimensionResource(R.dimen.icon_size_small) + dimensionResource(R.dimen.padding_tiny)), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(dimensionResource(R.dimen.padding_small) + dimensionResource(R.dimen.padding_extra_small)))
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
        shape  = RoundedCornerShape(dimensionResource(R.dimen.card_corner_radius_small)),
        color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.padding_tiny))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_medium), vertical = dimensionResource(R.dimen.padding_small) + dimensionResource(R.dimen.padding_tiny)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(dimensionResource(R.dimen.icon_size_small) + dimensionResource(R.dimen.padding_tiny)), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(dimensionResource(R.dimen.padding_small) + dimensionResource(R.dimen.padding_extra_small)))
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}