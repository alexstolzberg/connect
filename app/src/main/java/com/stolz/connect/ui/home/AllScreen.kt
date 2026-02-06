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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import com.stolz.connect.data.preferences.AllSortOrder
import com.stolz.connect.ui.design.ConnectPrimaryButton
import com.stolz.connect.ui.design.EmptyState
import com.stolz.connect.ui.theme.Dimensions
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
fun AllScreen(
    onAddClick: () -> Unit,
    onConnectionClick: (Long) -> Unit,
    onShowSnackbar: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val allSortOrder by viewModel.allSortOrder.collectAsState()
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var searchActive by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
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
                        LaunchedEffect(Unit) {
                            searchFocusRequester.requestFocus()
                            keyboardController?.show()
                        }
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(searchFocusRequester),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    } else {
                        Text("All Connections")
                    }
                },
                actions = {
                    IconButton(onClick = { sortMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort by"
                        )
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("A–Z") },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setAllSortOrder(AllSortOrder.A_Z)
                                sortMenuExpanded = false
                            },
                            leadingIcon = {
                                if (allSortOrder == AllSortOrder.A_Z) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(Dimensions.iconSmall)
                                    )
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Date (soonest first)") },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setAllSortOrder(AllSortOrder.DATE_ASCENDING)
                                sortMenuExpanded = false
                            },
                            leadingIcon = {
                                if (allSortOrder == AllSortOrder.DATE_ASCENDING) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(Dimensions.iconSmall)
                                    )
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Date (latest first)") },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setAllSortOrder(AllSortOrder.DATE_DESCENDING)
                                sortMenuExpanded = false
                            },
                            leadingIcon = {
                                if (allSortOrder == AllSortOrder.DATE_DESCENDING) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(Dimensions.iconSmall)
                                    )
                                }
                            }
                        )
                    }
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
            if (uiState.allConnections.isEmpty()) {
                EmptyState(
                    message = if (searchQuery.isNotBlank()) {
                        "No connections found matching \"$searchQuery\"."
                    } else {
                        "No connections yet."
                    },
                    action = if (searchQuery.isBlank()) {
                        {
                            ConnectPrimaryButton(
                                onClick = onAddClick,
                                leadingIcon = Icons.Default.Add
                            ) {
                                Text("Add Connection")
                            }
                        }
                    } else null
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = paddingValues.calculateTopPadding() + Dimensions.screenPaddingVertical,
                            start = Dimensions.screenPaddingHorizontal,
                            end = Dimensions.screenPaddingHorizontal,
                            bottom = paddingValues.calculateBottomPadding() + Dimensions.screenPaddingVertical + Dimensions.bottomBarHeight
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.listItemSpacing)
                    ) {
                        items(
                            items = uiState.allConnections,
                            key = { it.id }
                        ) { connection ->
                            ConnectionItem(
                                connection = connection,
                                isHighlighted = connection.isDueToday,
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
                                onMarkComplete = {
                                    viewModel.markAsContacted(connection)
                                    onShowSnackbar("Marked as contacted")
                                },
                                onSnoozeClick = null // Snooze only available in Inbox view
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
