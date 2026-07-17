package com.nguonc.stream.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// Premium Shapes - consistent 20dp language
private val PremiumShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val DarkColors = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = Color.Black,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = Color(0xFFFFE082),
    tertiary = AccentCyan,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF122629),
    onTertiaryContainer = Color(0xFFB6F5FF),
    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFF3A0A12),
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkBackground,
    onBackground = OnDarkBackground,
    surface = DarkSurface,
    onSurface = OnDarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSurfaceVariant,
    surfaceDim = DarkBackground,
    surfaceBright = DarkSurfaceElevated,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceAlt,
    surfaceContainerHighest = DarkSurfaceElevated,
    surfaceContainerLow = DarkBackgroundSoft,
    surfaceContainerLowest = DarkBackground,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = Color.Black.copy(alpha = 0.65f),
    inverseSurface = Color(0xFFE8E9F0),
    inverseOnSurface = Color(0xFF11111A),
    inversePrimary = PrimaryDark
)

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFFB89600),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFECB3),
    onSecondaryContainer = Color(0xFF261A00),
    tertiary = Color(0xFF00B8D4),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = OnLightBackground,
    surface = LightSurface,
    onSurface = OnLightSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = OnLightSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
)

/**
 * FilmFlix Premium 2.0 Theme
 * - True OLED dark with layered surfaces
 * - Edge-to-edge with transparent system bars
 * - Premium shapes & motion ready
 */
@Composable
fun NguonCTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
            @Suppress("DEPRECATION")
            run {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = PremiumShapes,
        content = content,
    )
}

// Alias for new naming
@Composable
fun FilmFlixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = NguonCTheme(darkTheme, content)
