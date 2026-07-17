package com.nguonc.stream.ui.theme

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

// Bảng màu chủ đạo: đỏ rạp chiếu phim trên nền trung tính
private val AccentRed = Color(0xFFE50914)

private val DarkColors = darkColorScheme(
    primary = AccentRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5C0A0E),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFC7BDBD),
    background = Color(0xFF0F1115),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF16181D),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF23262E),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF4A4E58),
)

private val LightColors = lightColorScheme(
    primary = AccentRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF775652),
    background = Color(0xFFFDF8F7),
    onBackground = Color(0xFF1C1B1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1B),
    surfaceVariant = Color(0xFFF4DDDB),
    onSurfaceVariant = Color(0xFF534342),
    outline = Color(0xFF857371),
)

/** Tự động dark/light theo hệ thống (lựa chọn của user). */
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
        content = content,
    )
}
