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

/**
 * FILM FLIX 3.0 — Aurora Theme
 * - True OLED black với 9 cấp surface
 * - Edge-to-edge, status & nav bar trong suốt
 * - Brand cherry dùng làm điểm nhấn, ánh vàng champagne cho rating
 */

private val PremiumShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val DarkColors = darkColorScheme(
    primary = BrandCherry,
    onPrimary = Color.White,
    primaryContainer = BrandCherryInk,
    onPrimaryContainer = BrandCherryLight,
    secondary = SunGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2B2310),
    onSecondaryContainer = SunCream,
    tertiary = AuroraCyan,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF0B2326),
    onTertiaryContainer = Color(0xFFB6F5FF),
    error = StatusDanger,
    onError = Color.White,
    errorContainer = Color(0xFF2A050D),
    onErrorContainer = Color(0xFFFFDAD6),
    background = OLED1,
    onBackground = Ink90,
    surface = OLED3,
    onSurface = Ink80,
    surfaceVariant = OLED5,
    onSurfaceVariant = Ink60,
    surfaceDim = OLED0,
    surfaceBright = OLED6,
    surfaceContainer = OLED3,
    surfaceContainerHigh = OLED4,
    surfaceContainerHighest = OLED6,
    surfaceContainerLow = OLED2,
    surfaceContainerLowest = OLED0,
    outline = OLED8,
    outlineVariant = Color(0xFF3A3D52),
    scrim = Color.Black.copy(alpha = 0.7f),
    inverseSurface = Color(0xFFE8E9F0),
    inverseOnSurface = Color(0xFF11111A),
    inversePrimary = BrandCherrySoft
)

private val LightColors = lightColorScheme(
    primary = BrandCherry,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9DE),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFFB89600),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFECB3),
    onSecondaryContainer = Color(0xFF261A00),
    tertiary = AuroraCyan,
    onTertiary = Color.White,
    background = Paper1,
    onBackground = PaperInk,
    surface = Paper0,
    onSurface = Color(0xFF181A22),
    surfaceVariant = Paper2,
    onSurfaceVariant = Color(0xFF6A6E8A),
    outline = Paper3,
    outlineVariant = Color(0xFFC9CCD8)
)

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

@Composable
fun FilmFlixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = NguonCTheme(darkTheme, content)
