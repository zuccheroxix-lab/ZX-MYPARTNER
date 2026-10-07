package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalAccentColor = staticCompositionLocalOf { CyanAccent }

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
    accentColor: Color = CyanAccent,
    content: @Composable () -> Unit
) {
    val baseScheme = if (darkTheme) ZXDarkColorScheme else ZXLightColorScheme
    val colorScheme = baseScheme.copy(
        primary = accentColor,
        tertiary = accentColor
    )

    CompositionLocalProvider(LocalAccentColor provides accentColor) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
