package com.stolz.connect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.stolz.connect.ui.navigation.MainScreen
import com.stolz.connect.ui.navigation.MainViewModel
import com.stolz.connect.ui.theme.ConnectTheme
import com.stolz.connect.util.NotificationManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                intent?.getLongExtra(NotificationManager.EXTRA_OPEN_CONNECTION_ID, -1L)
                    ?.takeIf { it > 0 }
                    ?.let { mainViewModel.setPendingConnectionId(it) }
            }
            
            ConnectTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    MainScreen(navController = navController, mainViewModel = mainViewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { setIntent(it) }
        intent?.getLongExtra(NotificationManager.EXTRA_OPEN_CONNECTION_ID, -1L)
            ?.takeIf { it > 0 }
            ?.let { mainViewModel.setPendingConnectionId(it) }
    }
}