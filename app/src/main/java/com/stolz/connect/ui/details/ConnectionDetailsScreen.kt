package com.stolz.connect.ui.details

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.graphics.Color
import com.stolz.connect.platform.ContactHelper
import com.stolz.connect.util.ContactColorCategory
import com.stolz.connect.util.TimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionDetailsScreen(
    connectionId: Long,
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: ConnectionDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteResult by viewModel.deleteResult.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Refresh when screen resumes (comes back from edit screen)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Refresh connection data when screen resumes
                viewModel.refreshConnection()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
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
    
    LaunchedEffect(deleteResult) {
        if (deleteResult is DeleteResult.Success) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connection Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(connectionId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ConnectionDetailsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ConnectionDetailsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is ConnectionDetailsUiState.Success -> {
                val connection = state.connection
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                
                // Get color category for visual indicator
                val colorCategory = TimeFormatter.getLastContactedColorCategory(
                    connection.lastContactedDate,
                    connection.reminderFrequencyDays
                )
                val indicatorColor = when (colorCategory) {
                    ContactColorCategory.GREEN -> Color(0xFF4CAF50)
                    ContactColorCategory.YELLOW -> Color(0xFFFFC107)
                    ContactColorCategory.RED -> Color(0xFFF44336)
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 3.dp,
                            color = indicatorColor.copy(alpha = 0.7f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = connection.contactName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = connection.contactPhoneNumber,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Divider()
                            
                            InfoRow("Frequency", "${connection.reminderFrequencyDays} days")
                            InfoRow("Method", connection.preferredMethod.name.lowercase().replaceFirstChar { it.uppercaseChar() })
                            InfoRow(
                                "Next Reminder",
                                dateFormat.format(connection.nextReminderDate)
                            )
                            if (connection.lastContactedDate != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Last Contacted",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = TimeFormatter.formatRelativeTime(connection.lastContactedDate),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = indicatorColor
                                        )
                                        Text(
                                            text = "(${dateFormat.format(connection.lastContactedDate)})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                InfoRow(
                                    "Last Contacted",
                                    "Never",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            if (connection.birthday != null) {
                                Divider()
                                InfoRow(
                                    "Birthday",
                                    dateFormat.format(connection.birthday)
                                )
                            }
                            if (connection.notes != null && connection.notes.isNotBlank()) {
                                Divider()
                                Text(
                                    text = "Notes",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = connection.notes,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.CALL ||
                            connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.BOTH
                        ) {
                            Button(
                                onClick = {
                                    handleCallClick(connection.contactPhoneNumber)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Call")
                            }
                        }
                        if (connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.MESSAGE ||
                            connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.BOTH
                        ) {
                            Button(
                                onClick = {
                                    ContactHelper.sendMessage(context, connection.contactPhoneNumber)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Message")
                            }
                        }
                    }
                    
                    if (connection.isDueToday) {
                        Button(
                            onClick = {
                                viewModel.markAsContacted()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text("Mark as Contacted")
                        }
                    }
                    
                    if (deleteResult is DeleteResult.Error) {
                        Text(
                            text = (deleteResult as DeleteResult.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Connection") },
            text = { Text("Are you sure you want to delete this connection?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteConnection()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String, color: Color? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color ?: MaterialTheme.colorScheme.onSurface
        )
    }
}
