package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CineDarkColorScheme = darkColorScheme(
    primary = CinemaCyan,
    onPrimary = ObsidianBg,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = CinemaCyan,
    secondary = CinemaAmber,
    onSecondary = ObsidianBg,
    tertiary = CinemaGreen,
    onTertiary = ObsidianBg,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = DeckSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = SurfaceHighlight,
    error = CinemaRed,
    onError = TextPrimary
)

@Composable
fun CineAiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CineDarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
