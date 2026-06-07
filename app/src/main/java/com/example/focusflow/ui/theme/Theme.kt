package com.example.focusflow.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = FocusPrimary,
    onPrimary = FocusOnPrimary,
    primaryContainer = FocusPrimaryContainer,
    onPrimaryContainer = FocusOnPrimaryContainer,
    secondary = FocusSecondary,
    onSecondary = FocusOnSecondary,
    secondaryContainer = FocusSecondaryContainer,
    onSecondaryContainer = FocusOnSecondaryContainer,
    tertiary = FocusTertiary,
    onTertiary = FocusOnTertiary,
    tertiaryContainer = FocusTertiaryContainer,
    onTertiaryContainer = FocusOnTertiaryContainer,
    background = FocusBackground,
    onBackground = FocusOnBackground,
    surface = FocusSurface,
    onSurface = FocusOnSurface,
    surfaceVariant = FocusSurfaceVariant,
    onSurfaceVariant = FocusOnSurfaceVariant,
    outline = FocusOutline,
    error = FocusError,
    onError = FocusOnError
)

private val DarkColorScheme = darkColorScheme(
    primary = FocusPrimaryContainer,
    onPrimary = FocusPrimary,
    primaryContainer = FocusPrimary,
    onPrimaryContainer = FocusPrimaryContainer,
    secondary = FocusSecondaryContainer,
    onSecondary = FocusSecondary,
    secondaryContainer = FocusSecondary,
    onSecondaryContainer = FocusSecondaryContainer,
    tertiary = FocusTertiaryContainer,
    onTertiary = FocusTertiary,
    tertiaryContainer = FocusTertiary,
    onTertiaryContainer = FocusTertiaryContainer,
    background = DarkFocusBackground,
    onBackground = DarkFocusOnBackground,
    surface = DarkFocusSurface,
    onSurface = DarkFocusOnSurface,
    surfaceVariant = DarkFocusSurfaceVariant,
    onSurfaceVariant = FocusOnSurfaceVariant,
    outline = FocusOutline,
    error = FocusError,
    onError = FocusOnError
)

@Composable
fun FocusFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
