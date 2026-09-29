package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ZXDarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color(0xFF001E2E),
    primaryContainer = Color(0xFF00384D),
    onPrimaryContainer = Color(0xFFBBE9FF),
    secondary = BlueAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF00325B),
    onSecondaryContainer = Color(0xFFCCE5FF),
    tertiary = CyanGlow,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    outlineVariant = CardBorderSubtle,
)

// Clean fallback for light theme if requested in settings
private val ZXLightColorScheme = lightColorScheme(
    primary = CyanMuted,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4EFFF),
    onPrimaryContainer = Color(0xFF001E2E),
    secondary = BlueAccent,
    onSecondary = Color.White,
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFFCBD5E1),
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ZXDarkColorScheme else ZXLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
