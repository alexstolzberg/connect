package com.stolz.connect.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.stolz.connect.data.preferences.ThemeMode

/** Whether the current theme is dark (system or app setting). Use this instead of luminance/brightness. */
val LocalConnectDarkTheme = compositionLocalOf { false }

@Composable
fun isConnectDarkTheme(): Boolean = LocalConnectDarkTheme.current

private val DarkColorScheme = darkColorScheme(
    primary = ConnectPrimary, // Same blue as light theme and frequency pills
    secondary = Color(0xFF6D4DD9), // Muted darker purple for dark theme
    tertiary = Color(0xFFC2185B), // Muted darker pink for dark theme
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    primaryContainer = Color(0xFF2E2D5C), // Dark muted container for dark theme
    secondaryContainer = Color(0xFF3D2A5C), // Dark muted container for dark theme
    tertiaryContainer = Color(0xFF4A1A2E) // Dark muted container for dark theme
)

private val LightColorScheme = lightColorScheme(
    primary = ConnectPrimary,
    secondary = ConnectSecondary,
    tertiary = ConnectTertiary,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    surfaceVariant = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White
)

@Composable
fun ConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ConnectShapes,
        content = {
            CompositionLocalProvider(LocalConnectDarkTheme provides darkTheme) {
                content()
            }
        }
    )
}

@Composable
fun ConnectTheme(
    themeMode: ThemeMode,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    
    ConnectTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}