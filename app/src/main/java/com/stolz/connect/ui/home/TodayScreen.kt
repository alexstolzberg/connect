package com.stolz.connect.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material.ExperimentalMaterialApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.stolz.connect.platform.ContactHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TodayScreen(
    onAddClick: () -> Unit,
    onConnectionClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Refresh when screen resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
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
    
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.refreshConnections()
            scope.launch {
                delay(800)
                isRefreshing = false
            }
        }
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Connection")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            if (uiState.todayConnections.isEmpty()) {
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = paddingValues.calculateTopPadding(),
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.todayConnections,
                            key = { it.id }
                        ) { connection ->
                            ConnectionItem(
                                connection = connection,
                                isHighlighted = true,
                                onClick = { onConnectionClick(connection.id) },
                                onCallClick = { 
                                    connection.contactPhoneNumber?.let { handleCallClick(it) }
                                },
                                onMessageClick = {
                                    connection.contactPhoneNumber?.let { ContactHelper.sendMessage(context, it) }
                                },
                                onEmailClick = {
                                    connection.contactEmail?.let { ContactHelper.sendEmail(context, it) }
                                },
                                onMarkComplete = { viewModel.markAsContacted(connection) }
                            )
                        }
                    }
                    PullRefreshIndicator(
                        refreshing = isRefreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}
