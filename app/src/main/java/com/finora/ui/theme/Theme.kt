package com.finora.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

private val DarkColors = darkColorScheme(
    primary = Cyan,
    secondary = Violet,
    tertiary = CashIn,
    background = Ink,
    surface = Night,
    surfaceVariant = Elevated,
    onPrimary = Ink,
    onBackground = Bright,
    onSurface = Bright,
    onSurfaceVariant = Muted,
    outline = SoftLine,
    error = CashOut
)

private val LightColors = lightColorScheme(
    primary = ColorTokens.primary,
    secondary = Violet,
    tertiary = ColorTokens.success,
    background = LightCanvas,
    surface = LightCard,
    surfaceVariant = ColorTokens.surfaceVariant,
    onPrimary = LightInk,
    onBackground = LightInk,
    onSurface = LightInk,
    onSurfaceVariant = ColorTokens.muted,
    outline = ColorTokens.line,
    error = ColorTokens.error
)

private object ColorTokens {
    val primary = androidx.compose.ui.graphics.Color(0xFF006B78)
    val success = androidx.compose.ui.graphics.Color(0xFF158052)
    val error = androidx.compose.ui.graphics.Color(0xFFC83F4D)
    val surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE9EEF7)
    val muted = androidx.compose.ui.graphics.Color(0xFF61718A)
    val line = androidx.compose.ui.graphics.Color(0xFFD7DFEB)
}

@Composable
fun FinoraTheme(themeChoice: String, content: @Composable () -> Unit) {
    val dark = when (themeChoice) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val locale = LocalConfiguration.current.locales.get(0)
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = if (locale.language == "bn") FinoraBengaliTypography else FinoraTypography,
        shapes = FinoraShapes,
        content = content
    )
}
