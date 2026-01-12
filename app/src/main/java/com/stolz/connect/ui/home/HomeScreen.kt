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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.stolz.connect.platform.ContactHelper
import com.stolz.connect.util.ContactColorCategory
import com.stolz.connect.util.TimeFormatter
import java.text.SimpleDateFormat
import java.util.Locale

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
                        onMarkComplete = { onMarkComplete(connection) }
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
                        onMarkComplete = { onMarkComplete(connection) }
                    )
                }
        }
    }
}

@Composable
fun ConnectionItem(
    connection: com.stolz.connect.domain.model.ScheduledConnection,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onCallClick: () -> Unit,
    onMessageClick: () -> Unit,
    onEmailClick: (() -> Unit)? = null,
    onMarkComplete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    // Get color category based on last contacted date
    val colorCategory = TimeFormatter.getLastContactedColorCategory(
        connection.lastContactedDate,
        connection.reminderFrequencyDays
    )
    
    // Determine border/indicator color
    val indicatorColor = when (colorCategory) {
        ContactColorCategory.GREEN -> Color(0xFF4CAF50) // Green
        ContactColorCategory.YELLOW -> Color(0xFFFFC107) // Yellow/Amber
        ContactColorCategory.RED -> Color(0xFFF44336) // Red
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = indicatorColor.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Profile picture or placeholder
                if (connection.contactPhotoUri != null) {
                    AsyncImage(
                        model = connection.contactPhotoUri,
                        contentDescription = connection.contactName,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = connection.contactName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (connection.contactPhoneNumber != null) {
                        Text(
                            text = connection.contactPhoneNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (connection.contactEmail != null) {
                        if (connection.contactPhoneNumber != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        Text(
                            text = connection.contactEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Next reminder
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Next:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateFormat.format(connection.nextReminderDate),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Last contacted with relative time
                    if (connection.lastContactedDate != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Last:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = TimeFormatter.formatRelativeTime(connection.lastContactedDate),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = indicatorColor
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Never contacted",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
                
                // Action buttons column
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Mark complete button (if due today)
                    if (connection.isDueToday) {
                        IconButton(
                            onClick = onMarkComplete,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Mark as Contacted",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Contact method buttons (only show if available)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Show call button only if phone number exists and method allows it
                        if (connection.contactPhoneNumber != null &&
                            (connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.CALL ||
                             connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.BOTH)
                        ) {
                            IconButton(
                                onClick = onCallClick,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Call",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        // Show message button only if phone number exists and method allows it
                        if (connection.contactPhoneNumber != null &&
                            (connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.MESSAGE ||
                             connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.BOTH)
                        ) {
                            IconButton(
                                onClick = onMessageClick,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Message",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        // Show email button only if email exists and method allows it
                        if (connection.contactEmail != null &&
                            (connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.EMAIL ||
                             connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.BOTH)
                        ) {
                            IconButton(
                                onClick = onEmailClick ?: {},
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
