package com.nguonc.stream.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Local flag so components can opt into OLED-friendly true-black surfaces
// even when the system theme is light (e.g. player overlay).
val LocalIsDarkCinematic = compositionLocalOf { true }

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
    scrim = Color.Black.copy(alpha = 0.70f),
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
 * FilmFlix Premium 3.0 Theme
 *
 *  - True OLED dark (0x00 background) for pixel-perfect blacks
 *  - Edge-to-edge with transparent system bars
 *  - Premium shapes (24dp large, 32dp XL, squircle available)
 *  - Motion / Shadows / Icons / Fonts wired via composition locals
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

    CompositionLocalProvider(LocalIsDarkCinematic provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppMaterialShapes,
            content = content,
        )
    }
}

/** Alias for the new naming. */
@Composable
fun FilmFlixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = NguonCTheme(darkTheme, content)
