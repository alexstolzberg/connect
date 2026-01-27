package com.stolz.connect.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import com.stolz.connect.platform.ContactHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onConnectionClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    // Refresh when screen resumes (comes back into focus)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Refresh connections when screen resumes
                viewModel.refreshConnections()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    var phoneNumberToCall by remember { mutableStateOf<String?>(null) }
    
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && phoneNumberToCall != null) {
            ContactHelper.makeCall(context, phoneNumberToCall!!) {}
            phoneNumberToCall = null
        }
    }
    
    fun handleCallClick(phoneNumber: String) {
        ContactHelper.makeCall(context, phoneNumber) {
            phoneNumberToCall = phoneNumber
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connections") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Connection")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Today") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("All") }
                )
            }
            
            // Tab Content
            when (selectedTabIndex) {
                0 -> TodayTab(
                    connections = uiState.todayConnections,
                    onConnectionClick = onConnectionClick,
                    onCallClick = ::handleCallClick,
                    onMessageClick = { phoneNumber ->
                        ContactHelper.sendMessage(context, phoneNumber)
                    },
                    onMarkComplete = { connection ->
                        viewModel.markAsContacted(connection)
                    }
                )
                1 -> AllTab(
                    connections = uiState.allConnections,
                    onConnectionClick = onConnectionClick,
                    onCallClick = ::handleCallClick,
                    onMessageClick = { phoneNumber ->
                        ContactHelper.sendMessage(context, phoneNumber)
                    },
                    onMarkComplete = { connection ->
                        viewModel.markAsContacted(connection)
                    }
                )
            }
        }
    }
}

@Composable
fun TodayTab(
    connections: List<com.stolz.connect.domain.model.ScheduledConnection>,
    onConnectionClick: (Long) -> Unit,
    onCallClick: (String) -> Unit,
    onMessageClick: (String) -> Unit,
    onMarkComplete: (com.stolz.connect.domain.model.ScheduledConnection) -> Unit
) {
    if (connections.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No connections due today.\nTap the + button to add one.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = connections,
                key = { it.id }
                ) { connection ->
                    ConnectionItem(
                        connection = connection,
                        isHighlighted = true,
                        onClick = { onConnectionClick(connection.id) },
                        onCallClick = { 
                            connection.contactPhoneNumber?.let { onCallClick(it) }
                        },
                        onMessageClick = { 
                            connection.contactPhoneNumber?.let { onMessageClick(it) }
                        },
                        onMarkComplete = { onMarkComplete(connection) },
                        onSnoozeClick = null
                    )
                }
        }
    }
}

@Composable
fun AllTab(
    connections: List<com.stolz.connect.domain.model.ScheduledConnection>,
    onConnectionClick: (Long) -> Unit,
    onCallClick: (String) -> Unit,
    onMessageClick: (String) -> Unit,
    onMarkComplete: (com.stolz.connect.domain.model.ScheduledConnection) -> Unit
) {
    if (connections.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No connections yet.\nTap the + button to add one.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = connections,
                key = { it.id }
                ) { connection ->
                    ConnectionItem(
                        connection = connection,
                        isHighlighted = connection.isDueToday,
                        onClick = { onConnectionClick(connection.id) },
                        onCallClick = { 
                            connection.contactPhoneNumber?.let { onCallClick(it) }
                        },
                        onMessageClick = { 
                            connection.contactPhoneNumber?.let { onMessageClick(it) }
                        },
                        onMarkComplete = { onMarkComplete(connection) },
                        onSnoozeClick = null
                    )
                }
        }
    }
}


