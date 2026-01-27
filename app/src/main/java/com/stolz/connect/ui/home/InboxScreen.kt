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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import com.stolz.connect.ui.home.SnoozeBottomSheet
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stolz.connect.ui.theme.Dimensions
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material.ExperimentalMaterialApi
import com.stolz.connect.domain.model.ScheduledConnection
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.stolz.connect.platform.ContactHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun InboxScreen(
    onAddClick: () -> Unit,
    onConnectionClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    var searchActive by remember { mutableStateOf(false) }
    var showSnoozeSheet by remember { mutableStateOf<ScheduledConnection?>(null) }
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
                title = { 
                    if (searchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    } else {
                        Text("Inbox")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        if (searchActive) {
                            viewModel.setSearchQuery("")
                        }
                        searchActive = !searchActive 
                    }) {
                        Icon(
                            imageVector = if (searchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (searchActive) "Close Search" else "Search"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val hasConnections = uiState.inboxSections.any { it.connections.isNotEmpty() }
            
            if (!hasConnections) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimensions.xlarge),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "No connections found matching \"$searchQuery\"."
                        } else {
                            "No connections due in the next week.\nTap the + button to add one."
                        },
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
                            top = paddingValues.calculateTopPadding() + Dimensions.medium,
                            start = Dimensions.medium,
                            end = Dimensions.medium,
                            bottom = Dimensions.medium
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.xsmall)
                    ) {
                        uiState.inboxSections.forEachIndexed { sectionIndex, section ->
                            item(key = "header_${section.title}") {
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        top = if (sectionIndex == 0) 0.dp else Dimensions.large,
                                        bottom = Dimensions.xsmall
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            items(
                                items = section.connections,
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
                                    onMarkComplete = { viewModel.markAsContacted(connection) },
                                    onSnoozeClick = if (connection.isPastDue || connection.isDueToday) {
                                        { showSnoozeSheet = connection }
                                    } else null
                                )
                            }
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
        
        // Snooze bottom sheet
        showSnoozeSheet?.let { connection ->
            SnoozeBottomSheet(
                onDismiss = { showSnoozeSheet = null },
                onSnoozeSelected = { snoozeDate ->
                    viewModel.snoozeReminder(connection, snoozeDate)
                    showSnoozeSheet = null
                }
            )
        }
    }
}
