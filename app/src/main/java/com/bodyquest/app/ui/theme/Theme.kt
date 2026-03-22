package com.bodyquest.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BodyQuestColorScheme = darkColorScheme(
    primary = NeonPurple,
    secondary = NeonPurpleLight,
    tertiary = NeonCyan,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = NeonRed
)

@Composable
fun BodyQuestTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BodyQuestColorScheme,
        typography = Typography,
        content = content
    )
}
