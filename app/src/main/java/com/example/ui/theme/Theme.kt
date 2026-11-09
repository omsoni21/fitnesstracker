package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonLime,
    onPrimary = CharcoalDarker,
    primaryContainer = Color(0xFF263300),
    onPrimaryContainer = NeonLime,
    secondary = ElectricCyan,
    onSecondary = CharcoalDarker,
    secondaryContainer = Color(0xFF00363D),
    onSecondaryContainer = ElectricCyan,
    tertiary = FieryOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4E1D00),
    onTertiaryContainer = Color(0xFFFFB59D),
    background = CharcoalBackground,
    onBackground = TextPrimaryDark,
    surface = CharcoalSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CharcoalCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = CharcoalBorder,
    outlineVariant = Color(0xFF232834)
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4FF66),
    onPrimaryContainer = Color(0xFF1B2C00),
    secondary = Color(0xFF006876),
    onSecondary = Color.White,
    tertiary = Color(0xFF9E4300),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to true as per requirements: "Dark theme as default, with optional light theme"
    dynamicColor: Boolean = false, // Keep branded neon theme consistent
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
