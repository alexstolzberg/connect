package com.stolz.connect.ui.theme

import androidx.compose.ui.graphics.Color

// Modern color scheme for Connect app
val ConnectPrimary = Color(0xFF6366F1) // Indigo
val ConnectSecondary = Color(0xFF8B5CF6) // Purple
val ConnectTertiary = Color(0xFFEC4899) // Pink

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Connection status colors
object ConnectionColors {
    // Indicator colors (same for light and dark)
    val GreenIndicator = Color(0xFF4CAF50)
    val YellowIndicator = Color(0xFFFFC107)
    val RedIndicator = Color(0xFFF44336)
    
    // Background colors - Light theme
    val GreenBackgroundLight = Color(0xFFE8F5E9) // Light green
    val YellowBackgroundLight = Color(0xFFFFF8E1) // Muted pastel yellow
    val RedBackgroundLight = Color(0xFFFFEBEE) // Light red
    
    // Background colors - Dark theme
    val GreenBackgroundDark = Color(0xFF0C3301) // Very dark green
    val YellowBackgroundDark = Color(0xFFD4D272) // Light yellow background
    val RedBackgroundDark = Color(0xFFD18A82) // Light red background
    
    // Outline colors for dark theme (lighter than background)
    val GreenOutlineDark = Color(0xFF4CAF50) // Lighter green outline
    val YellowOutlineDark = Color(0xFFD4D272) // Light yellow outline
    val RedOutlineDark = Color(0xFFD18A82) // Light red outline
}