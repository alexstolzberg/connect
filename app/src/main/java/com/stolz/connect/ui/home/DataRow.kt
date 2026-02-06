package com.stolz.connect.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.stolz.connect.ui.theme.ConnectionColors
import com.stolz.connect.ui.theme.Dimensions
import com.stolz.connect.ui.theme.isConnectDarkTheme
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
    val isDarkTheme = isConnectDarkTheme()
    val labelColor = if (isDarkTheme) ConnectionColors.OnCardDark.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
    val valueColor = if (isDarkTheme) ConnectionColors.OnCardDark else MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(Dimensions.dataRowLabelToValue))
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
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.xxsmall)
                ) {
                    actions.forEach { action ->
                        IconButton(
                            onClick = action.onClick,
                            modifier = Modifier.size(Dimensions.iconExtraLarge)
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.contentDescription,
                                tint = if (colorCategory != null) valueColor else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(Dimensions.iconSmall)
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
                icon = Icons.AutoMirrored.Filled.Send,
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
