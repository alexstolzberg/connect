package com.stolz.connect.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.stolz.connect.ui.addedit.AddEditScreen
import com.stolz.connect.ui.about.AboutScreen
import com.stolz.connect.ui.details.ConnectionDetailsScreen
import com.stolz.connect.ui.home.AllScreen
import com.stolz.connect.ui.home.InboxScreen
import com.stolz.connect.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Inbox : Screen("inbox")
    object All : Screen("all")
    object Settings : Screen("settings")
    object About : Screen("about")
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
    modifier: Modifier = Modifier,
    pendingConnectionId: Long? = null,
    onPendingConnectionIdConsumed: () -> Unit = {},
    onShowSnackbar: (String) -> Unit = {}
) {
    androidx.compose.runtime.LaunchedEffect(pendingConnectionId) {
        pendingConnectionId?.let { id ->
            navController.navigate(Screen.Inbox.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
            navController.navigate(Screen.Details.createRoute(id))
            onPendingConnectionIdConsumed()
        }
    }
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            com.stolz.connect.ui.splash.SplashScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.Inbox.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Inbox.route) {
            InboxScreen(
                onAddClick = {
                    navController.navigate(Screen.AddEdit.createRoute(null))
                },
                onConnectionClick = { connectionId ->
                    navController.navigate(Screen.Details.createRoute(connectionId))
                },
                onShowSnackbar = onShowSnackbar
            )
        }
        
        composable(Screen.All.route) {
            AllScreen(
                onAddClick = {
                    navController.navigate(Screen.AddEdit.createRoute(null))
                },
                onConnectionClick = { connectionId ->
                    navController.navigate(Screen.Details.createRoute(connectionId))
                },
                onShowSnackbar = onShowSnackbar
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                },
                onShowSnackbar = onShowSnackbar
            )
        }
        
        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = {
                    navController.popBackStack()
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
                },
                onShowSnackbar = onShowSnackbar
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
                },
                onShowSnackbar = onShowSnackbar
            )
        }
    }
}
