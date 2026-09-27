package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// RK TIFFINES Luxury Black & Gold Material 3 Dark Theme
private val RkDarkColorScheme = darkColorScheme(
    primary = RkGoldPrimary,
    onPrimary = RkTextOnGold,
    primaryContainer = Color(0xFF382300),
    onPrimaryContainer = Color(0xFFFFE082),

    secondary = RkOrangeSecondary,
    onSecondary = Color(0xFF2A0D00),
    secondaryContainer = Color(0xFF421700),
    onSecondaryContainer = Color(0xFFFFCC80),

    tertiary = RkWarmRedTertiary,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF441306),
    onTertiaryContainer = Color(0xFFFFAB91),

    background = RkBlackBackground,
    onBackground = RkTextPrimary,

    surface = RkSurfaceDark,
    onSurface = RkTextPrimary,

    surfaceVariant = RkSurfaceVariantDark,
    onSurfaceVariant = RkTextSecondary,

    surfaceContainer = RkSurfaceDark,
    surfaceContainerHigh = RkSurfaceElevated,
    surfaceContainerHighest = Color(0xFF34302B),

    outline = RkBorderGoldSubtle,
    outlineVariant = Color(0xFF332E28),

    error = Color(0xFFCF6679),
    onError = Color(0xFF1E0004)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Keep consistent RK brand colors across all devices
    content: @Composable () -> Unit,
) {
    // The RK Branded dark theme is the primary & default design system
    MaterialTheme(
        colorScheme = RkDarkColorScheme,
        typography = Typography,
        content = content
    )
}
