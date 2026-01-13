package com.stolz.connect.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination
    
    // Determine selected tab index
    val selectedIndex = when (currentDestination?.route) {
        Screen.Today.route -> 0
        Screen.All.route -> 1
        Screen.About.route -> 2
        else -> 0
    }
    
    Scaffold(
        bottomBar = {
            // Only show bottom bar on main tabs (not on detail/edit screens or splash)
            if (currentDestination?.route == Screen.Today.route || 
                currentDestination?.route == Screen.All.route ||
                currentDestination?.route == Screen.About.route) {
                AnimatedNavigationBar(
                    selectedIndex = selectedIndex,
                    onTabSelected = { index ->
                        when (index) {
                            0 -> {
                                navController.navigate(Screen.Today.route) {
                                    popUpTo(Screen.Today.route) { inclusive = true }
                                }
                            }
                            1 -> {
                                navController.navigate(Screen.All.route) {
                                    popUpTo(Screen.All.route) { inclusive = true }
                                }
                            }
                            2 -> {
                                navController.navigate(Screen.About.route) {
                                    popUpTo(Screen.About.route) { inclusive = true }
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun AnimatedNavigationBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        TabItem("Today", Icons.Default.Home),
        TabItem("All", Icons.Default.List),
        TabItem("About", Icons.Default.Info)
    )
    
    NavigationBar {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                icon = { 
                    Icon(tab.icon, contentDescription = tab.label)
                },
                label = { 
                    Text(tab.label)
                },
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) }
            )
        }
    }
}

data class TabItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
