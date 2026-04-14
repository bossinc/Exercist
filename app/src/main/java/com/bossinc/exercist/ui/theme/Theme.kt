package com.bossinc.exercist.ui.theme

import android.app.Activity
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
    primary = Green80,
    onPrimary = OnGreenContainer40,
    primaryContainer = GreenContainer80,
    onPrimaryContainer = OnGreenContainer80,
    secondary = Blue80,
    onSecondary = OnBlueContainer40,
    secondaryContainer = BlueContainer80,
    onSecondaryContainer = OnBlueContainer80,
    tertiary = Yellow80,
    onTertiary = OnYellowContainer40,
    tertiaryContainer = YellowContainer80,
    onTertiaryContainer = OnYellowContainer80,
    background = Background80,
    onBackground = OnSurface80,
    surface = Surface80,
    onSurface = OnSurface80,
    surfaceVariant = SurfaceVariant80,
    onSurfaceVariant = OnSurfaceVariant80,
    error = Error80,
    onError = Color.White,
    errorContainer = ErrorContainer80,
    onErrorContainer = ErrorContainer40,
    outline = Outline80
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = GreenContainer40,
    onPrimaryContainer = OnGreenContainer40,
    secondary = Blue40,
    onSecondary = Color.White,
    secondaryContainer = BlueContainer40,
    onSecondaryContainer = OnBlueContainer40,
    tertiary = Yellow40,
    onTertiary = Color.White,
    tertiaryContainer = YellowContainer40,
    onTertiaryContainer = OnYellowContainer40,
    background = Background40,
    onBackground = OnSurface40,
    surface = Surface40,
    onSurface = OnSurface40,
    surfaceVariant = SurfaceVariant40,
    onSurfaceVariant = OnSurfaceVariant40,
    error = Error40,
    onError = Color.White,
    errorContainer = ErrorContainer40,
    onErrorContainer = Color(0xFF7F0000),
    outline = Outline40
)

@Composable
fun ExercistTheme(
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
        shapes = Shapes,
        typography = Typography,
        content = content
    )
}
