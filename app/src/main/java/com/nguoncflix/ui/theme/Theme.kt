package com.nguoncflix.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NetflixRed = Color(0xFFE50914)
val NetflixDark = Color(0xFF141414)
val NetflixDarkGray = Color(0xFF181818)
val NetflixGray = Color(0xFF333333)
val NetflixWhite = Color(0xFFFFFFFF)
val NetflixTextSecondary = Color(0xFFB3B3B3)

private val DarkColorScheme = darkColorScheme(
    primary = NetflixRed,
    background = NetflixDark,
    surface = NetflixDarkGray,
    onPrimary = NetflixWhite,
    onBackground = NetflixWhite,
    onSurface = NetflixWhite,
    secondary = NetflixGray
)

@Composable
fun NguoncFlixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
