package com.stolz.connect.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Snooze
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
import coil.compose.AsyncImage
import com.stolz.connect.domain.model.ConnectionMethod
import com.stolz.connect.domain.model.ScheduledConnection
import com.stolz.connect.ui.design.ConnectCard
import com.stolz.connect.ui.theme.ConnectionColors
import com.stolz.connect.ui.theme.Dimensions
import com.stolz.connect.ui.theme.isConnectDarkTheme
import com.stolz.connect.ui.theme.AvatarColors
import com.stolz.connect.util.ContactColorCategory
import com.stolz.connect.util.NameUtils
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
    onMarkComplete: () -> Unit,
    onSnoozeClick: (() -> Unit)? = null
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
    
    // Check if item is snoozed (in upcoming section but was originally past due/today)
    val isSnoozed = !connection.isPastDue && !connection.isDueToday && 
                    connection.nextReminderDate.after(Date())
    
    // For snoozed items, always use green color (they're in upcoming, not overdue)
    val baseColorCategory = TimeFormatter.getLastContactedColorCategory(
        connection.lastContactedDate,
        connection.reminderFrequencyDays
    )
    val colorCategory = if (isSnoozed) ContactColorCategory.GREEN else baseColorCategory
    
    val isDarkTheme = isConnectDarkTheme()
    val colors = remember(colorCategory, isDarkTheme) {
        val indicatorColor = when (colorCategory) {
            ContactColorCategory.GREEN -> ConnectionColors.GreenIndicator
            ContactColorCategory.YELLOW -> ConnectionColors.YellowIndicator
            ContactColorCategory.RED -> ConnectionColors.RedIndicator
        }
        val backgroundColor = when (colorCategory) {
            ContactColorCategory.GREEN -> if (isDarkTheme) ConnectionColors.GreenBackgroundDark else ConnectionColors.GreenBackgroundLight
            ContactColorCategory.YELLOW -> if (isDarkTheme) ConnectionColors.YellowBackgroundDark else ConnectionColors.YellowBackgroundLight
            ContactColorCategory.RED -> if (isDarkTheme) ConnectionColors.RedBackgroundDark else ConnectionColors.RedBackgroundLight
        }
        val outlineColor = if (isDarkTheme) {
            when (colorCategory) {
                ContactColorCategory.GREEN -> ConnectionColors.GreenOutlineDark
                ContactColorCategory.YELLOW -> ConnectionColors.YellowOutlineDark
                ContactColorCategory.RED -> ConnectionColors.RedOutlineDark
            }
        } else {
            indicatorColor.copy(alpha = 0.6f)
        }
        Colors(indicatorColor, backgroundColor, outlineColor)
    }
    
    val relativeTimeText = remember(refreshTrigger, connection.lastContactedDate) {
        connection.lastContactedDate?.let { TimeFormatter.formatRelativeTime(it) }
    }

    val actionTint = if (isDarkTheme) ConnectionColors.OnCardDark else MaterialTheme.colorScheme.primary

    ConnectCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(animationState.itemAlpha)
            .offset { IntOffset(animationState.itemOffsetX.toInt(), 0) }
            .clickable(onClick = onClick),
        onClick = null,
        containerColor = colors.backgroundColor,
        contentColor = if (isDarkTheme) ConnectionColors.OnCardDark else MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = Dimensions.xxxsmall,
            color = colors.outlineColor
        ),
        leadingBarColor = colors.indicatorColor
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                    colorCategory = colorCategory,
                    refreshTrigger = refreshTrigger,
                    relativeTimeText = relativeTimeText,
                    indicatorColor = colors.indicatorColor,
                    isSnoozed = isSnoozed,
                    modifier = Modifier.weight(1f)
                )
            }

            // Actions in a dedicated row so they don't overlap the name
            Spacer(modifier = Modifier.height(Dimensions.small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onSnoozeClick != null) {
                    IconButton(
                        onClick = onSnoozeClick,
                        modifier = Modifier.size(Dimensions.iconButtonSize),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = actionTint)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Snooze,
                            contentDescription = "Snooze",
                            tint = actionTint
                        )
                    }
                }
                IconButton(
                    onClick = animationState.handleMarkComplete,
                    modifier = Modifier
                        .size(Dimensions.iconButtonSize)
                        .scale(animationState.checkmarkScaleAnim),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (animationState.checkmarkState == 2) animationState.checkmarkColor else actionTint
                    )
                ) {
                    Icon(
                        imageVector = if (animationState.checkmarkState == 2) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Outlined.CheckCircle
                        },
                        contentDescription = "Mark as Contacted",
                        tint = if (animationState.checkmarkState == 2) animationState.checkmarkColor else (if (isDarkTheme) ConnectionColors.OnCardDark else Color.Gray)
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
                .size(Dimensions.avatarMedium)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(Dimensions.small))
    } else {
        val avatarColor = if (connection.avatarColor != null) {
            Color(connection.avatarColor)
        } else {
            AvatarColors.getColorForName(connection.contactName)
        }
        val initials = NameUtils.getInitials(connection.contactName)
        
        Surface(
            modifier = Modifier.size(Dimensions.avatarMedium),
            shape = MaterialTheme.shapes.medium,
            color = avatarColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
    colorCategory: ContactColorCategory,
    refreshTrigger: Int,
    relativeTimeText: String?,
    indicatorColor: Color,
    isSnoozed: Boolean,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isConnectDarkTheme()
    val textColor = if (isDarkTheme) ConnectionColors.OnCardDark else MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier.padding(start = Dimensions.small)) {
        Text(
            text = connection.contactName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Spacer(modifier = Modifier.height(Dimensions.dataRowSpacing))
        
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
                actions = phoneActions,
                colorCategory = colorCategory
            )
        }
        
        if (connection.contactEmail != null) {
            if (connection.contactPhoneNumber != null) {
                Spacer(modifier = Modifier.height(Dimensions.dataRowSpacing))
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
                ),
                colorCategory = colorCategory
            )
        }
        
        if (connection.birthday != null) {
            if (connection.contactPhoneNumber != null || connection.contactEmail != null) {
                Spacer(modifier = Modifier.height(Dimensions.dataRowSpacing))
            }
            // Check if today is the birthday
            val isBirthdayToday = remember(connection.birthday, refreshTrigger) {
                if (connection.birthday == null) false else {
                    val today = Date()
                    val todayCalendar = Calendar.getInstance().apply {
                        time = today
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val birthdayCalendar = Calendar.getInstance().apply {
                        time = connection.birthday
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    todayCalendar.get(Calendar.DAY_OF_MONTH) == birthdayCalendar.get(Calendar.DAY_OF_MONTH) &&
                    todayCalendar.get(Calendar.MONTH) == birthdayCalendar.get(Calendar.MONTH)
                }
            }
            
            if (isBirthdayToday) {
                DataRow(
                    label = "Birthday",
                    value = "🎉 Today!",
                    colorCategory = colorCategory
                )
            } else {
                DataRow(
                    label = "Birthday",
                    value = dateFormat.format(connection.birthday),
                    colorCategory = colorCategory
                )
            }
        }
        
        if (connection.notes != null && connection.notes.isNotBlank()) {
            if (connection.contactPhoneNumber != null || connection.contactEmail != null || connection.birthday != null) {
                Spacer(modifier = Modifier.height(Dimensions.dataRowSpacing))
            }
            DataRow(
                label = "Notes",
                value = connection.notes,
                colorCategory = colorCategory
            )
        }

        // Next / Last aligned with other rows (same indented column)
        Spacer(modifier = Modifier.height(Dimensions.dataRowSpacing))
        ConnectionItemMetadata(
            connection = connection,
            dateFormat = dateFormat,
            relativeTimeText = relativeTimeText,
            indicatorColor = indicatorColor,
            colorCategory = colorCategory,
            isSnoozed = isSnoozed
        )
    }
}

@Composable
private fun ConnectionItemMetadata(
    connection: ScheduledConnection,
    dateFormat: SimpleDateFormat,
    relativeTimeText: String?,
    indicatorColor: Color,
    colorCategory: ContactColorCategory,
    isSnoozed: Boolean
) {
    val isDarkTheme = isConnectDarkTheme()
    val labelColor = if (isDarkTheme) ConnectionColors.OnCardDark.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
    val valueColor = if (isDarkTheme) ConnectionColors.OnCardDark else MaterialTheme.colorScheme.onSurface
    val errorTextColor = if (isDarkTheme) Color(0xFFB71C1C) else MaterialTheme.colorScheme.error

    // Same label/value structure as DataRow so Next and Last line up with Phone, Email, etc.
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.dataRowSpacing)) {
        Column {
            Text(
                text = "Next reminder",
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(Dimensions.dataRowLabelToValue))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.xxsmall)
            ) {
                Text(
                    text = dateFormat.format(connection.nextReminderDate),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = valueColor
                )
                if (isSnoozed) {
                    Icon(
                        imageVector = Icons.Outlined.Snooze,
                        contentDescription = "Snoozed",
                        tint = labelColor,
                        modifier = Modifier.size(Dimensions.iconSmall)
                    )
                }
            }
        }
        Column {
            Text(
                text = "Last contacted",
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(Dimensions.dataRowLabelToValue))
            Text(
                text = if (connection.lastContactedDate != null) (relativeTimeText ?: "") else "Never contacted",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (connection.lastContactedDate != null) indicatorColor else errorTextColor,
                fontStyle = if (connection.lastContactedDate != null) androidx.compose.ui.text.font.FontStyle.Normal else androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

private data class Colors(
    val indicatorColor: Color,
    val backgroundColor: Color,
    val outlineColor: Color
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
        onMarkComplete = {},
        onSnoozeClick = null
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
        onMarkComplete = {},
        onSnoozeClick = null
    )
}
