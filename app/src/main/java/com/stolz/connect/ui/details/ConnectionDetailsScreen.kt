package com.stolz.connect.ui.details

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import com.stolz.connect.platform.ContactHelper
import com.stolz.connect.util.ContactColorCategory
import com.stolz.connect.util.StringUtils
import com.stolz.connect.util.TimeFormatter
import com.stolz.connect.ui.theme.Dimensions
import com.stolz.connect.util.PhoneNumberFormatter
import kotlinx.coroutines.delay
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
                
                // Auto-refresh timer for relative time display (updates every minute)
                var refreshTrigger by remember { mutableStateOf(0) }
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(60_000) // 60 seconds = 1 minute
                        refreshTrigger++
                    }
                }
                
                // Check if item is snoozed
                val isSnoozed = !connection.isPastDue && !connection.isDueToday && 
                                connection.nextReminderDate.after(Date())
                
                // Get color category for visual indicator
                // For snoozed items, always use green color (they're in upcoming, not overdue)
                val baseColorCategory = TimeFormatter.getLastContactedColorCategory(
                    connection.lastContactedDate,
                    connection.reminderFrequencyDays
                )
                val colorCategory = if (isSnoozed) ContactColorCategory.GREEN else baseColorCategory
                
                val indicatorColor = when (colorCategory) {
                    ContactColorCategory.GREEN -> Color(0xFF4CAF50)
                    ContactColorCategory.YELLOW -> Color(0xFFFFC107)
                    ContactColorCategory.RED -> Color(0xFFF44336)
                }
                
                val backgroundColor = when (colorCategory) {
                    ContactColorCategory.GREEN -> Color(0xFFE8F5E9) // Light green
                    ContactColorCategory.YELLOW -> Color(0xFFFFF9C4) // Light yellow
                    ContactColorCategory.RED -> Color(0xFFFFEBEE) // Light red
                }
                
                // Use refreshTrigger to force recomposition and recalculate relative time
                val relativeTimeText = remember(refreshTrigger, connection.lastContactedDate) {
                    if (connection.lastContactedDate != null) {
                        TimeFormatter.formatRelativeTime(connection.lastContactedDate)
                    } else {
                        null
                    }
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(Dimensions.medium),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.medium)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = backgroundColor
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 3.dp,
                            color = indicatorColor.copy(alpha = 0.7f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimensions.medium),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.small)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = connection.contactName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                // Show connection type badge
                                if (connection.contactId != null) {
                                    val canOpenContact = remember(connection.contactId) {
                                        ContactHelper.canOpenContactInPhone(context, connection.contactId)
                                    }
                                    Surface(
                                        color = if (canOpenContact) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        shape = MaterialTheme.shapes.small,
                                        modifier = if (canOpenContact) {
                                            Modifier.clickable {
                                                try {
                                                    ContactHelper.openContactInPhone(context, connection.contactId)
                                                } catch (e: Exception) {
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Unable to open contact",
                                                        android.widget.Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } else {
                                            Modifier
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = Dimensions.xsmall, vertical = Dimensions.xxsmall),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(Dimensions.xxsmall)
                                        ) {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = null,
                                                modifier = Modifier.size(Dimensions.medium),
                                                tint = if (canOpenContact) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                            Text(
                                                text = "View Contact",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (canOpenContact) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = "Manual Input",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = Dimensions.xsmall, vertical = Dimensions.xxsmall)
                                        )
                                    }
                                }
                            }
                            if (connection.contactPhoneNumber != null) {
                                Text(
                                    text = "Phone",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = PhoneNumberFormatter.format(connection.contactPhoneNumber),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (connection.contactEmail != null) {
                                Text(
                                    text = "Email",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = connection.contactEmail,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Divider()
                            
                            InfoRow(
                                "Frequency", 
                                StringUtils.pluralize(connection.reminderFrequencyDays, "day", "days")
                            )
                            InfoRow(
                                "Method", 
                                when (connection.preferredMethod) {
                                    com.stolz.connect.domain.model.ConnectionMethod.CALL -> "Call"
                                    com.stolz.connect.domain.model.ConnectionMethod.MESSAGE -> "Message"
                                    com.stolz.connect.domain.model.ConnectionMethod.EMAIL -> "Email"
                                    com.stolz.connect.domain.model.ConnectionMethod.BOTH -> "No Preference"
                                }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Next Reminder",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(Dimensions.xxsmall),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dateFormat.format(connection.nextReminderDate),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSnoozed) {
                                        Icon(
                                            imageVector = Icons.Outlined.Snooze,
                                            contentDescription = "Snoozed",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(Dimensions.medium)
                                        )
                                    }
                                }
                            }
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
                                        horizontalArrangement = Arrangement.spacedBy(Dimensions.xsmall),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = relativeTimeText ?: "",
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
                    
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.xsmall)
                    ) {
                        if (connection.contactPhoneNumber != null &&
                            (connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.CALL ||
                             connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.BOTH)
                        ) {
                            Button(
                                onClick = {
                                    handleCallClick(connection.contactPhoneNumber)
                                }
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null)
                                Spacer(modifier = Modifier.width(Dimensions.xsmall))
                                Text("Call")
                            }
                        }
                        if (connection.contactPhoneNumber != null &&
                            (connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.MESSAGE ||
                             connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.BOTH)
                        ) {
                            Button(
                                onClick = {
                                    ContactHelper.sendMessage(context, connection.contactPhoneNumber)
                                }
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(Dimensions.xsmall))
                                Text("Message")
                            }
                        }
                        if (connection.contactEmail != null &&
                            (connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.EMAIL ||
                             connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.BOTH)
                        ) {
                            Button(
                                onClick = {
                                    ContactHelper.sendEmail(context, connection.contactEmail)
                                }
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null)
                                Spacer(modifier = Modifier.width(Dimensions.xsmall))
                                Text("Email")
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
