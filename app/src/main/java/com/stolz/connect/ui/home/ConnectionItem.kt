package com.stolz.connect.ui.home

import android.provider.CalendarContract
import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.stolz.connect.domain.model.ConnectionMethod
import com.stolz.connect.domain.model.ScheduledConnection
import com.stolz.connect.ui.theme.ConnectionColors
import com.stolz.connect.ui.theme.Dimensions
import com.stolz.connect.util.ContactColorCategory
import com.stolz.connect.util.PhoneNumberFormatter
import com.stolz.connect.util.TimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * State holder for ConnectionItem animations
 */
@Composable
private fun rememberConnectionItemAnimationState(
    onMarkComplete: () -> Unit
): ConnectionItemAnimationState {
    var isRemoving by remember { mutableStateOf(false) }
    var checkmarkState by remember { mutableStateOf(0) } // 0 = hollow grey, 1 = drawing, 2 = filled green
    var checkmarkScale by remember { mutableStateOf(1f) }
    
    val checkmarkScaleAnim = animateFloatAsState(
        targetValue = checkmarkScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkmark_scale"
    )
    
    val checkmarkColor by animateColorAsState(
        targetValue = when (checkmarkState) {
            0 -> Color.Gray
            1 -> Color.Gray
            2 -> ConnectionColors.GreenIndicator
            else -> Color.Gray
        },
        animationSpec = tween(durationMillis = 200),
        label = "checkmark_color"
    )
    
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
    
    val scope = rememberCoroutineScope()
    
    fun handleMarkComplete() {
        scope.launch {
            checkmarkState = 1
            delay(300)
            checkmarkState = 2
            checkmarkScale = 1.2f
            delay(200)
            checkmarkScale = 1f
            delay(150)
            isRemoving = true
            delay(400)
            onMarkComplete()
        }
    }
    
    return ConnectionItemAnimationState(
        itemAlpha = itemAlpha,
        itemOffsetX = itemOffsetX,
        checkmarkScaleAnim = checkmarkScaleAnim.value,
        checkmarkColor = checkmarkColor,
        checkmarkState = checkmarkState,
        handleMarkComplete = ::handleMarkComplete
    )
}

private data class ConnectionItemAnimationState(
    val itemAlpha: Float,
    val itemOffsetX: Float,
    val checkmarkScaleAnim: Float,
    val checkmarkColor: Color,
    val checkmarkState: Int,
    val handleMarkComplete: () -> Unit
)

@Composable
fun ConnectionItem(
    connection: ScheduledConnection,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onCallClick: () -> Unit,
    onMessageClick: () -> Unit,
    onEmailClick: (() -> Unit)? = null,
    onMarkComplete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val animationState = rememberConnectionItemAnimationState(onMarkComplete)
    
    // Auto-refresh timer for relative time display
    var refreshTrigger by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            refreshTrigger++
        }
    }
    
    val colorCategory = TimeFormatter.getLastContactedColorCategory(
        connection.lastContactedDate,
        connection.reminderFrequencyDays
    )
    
    val colors = remember(colorCategory) {
        val indicatorColor = when (colorCategory) {
            ContactColorCategory.GREEN -> ConnectionColors.GreenIndicator
            ContactColorCategory.YELLOW -> ConnectionColors.YellowIndicator
            ContactColorCategory.RED -> ConnectionColors.RedIndicator
        }
        val backgroundColor = when (colorCategory) {
            ContactColorCategory.GREEN -> ConnectionColors.GreenBackground
            ContactColorCategory.YELLOW -> ConnectionColors.YellowBackground
            ContactColorCategory.RED -> ConnectionColors.RedBackground
        }
        Colors(indicatorColor, backgroundColor)
    }
    
    val relativeTimeText = remember(refreshTrigger, connection.lastContactedDate) {
        connection.lastContactedDate?.let { TimeFormatter.formatRelativeTime(it) }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(animationState.itemAlpha)
            .offset { IntOffset(animationState.itemOffsetX.toInt(), 0) }
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = colors.backgroundColor
        ),
        border = BorderStroke(
            width = 2.dp,
            color = colors.indicatorColor.copy(alpha = 0.6f)
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Dimensions.medium)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    ConnectionItemAvatar(connection)
                    
                    ConnectionItemContent(
                        connection = connection,
                        dateFormat = dateFormat,
                        onCallClick = onCallClick,
                        onMessageClick = onMessageClick,
                        onEmailClick = onEmailClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                ConnectionItemMetadata(
                    connection = connection,
                    dateFormat = dateFormat,
                    relativeTimeText = relativeTimeText,
                    indicatorColor = colors.indicatorColor
                )
            }
            
            if (connection.isDueToday) {
                IconButton(
                    onClick = animationState.handleMarkComplete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(40.dp)
                        .scale(animationState.checkmarkScaleAnim)
                ) {
                    Icon(
                        imageVector = if (animationState.checkmarkState == 2) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Outlined.CheckCircle
                        },
                        contentDescription = "Mark as Contacted",
                        tint = animationState.checkmarkColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionItemAvatar(connection: ScheduledConnection) {
    if (connection.contactPhotoUri != null) {
        AsyncImage(
            model = connection.contactPhotoUri,
            contentDescription = connection.contactName,
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(Dimensions.small))
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
                    modifier = Modifier.size(Dimensions.large)
                )
            }
        }
        Spacer(modifier = Modifier.width(Dimensions.small))
    }
}

@Composable
private fun ConnectionItemContent(
    connection: ScheduledConnection,
    dateFormat: SimpleDateFormat,
    onCallClick: () -> Unit,
    onMessageClick: () -> Unit,
    onEmailClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(start = Dimensions.small)) {
        Text(
            text = connection.contactName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        if (connection.contactPhoneNumber != null) {
            val phoneActions = mutableListOf<DataRowAction>()
            if (connection.preferredMethod == ConnectionMethod.CALL ||
                connection.preferredMethod == ConnectionMethod.BOTH
            ) {
                phoneActions.add(
                    DataRowAction(
                        icon = Icons.Default.Phone,
                        contentDescription = "Call",
                        onClick = onCallClick
                    )
                )
            }
            if (connection.preferredMethod == ConnectionMethod.MESSAGE ||
                connection.preferredMethod == ConnectionMethod.BOTH
            ) {
                phoneActions.add(
                    DataRowAction(
                        icon = Icons.Default.Send,
                        contentDescription = "Message",
                        onClick = onMessageClick
                    )
                )
            }
            
            DataRow(
                label = "Phone",
                value = PhoneNumberFormatter.format(connection.contactPhoneNumber),
                actions = phoneActions
            )
        }
        
        if (connection.contactEmail != null) {
            if (connection.contactPhoneNumber != null) {
                Spacer(modifier = Modifier.height(3.dp))
            }
            DataRow(
                label = "Email",
                value = connection.contactEmail,
                actions = listOf(
                    DataRowAction(
                        icon = Icons.Default.Email,
                        contentDescription = "Email",
                        onClick = onEmailClick ?: {}
                    )
                )
            )
        }
        
        if (connection.birthday != null) {
            if (connection.contactPhoneNumber != null || connection.contactEmail != null) {
                Spacer(modifier = Modifier.height(3.dp))
            }
            DataRow(
                label = "Birthday",
                value = dateFormat.format(connection.birthday)
            )
        }
    }
}

@Composable
private fun ConnectionItemMetadata(
    connection: ScheduledConnection,
    dateFormat: SimpleDateFormat,
    relativeTimeText: String?,
    indicatorColor: Color
) {
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
}

private data class Colors(
    val indicatorColor: Color,
    val backgroundColor: Color
)

@Preview(showBackground = true, name = "Connection Item - Due Today")
@Composable
fun ConnectionItemPreview() {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2024)
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 15)
    }
    val nextReminderDate = calendar.time
    
    val lastContactedCalendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, 2024)
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 10)
    }
    val lastContactedDate = lastContactedCalendar.time
    
    val birthdayCalendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, 1990)
        set(Calendar.MONTH, Calendar.MARCH)
        set(Calendar.DAY_OF_MONTH, 20)
    }
    val birthday = birthdayCalendar.time
    
    val sampleConnection = ScheduledConnection(
        id = 1,
        contactName = "John Doe",
        contactPhoneNumber = "5551234567",
        contactEmail = "john.doe@example.com",
        contactPhotoUri = null,
        contactId = null,
        reminderFrequencyDays = 7,
        preferredMethod = ConnectionMethod.BOTH,
        reminderTime = null,
        lastContactedDate = lastContactedDate,
        nextReminderDate = nextReminderDate,
        notes = "Great friend from college",
        birthday = birthday,
        promptOnBirthday = true,
        createdAt = Date(),
        isActive = true
    )
    
    ConnectionItem(
        connection = sampleConnection,
        isHighlighted = true,
        onClick = {},
        onCallClick = {},
        onMessageClick = {},
        onEmailClick = {},
        onMarkComplete = {}
    )
}

@Preview(showBackground = true, name = "Connection Item - Overdue")
@Composable
fun ConnectionItemOverduePreview() {
    val calendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_MONTH, -10)
    }
    val nextReminderDate = calendar.time
    
    val sampleConnection = ScheduledConnection(
        id = 2,
        contactName = "Jane Smith",
        contactPhoneNumber = "5559876543",
        contactEmail = null,
        contactPhotoUri = null,
        contactId = null,
        reminderFrequencyDays = 7,
        preferredMethod = ConnectionMethod.CALL,
        reminderTime = null,
        lastContactedDate = null,
        nextReminderDate = nextReminderDate,
        notes = null,
        birthday = null,
        promptOnBirthday = true,
        createdAt = Date(),
        isActive = true
    )
    
    ConnectionItem(
        connection = sampleConnection,
        isHighlighted = false,
        onClick = {},
        onCallClick = {},
        onMessageClick = {},
        onEmailClick = null,
        onMarkComplete = {}
    )
}
