package com.stolz.connect.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.stolz.connect.ui.addedit.AddEditScreen
import com.stolz.connect.ui.details.ConnectionDetailsScreen
import com.stolz.connect.ui.home.AllScreen
import com.stolz.connect.ui.home.TodayScreen

sealed class Screen(val route: String) {
    object Today : Screen("today")
    object All : Screen("all")
    object AddEdit : Screen("add_edit/{connectionId}") {
        fun createRoute(connectionId: Long? = null) = if (connectionId != null) {
            "add_edit/$connectionId"
        } else {
            "add_edit/-1"  // Use -1 to indicate new connection
        }
    }
    object Details : Screen("details/{connectionId}") {
        fun createRoute(connectionId: Long) = "details/$connectionId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Today.route,
        modifier = modifier
    ) {
        composable(Screen.Today.route) {
            TodayScreen(
                onAddClick = {
                    navController.navigate(Screen.AddEdit.createRoute(null))
                },
                onConnectionClick = { connectionId ->
                    navController.navigate(Screen.Details.createRoute(connectionId))
                }
            )
        }
        
        composable(Screen.All.route) {
            AllScreen(
                onAddClick = {
                    navController.navigate(Screen.AddEdit.createRoute(null))
                },
                onConnectionClick = { connectionId ->
                    navController.navigate(Screen.Details.createRoute(connectionId))
                }
            )
        }
        
        composable(
            route = Screen.AddEdit.route,
            arguments = listOf(navArgument("connectionId") { 
                type = NavType.LongType
                defaultValue = -1L  // Use -1 to indicate new connection
            })
        ) { backStackEntry ->
            val connectionIdArg = backStackEntry.arguments?.getLong("connectionId") ?: -1L
            val connectionId = if (connectionIdArg > 0) connectionIdArg else null
            AddEditScreen(
                connectionId = connectionId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.Details.route,
            arguments = listOf(navArgument("connectionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val connectionId = backStackEntry.arguments?.getLong("connectionId") ?: 0
            ConnectionDetailsScreen(
                connectionId = connectionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditClick = { id ->
                    navController.navigate(Screen.AddEdit.createRoute(id))
                }
            )
        }
    }
}
