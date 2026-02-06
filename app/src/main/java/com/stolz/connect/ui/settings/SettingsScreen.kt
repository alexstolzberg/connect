package com.stolz.connect.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.stolz.connect.ui.theme.Dimensions
import com.stolz.connect.data.preferences.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToAbout: () -> Unit,
    onShowSnackbar: (String) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val defaultReminderTime by viewModel.defaultReminderTime.collectAsState()
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setNotificationsEnabled(true)
        } else {
            onShowSnackbar("Permission denied. Enable notifications in system settings to receive reminders.")
        }
    }

    fun requestNotificationPermissionIfNeeded(turnOn: Boolean) {
        if (!turnOn) {
            viewModel.setNotificationsEnabled(false)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
                android.content.pm.PackageManager.PERMISSION_GRANTED -> viewModel.setNotificationsEnabled(true)
                else -> notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            viewModel.setNotificationsEnabled(true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = "Appearance") {
                ThemeSettingItem(
                    title = "Theme",
                    currentMode = themeMode,
                    onModeSelected = { viewModel.setThemeMode(it) }
                )
            }

            HorizontalDivider()

            SettingsSection(title = "Notifications") {
                ListItem(
                    headlineContent = { Text("Reminder notifications") },
                    supportingContent = {
                        Text(
                            "Receive push notifications when it's time to connect",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { requestNotificationPermissionIfNeeded(it) }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                ListItem(
                    headlineContent = { Text("Default reminder time") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null
                        )
                    },
                    supportingContent = {
                        Text(
                            "Time of day for reminders when not set per connection (e.g. 10:00 AM)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    trailingContent = {
                        Text(
                            text = formatTimeForDisplay(defaultReminderTime),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val (hour, minute) = parseTime(defaultReminderTime)
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    viewModel.setDefaultReminderTime("%02d:%02d".format(h, m))
                                },
                                hour,
                                minute,
                                false
                            ).show()
                        }
                )
            }

            HorizontalDivider()

            SettingsSection(title = "About") {
                SettingsItem(
                    title = "About Connect",
                    icon = Icons.Default.Info,
                    onClick = onNavigateToAbout
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(
                horizontal = Dimensions.medium,
                vertical = Dimensions.small
            ),
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
fun SettingsItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .then(
                Modifier.clickable { onClick() }
            )
    )
}

@Composable
fun ThemeSettingItem(
    title: String,
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Text(
                text = when (currentMode) {
                    ThemeMode.SYSTEM -> "System Default"
                    ThemeMode.LIGHT -> "Light"
                    ThemeMode.DARK -> "Dark"
                },
                style = MaterialTheme.typography.bodySmall
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    )
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column {
                    ThemeOption(
                        title = "System Default",
                        selected = currentMode == ThemeMode.SYSTEM,
                        onClick = {
                            onModeSelected(ThemeMode.SYSTEM)
                            showDialog = false
                        }
                    )
                    ThemeOption(
                        title = "Light",
                        selected = currentMode == ThemeMode.LIGHT,
                        onClick = {
                            onModeSelected(ThemeMode.LIGHT)
                            showDialog = false
                        }
                    )
                    ThemeOption(
                        title = "Dark",
                        selected = currentMode == ThemeMode.DARK,
                        onClick = {
                            onModeSelected(ThemeMode.DARK)
                            showDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = Dimensions.small, horizontal = Dimensions.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** Parses "HH:mm" to (hour24, minute). Defaults to 10:00 if invalid. */
private fun parseTime(time: String): Pair<Int, Int> {
    val parts = time.trim().split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 10
    val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
    return hour to minute
}

/** Formats "HH:mm" for display (e.g. "10:00 AM", "2:30 PM"). */
private fun formatTimeForDisplay(time: String): String {
    val (hour24, minute) = parseTime(time)
    val (hour12, amPm) = when {
        hour24 == 0 -> 12 to "AM"
        hour24 < 12 -> hour24 to "AM"
        hour24 == 12 -> 12 to "PM"
        else -> (hour24 - 12) to "PM"
    }
    return "%d:%02d %s".format(hour12, minute, amPm)
}
