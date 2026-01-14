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
import androidx.compose.material.icons.outlined.CheckCircle
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
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.stolz.connect.platform.ContactHelper
import com.stolz.connect.util.ContactColorCategory
import com.stolz.connect.util.TimeFormatter
import com.stolz.connect.util.PhoneNumberFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    // Animation state for checkmark and item removal
    var isRemoving by remember { mutableStateOf(false) }
    var checkmarkState by remember { mutableStateOf(0) } // 0 = hollow grey, 1 = drawing, 2 = filled green
    var checkmarkScale by remember { mutableStateOf(1f) }

    // Animate checkmark scale on click
    val checkmarkScaleAnim = animateFloatAsState(
        targetValue = checkmarkScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkmark_scale"
    )

    // Animate checkmark color
    val checkmarkColor by animateColorAsState(
        targetValue = when (checkmarkState) {
            0 -> Color.Gray // Hollow grey
            1 -> Color.Gray // Still grey while drawing
            2 -> Color(0xFF4CAF50) // Green when filled
            else -> Color.Gray
        },
        animationSpec = tween(durationMillis = 200),
        label = "checkmark_color"
    )

    // Animate item removal
    val itemAlpha by animateFloatAsState(
        targetValue = if (isRemoving) 0f else 1f,
        animationSpec = tween(durationMillis = 400),
        label = "item_alpha"
    )

    val itemOffsetX by animateFloatAsState(
        targetValue = if (isRemoving) -1000f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "item_offset"
    )
    
    // Auto-refresh timer for relative time display (updates every minute)
    var refreshTrigger by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000) // 60 seconds = 1 minute
            refreshTrigger++
        }
    }

    val scope = rememberCoroutineScope()

    fun handleMarkComplete() {
        scope.launch {
            // Step 1: Start drawing animation (switch to drawing state)
            checkmarkState = 1
            delay(300) // Wait for drawing animation

            // Step 2: Fill in and change to green
            checkmarkState = 2
            checkmarkScale = 1.2f
            delay(200) // Wait for color change

            // Step 3: Bounce effect
            checkmarkScale = 1f
            delay(150)

            // Step 4: Start removal animation
            isRemoving = true
            delay(400) // Wait for removal animation

            // Step 5: Call the actual mark complete
            onMarkComplete()
        }
    }
    
    // Get color category based on last contacted date
    val colorCategory = TimeFormatter.getLastContactedColorCategory(
        connection.lastContactedDate,
        connection.reminderFrequencyDays
    )
    
    // Determine border/indicator color and background color
    val indicatorColor = when (colorCategory) {
        ContactColorCategory.GREEN -> Color(0xFF4CAF50) // Green
        ContactColorCategory.YELLOW -> Color(0xFFFFC107) // Yellow/Amber
        ContactColorCategory.RED -> Color(0xFFF44336) // Red
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
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(itemAlpha)
            .offset { IntOffset(itemOffsetX.toInt(), 0) }
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = indicatorColor.copy(alpha = 0.6f)
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
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
                                .size(40.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                } else {
                    Surface(
                            modifier = Modifier.size(40.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = connection.contactName,
                                style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (connection.contactPhoneNumber != null) {
                                Column {
                                    Text(
                                        text = "Phone",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                        Text(
                                        text = PhoneNumberFormatter.format(connection.contactPhoneNumber),
                                        style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                                }
                    }
                    if (connection.contactEmail != null) {
                        if (connection.contactPhoneNumber != null) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                }
                                Column {
                                    Text(
                                        text = "Email",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = connection.contactEmail,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        if (connection.birthday != null) {
                            if (connection.contactPhoneNumber != null || connection.contactEmail != null) {
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                            Column {
                                Text(
                                    text = "Birthday",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = dateFormat.format(connection.birthday),
                                    style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                            }
                        }
                        }

                        // Fixed icon column on the right - always in same horizontal position
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            // Phone row icons - aligned with phone number text
                            if (connection.contactPhoneNumber != null) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 19.dp) // Align with phone number text (after header + spacing)
                                ) {
                                    // Show call icon if method allows
                                    if (connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.CALL ||
                                        connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.BOTH
                                    ) {
                                        IconButton(
                                            onClick = onCallClick,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = "Call",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    // Show message icon if method allows
                                    if (connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.MESSAGE ||
                                        connection.preferredMethod == com.stolz.connect.domain.model.ConnectionMethod.BOTH
                                    ) {
                                        IconButton(
                                            onClick = onMessageClick,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "Message",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Email row icon - aligned with email text
                            if (connection.contactEmail != null) {
                                // Calculate padding to align icon with email text
                                // Phone icons are at 19dp (aligned with phone number text)
                                // Email icon should align with email text, which is:
                                // - If phone exists: phone section (header + spacing + text) + gap + email header + spacing
                                // - If no phone: name + spacer + email header + spacing
                                val emailTopPadding = if (connection.contactPhoneNumber != null) {
                                    // Phone icons at 19dp, then add:
                                    // Phone header (~12dp) + spacing (1dp) + phone text (~20dp) + gap (3dp) + email header (~12dp) + spacing (1dp)
                                    // = 19 + 12 + 1 + 20 + 3 + 12 + 1 = 68dp
                                    68.dp
                                } else {
                                    // No phone: same offset as phone icons would be (19dp from name)
                                    19.dp
                                }
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = emailTopPadding)
                                ) {
                                    // Always show email icon when email exists
                                    IconButton(
                                        onClick = onEmailClick ?: {},
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Email",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                    
                    // Next reminder
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Next:",
                        style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateFormat.format(connection.nextReminderDate),
                        style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Last contacted with relative time
                    if (connection.lastContactedDate != null) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Last:",
                            style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = relativeTimeText ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = indicatorColor
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Never contacted",
                        style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                // Birthday (already shown in main content area, so remove duplicate)
            }

            // Mark complete button (if due today) - positioned at top right
                    if (connection.isDueToday) {
                        IconButton(
                    onClick = ::handleMarkComplete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(40.dp)
                        .scale(checkmarkScaleAnim.value)
                        ) {
                            Icon(
                        imageVector = if (checkmarkState == 2) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = "Mark as Contacted",
                        tint = checkmarkColor
                    )
                }
            }
        }
    }
}
