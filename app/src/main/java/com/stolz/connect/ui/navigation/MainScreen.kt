package com.stolz.connect.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination
    
    // Determine selected tab index
    val selectedIndex = when (currentDestination?.route) {
        Screen.Inbox.route -> 0
        Screen.All.route -> 1
        Screen.Settings.route -> 2
        else -> 0
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showSnackbar: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    val shouldShowNotificationPrompt by mainViewModel.shouldShowNotificationPrompt.collectAsState()
    val notificationsEnabled by mainViewModel.notificationsEnabled.collectAsState()
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        mainViewModel.onNotificationPermissionResult(granted)
    }

    val isOnMainApp = currentDestination?.route == Screen.Inbox.route ||
        currentDestination?.route == Screen.All.route ||
        currentDestination?.route == Screen.Settings.route

    // Only show the prompt on first load when notifications are off (never show if user already enabled in Settings)
    if (shouldShowNotificationPrompt && isOnMainApp && !notificationsEnabled) {
        AlertDialog(
            onDismissRequest = { mainViewModel.dismissNotificationPrompt(enableNotifications = false) },
            title = { Text("Enable reminder notifications?") },
            text = {
                Text(
                    "Get notified when it's time to connect with your contacts. " +
                        "You can change this anytime in Settings."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            when (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
                                android.content.pm.PackageManager.PERMISSION_GRANTED ->
                                    mainViewModel.dismissNotificationPrompt(enableNotifications = true)
                                else ->
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            mainViewModel.dismissNotificationPrompt(enableNotifications = true)
                        }
                    }
                ) {
                    Text("Yes", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { mainViewModel.dismissNotificationPrompt(enableNotifications = false) }) {
                    Text("Not now", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Only show bottom bar on main tabs (not on detail/edit screens or splash)
            if (currentDestination?.route == Screen.Inbox.route || 
                currentDestination?.route == Screen.All.route ||
                currentDestination?.route == Screen.Settings.route) {
                AnimatedNavigationBar(
                    selectedIndex = selectedIndex,
                    onTabSelected = { index ->
                        when (index) {
                            0 -> {
                                navController.navigate(Screen.Inbox.route) {
                                    popUpTo(Screen.Inbox.route) { inclusive = true }
                                }
                            }
                            1 -> {
                                navController.navigate(Screen.All.route) {
                                    popUpTo(Screen.All.route) { inclusive = true }
                                }
                            }
                            2 -> {
                                navController.navigate(Screen.Settings.route) {
                                    popUpTo(Screen.Settings.route) { inclusive = true }
                                }
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            // Show FAB only on Inbox and All screens
            if (currentDestination?.route == Screen.Inbox.route || 
                currentDestination?.route == Screen.All.route) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.AddEdit.createRoute(null))
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Connection",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        val pendingConnectionId by mainViewModel.pendingConnectionId.collectAsState()
        NavGraph(
            navController = navController,
            pendingConnectionId = pendingConnectionId,
            onPendingConnectionIdConsumed = { mainViewModel.clearPendingConnectionId() },
            onShowSnackbar = showSnackbar
        )
    }
}

@Composable
fun AnimatedNavigationBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        TabItem("Inbox", Icons.Default.Home),
        TabItem("All", Icons.Default.List),
        TabItem("Settings", Icons.Default.Settings)
    )
    
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (selectedIndex == index) onPrimary else primary
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        color = primary
                    )
                },
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = onPrimary,
                    selectedTextColor = primary,
                    indicatorColor = primary,
                    unselectedIconColor = primary,
                    unselectedTextColor = primary
                )
            )
        }
    }
}

data class TabItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
