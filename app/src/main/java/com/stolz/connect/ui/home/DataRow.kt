package com.stolz.connect.ui.home

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.stolz.connect.util.ContactColorCategory

data class DataRowAction(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)

@Composable
fun DataRow(
    label: String,
    value: String,
    actions: List<DataRowAction> = emptyList(),
    colorCategory: ContactColorCategory? = null,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    // In dark mode: green has dark background (use white text), yellow/red have light backgrounds (use dark text)
    val labelColor = if (isDarkTheme && colorCategory != null) {
        when (colorCategory) {
            ContactColorCategory.GREEN -> Color.White.copy(alpha = 0.7f) // Dark green background needs white text
            ContactColorCategory.YELLOW, ContactColorCategory.RED -> Color(0xFF1A1A1A).copy(alpha = 0.7f) // Light backgrounds need dark text
        }
    } else if (isDarkTheme) {
        Color(0xFF1A1A1A).copy(alpha = 0.7f) // Default dark text for dark mode
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val valueColor = if (isDarkTheme && colorCategory != null) {
        when (colorCategory) {
            ContactColorCategory.GREEN -> Color.White // Dark green background needs white text
            ContactColorCategory.YELLOW, ContactColorCategory.RED -> Color(0xFF1A1A1A) // Light backgrounds need dark text
        }
    } else if (isDarkTheme) {
        Color(0xFF1A1A1A) // Default dark text for dark mode
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(1.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = valueColor,
                modifier = Modifier.weight(1f)
            )
            if (actions.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    actions.forEach { action ->
                        IconButton(
                            onClick = action.onClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.contentDescription,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "DataRow - Phone with Actions")
@Composable
private fun DataRowPhonePreview() {
    DataRow(
        label = "Phone",
        value = "555-123-4567",
        actions = listOf(
            DataRowAction(
                icon = Icons.Default.Phone,
                contentDescription = "Call",
                onClick = {}
            ),
            DataRowAction(
                icon = Icons.Default.Send,
                contentDescription = "Message",
                onClick = {}
            )
        )
    )
}

@Preview(showBackground = true, name = "DataRow - Email with Action")
@Composable
private fun DataRowEmailPreview() {
    DataRow(
        label = "Email",
        value = "john.doe@example.com",
        actions = listOf(
            DataRowAction(
                icon = Icons.Default.Email,
                contentDescription = "Email",
                onClick = {}
            )
        )
    )
}

@Preview(showBackground = true, name = "DataRow - No Actions")
@Composable
private fun DataRowNoActionsPreview() {
    DataRow(
        label = "Birthday",
        value = "Jan 15, 2024"
    )
}
