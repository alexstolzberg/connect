package com.stolz.connect.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.stolz.connect.ui.theme.Dimensions
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozeBottomSheet(
    onDismiss: () -> Unit,
    onSnoozeSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.medium)
        ) {
            Text(
                text = "Snooze Reminder",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Dimensions.medium)
            )
            
            // Quick options
            SnoozeOption(
                label = "1 day",
                onClick = {
                    val calendar = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, 1)
                    }
                    onSnoozeSelected(calendar.time)
                    onDismiss()
                }
            )
            
            SnoozeOption(
                label = "2 days",
                onClick = {
                    val calendar = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, 2)
                    }
                    onSnoozeSelected(calendar.time)
                    onDismiss()
                }
            )
            
            SnoozeOption(
                label = "1 week",
                onClick = {
                    val calendar = Calendar.getInstance().apply {
                        add(Calendar.WEEK_OF_YEAR, 1)
                    }
                    onSnoozeSelected(calendar.time)
                    onDismiss()
                }
            )
            
            SnoozeOption(
                label = "1 month",
                onClick = {
                    val calendar = Calendar.getInstance().apply {
                        add(Calendar.MONTH, 1)
                    }
                    onSnoozeSelected(calendar.time)
                    onDismiss()
                }
            )
            
            Divider(modifier = Modifier.padding(vertical = Dimensions.small))
            
            // Custom date picker option
            SnoozeOption(
                label = "Pick a date",
                icon = Icons.Default.DateRange,
                onClick = { showDatePicker = true }
            )
            
            Spacer(modifier = Modifier.height(Dimensions.medium))
        }
    }
    
    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = calendar.timeInMillis
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Normalize to midnight in local timezone
                            val selectedCalendar = Calendar.getInstance().apply {
                                timeInMillis = millis
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onSnoozeSelected(selectedCalendar.time)
                            onDismiss()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SnoozeOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimensions.medium, horizontal = Dimensions.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.iconSmall),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(Dimensions.small))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
