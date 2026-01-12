package com.stolz.connect.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Connect") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // App Icon/Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Connect",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Divider()
            
            // Purpose Section
            Text(
                text = "Purpose",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Connect helps you maintain meaningful relationships by reminding you to reach out to the people who matter most. Life gets busy, and it's easy to lose touch with friends, family, and colleagues. This app ensures you never forget to stay connected.",
                style = MaterialTheme.typography.bodyLarge
            )
            
            // How to Use Section
            Text(
                text = "How to Use",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HowToStep(
                    number = "1",
                    title = "Add Connections",
                    description = "Tap the + button to add a new connection. Pick a contact from your device or add one manually. Set how often you want to be reminded to reach out."
                )
                HowToStep(
                    number = "2",
                    title = "View Your Reminders",
                    description = "Check the 'Today' tab to see connections due today, or the 'All' tab to see all your scheduled connections. Connections are color-coded: green (recent), yellow (medium), red (long time since contact)."
                )
                HowToStep(
                    number = "3",
                    title = "Stay Connected",
                    description = "When it's time to reach out, tap the checkmark to mark as contacted, or use the call/message buttons to contact them directly. The app will automatically schedule your next reminder."
                )
                HowToStep(
                    number = "4",
                    title = "Manage Connections",
                    description = "Tap any connection to view details, edit settings, or delete if needed. You can also add notes and birthdays for each connection."
                )
            }
            
            Divider()
            
            // Features Section
            Text(
                text = "Features",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            FeatureItem("📅 Flexible Reminders", "Set daily, weekly, monthly, or custom reminder frequencies")
            FeatureItem("📞 Multiple Contact Methods", "Call, message, or email your contacts directly from the app")
            FeatureItem("🎨 Visual Indicators", "Color-coded contacts help you see who needs attention")
            FeatureItem("📝 Notes & Birthdays", "Keep track of important information and special dates")
            FeatureItem("🔄 Automatic Scheduling", "The app automatically calculates your next reminder date")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Stay connected with the people who matter most.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun HowToStep(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FeatureItem(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
