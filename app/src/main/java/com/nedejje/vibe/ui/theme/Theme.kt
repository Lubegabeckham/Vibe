package com.nedejje.vibe.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary             = VibePrimaryBright,
    onPrimary           = Color.White,
    primaryContainer    = VibePrimaryContainer,
    onPrimaryContainer  = Color(0xFFDDC8FF),
    secondary           = VibeSecondary,
    onSecondary         = Color(0xFF2E1C00),
    secondaryContainer  = VibeSecondaryContainer,
    onSecondaryContainer= Color(0xFFFFDFA0),
    tertiary            = VibeAccent,
    onTertiary          = Color(0xFF003328),
    error               = VibeError,
    background          = VibeDeepViolet,
    onBackground        = Color(0xFFEDE8FF),
    surface             = VibeSurface,
    onSurface           = Color(0xFFEDE8FF),
    surfaceVariant      = VibeSurfaceVariant,
    onSurfaceVariant    = Color(0xFFB0A8D0),
    outline             = Color(0xFF6B5FA0),
    inverseSurface      = Color(0xFFEDE8FF),
    inverseOnSurface    = VibeDeepViolet,
    inversePrimary      = VibeLightPrimary
)

private val LightColorScheme = lightColorScheme(
    primary             = VibeLightPrimary,
    onPrimary           = Color.White,
    primaryContainer    = VibeLightPrimaryCont,
    onPrimaryContainer  = Color(0xFF1A005C),
    secondary           = Color(0xFF8B6200),
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFFFFDFA0),
    onSecondaryContainer= Color(0xFF2E1C00),
    tertiary            = Color(0xFF006B54),
    onTertiary          = Color.White,
    error               = Color(0xFFCC1B3F),
    background          = VibeLightBackground,
    onBackground        = Color(0xFF100B26),
    surface             = VibeLightSurface,
    onSurface           = Color(0xFF100B26),
    surfaceVariant      = VibeLightSurfaceVar,
    onSurfaceVariant    = Color(0xFF4A3F70),
    outline             = Color(0xFF8B7BB5),
    inverseSurface      = VibeSurface,
    inverseOnSurface    = Color(0xFFEDE8FF),
    inversePrimary      = VibePrimaryBright
)

@Composable
fun VibeTheme(
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
        typography  = Typography,
        shapes      = Shapes,
        content     = content
    )
}
