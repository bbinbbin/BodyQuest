package com.bodyquest.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val NeonPurple = Color(0xFF8B5CF6)
private val NeonPurpleLight = Color(0xFFBB86FC)
private val NeonCyan = Color(0xFF06B6D4)
private val DarkBackground = Color(0xFF0D0D1A)
private val DarkSurface = Color(0xFF1A1A2E)
private val NeonRed = Color(0xFFEF4444)

private val WearColors = Colors(
    primary = NeonPurple,
    primaryVariant = NeonPurpleLight,
    secondary = NeonCyan,
    secondaryVariant = NeonCyan,
    background = DarkBackground,
    surface = DarkSurface,
    error = NeonRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE8E8F0),
    onSurface = Color(0xFFE8E8F0),
    onSurfaceVariant = Color(0xFF9090B0),
    onError = Color.White
)

@Composable
fun BodyQuestWearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = WearColors,
        content = content
    )
}
