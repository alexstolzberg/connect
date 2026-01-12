package com.stolz.connect.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination
    
    Scaffold(
        bottomBar = {
            // Only show bottom bar on main tabs (not on detail/edit screens)
            if (currentDestination?.route == Screen.Today.route || 
                currentDestination?.route == Screen.All.route) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Today") },
                        label = { Text("Today") },
                        selected = currentDestination?.route == Screen.Today.route,
                        onClick = {
                            navController.navigate(Screen.Today.route) {
                                popUpTo(Screen.Today.route) { inclusive = true }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = "All") },
                        label = { Text("All") },
                        selected = currentDestination?.route == Screen.All.route,
                        onClick = {
                            navController.navigate(Screen.All.route) {
                                popUpTo(Screen.All.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
