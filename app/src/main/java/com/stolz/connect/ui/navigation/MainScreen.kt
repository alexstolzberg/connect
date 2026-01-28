package com.stolz.connect.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.stolz.connect.ui.theme.ConnectPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination
    
    // Determine selected tab index
    val selectedIndex = when (currentDestination?.route) {
        Screen.Inbox.route -> 0
        Screen.All.route -> 1
        Screen.Settings.route -> 2
        else -> 0
    }
    
    Scaffold(
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
                    containerColor = ConnectPrimary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Connection",
                        tint = Color.White
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        NavGraph(
            navController = navController
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
    
    // Use ConnectPrimary (same blue as frequency pills)
    val darkBlue = ConnectPrimary
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (selectedIndex == index) {
                            Color.White // White icon when selected
                        } else {
                            darkBlue // Dark blue icon when not selected
                        }
                    )
                },
                label = { 
                    Text(
                        text = tab.label,
                        color = if (selectedIndex == index) {
                            darkBlue // Blue text when selected (not white)
                        } else {
                            darkBlue // Dark blue text when not selected
                        }
                    )
                },
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White, // White icon when selected
                    selectedTextColor = darkBlue, // Blue text when selected
                    indicatorColor = darkBlue, // Dark blue fill
                    unselectedIconColor = darkBlue, // Dark blue icon when not selected
                    unselectedTextColor = darkBlue // Dark blue text when not selected
                )
            )
        }
    }
}

data class TabItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
