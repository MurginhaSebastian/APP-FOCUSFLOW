package com.example.focusflow.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

val FocusSeed = Color(0xFF689F38)

val FocusPrimary = Color(0xFF689F38)
val FocusOnPrimary = Color.White
val FocusPrimaryContainer = Color(0xFFC8E6C9)
val FocusOnPrimaryContainer = Color(0xFF1B3D0B)

val FocusSecondary = Color(0xFF4DD0E1)
val FocusOnSecondary = Color.White
val FocusSecondaryContainer = Color(0xFFB2EBF2)
val FocusOnSecondaryContainer = Color(0xFF004D5A)

val FocusTertiary = Color(0xFF1A237E)
val FocusOnTertiary = Color.White
val FocusTertiaryContainer = Color(0xFFE8EAF6)
val FocusOnTertiaryContainer = Color(0xFF1A237E)

val FocusAccent = Color(0xFFFFD54F)
val FocusOnAccent = Color(0xFF3E2E00)
val FocusAccentContainer = Color(0xFFFFF9C4)
val FocusOnAccentContainer = Color(0xFF3E2E00)

val FocusBackground = Color(0xFFF1F8E9)
val FocusOnBackground = Color(0xFF1C1B1F)

val FocusSurface = Color.White
val FocusOnSurface = Color(0xFF1C1B1F)
val FocusSurfaceVariant = Color(0xFFF5F5F5)
val FocusOnSurfaceVariant = Color(0xFF49454F)

val FocusOutline = Color(0xFFDDDDDD)
val FocusError = Color(0xFFE57373)
val FocusOnError = Color.White

val DarkFocusBackground = Color(0xFF121212)
val DarkFocusSurface = Color(0xFF1E1E1E)
val DarkFocusSurfaceVariant = Color(0xFF2C2C2C)
val DarkFocusOnBackground = Color(0xFFE6E6E6)
val DarkFocusOnSurface = Color(0xFFE6E6E6)

val LightColorScheme = lightColorScheme(
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
    onError = FocusOnError,
)

val DarkColorScheme = darkColorScheme(
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
    onError = FocusOnError,
)
