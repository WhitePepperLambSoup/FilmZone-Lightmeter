package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MeterDarkColorScheme = darkColorScheme(
    primary = MeterAccentPrimary,
    onPrimary = MeterAccentOnPrimary,
    primaryContainer = MeterAccentSubtle,
    onPrimaryContainer = MeterAccentPrimary,
    secondary = MeterAmber,
    onSecondary = MeterBlack,
    secondaryContainer = MeterCardElevated,
    onSecondaryContainer = MeterTextPrimary,
    tertiary = MeterCyan,
    onTertiary = MeterBlack,
    background = MeterBlack,
    onBackground = MeterTextPrimary,
    surface = MeterDarkSurface,
    onSurface = MeterTextPrimary,
    surfaceVariant = MeterCardBg,
    onSurfaceVariant = MeterTextSecondary,
    outline = MeterBorder,
    outlineVariant = MeterBorderSubtle,
    error = MeterRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to pro dark OLED theme for photography light meters
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MeterDarkColorScheme,
        typography = Typography,
        content = content
    )
}
