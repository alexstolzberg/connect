package com.stolz.connect.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.stolz.connect.ui.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Connect") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val context = LocalContext.current
        val versionName = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        } catch (_: Exception) {
            ""
        }
        // Only use bottom padding to avoid double top padding
        val contentPadding = PaddingValues(
            top = Dimensions.spacingNone,
            bottom = paddingValues.calculateBottomPadding(),
            start = Dimensions.spacingNone,
            end = Dimensions.spacingNone
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(Dimensions.large)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimensions.large)
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
                    modifier = Modifier.size(Dimensions.xxxlarge),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(Dimensions.medium))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Connect",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version $versionName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            HorizontalDivider()
            
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
                verticalArrangement = Arrangement.spacedBy(Dimensions.medium)
            ) {
                HowToStep(
                    number = "1",
                    title = "Add Connections",
                    description = "Tap the + button to add a new connection. Pick a contact from your device or add one manually. Set how often you want to be reminded to reach out."
                )
                HowToStep(
                    number = "2",
                    title = "View Your Reminders",
                    description = "Check the 'Today' tab to see connections due today, or the 'All' tab to see all your scheduled connections. Connections are color-coded based on how recently you've contacted them (see Color Coding section below)."
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
            
            HorizontalDivider()
            
            // Color Coding Section
            Text(
                text = "Color Coding",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Connections are color-coded to help you quickly see who needs attention:",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(Dimensions.xsmall))
            ColorCodingRule(
                colorName = "Green",
                color = Color(0xFF4CAF50),
                description = "Contacted within your reminder frequency (e.g., within 7 days for weekly reminders)"
            )
            ColorCodingRule(
                colorName = "Yellow",
                color = Color(0xFFFFC107),
                description = "Overdue by up to one reminder period (e.g., 7-14 days for weekly reminders)"
            )
            ColorCodingRule(
                colorName = "Red",
                color = Color(0xFFF44336),
                description = "Overdue by more than one reminder period (e.g., more than 14 days for weekly reminders) or never contacted"
            )
            
            HorizontalDivider()
            
            // Features Section
            Text(
                text = "Features",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            FeatureItem("Flexible Reminders", "Set daily, weekly, monthly, or custom reminder frequencies")
            FeatureItem("Multiple Contact Methods", "Call, message, or email your contacts directly from the app")
            FeatureItem("Visual Indicators", "Color-coded contacts help you see who needs attention")
            FeatureItem("Notes & Birthdays", "Keep track of important information and special dates")
            FeatureItem("Automatic Scheduling", "The app automatically calculates your next reminder date")
            
            Spacer(modifier = Modifier.height(Dimensions.medium))
            
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
        horizontalArrangement = Arrangement.spacedBy(Dimensions.medium)
    ) {
        Surface(
            modifier = Modifier.size(Dimensions.avatarMedium),
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
            verticalArrangement = Arrangement.spacedBy(Dimensions.xxsmall)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.xsmall),
        verticalArrangement = Arrangement.spacedBy(Dimensions.xxsmall)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ColorCodingRule(
    colorName: String,
    color: Color,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.xsmall),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(Dimensions.large),
            shape = MaterialTheme.shapes.small,
            color = color
        ) {}
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = colorName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
