package com.nguonc.stream.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp

// ======================================================
// FILM FLIX PREMIUM 3.0 — TYPOGRAPHY SYSTEM
// ------------------------------------------------------
// Font: Outfit (when wired) / Google Sans / Roboto fallback.
// Scale: tuned for cinematic feel — tight tracking on display,
// airy on body, all-caps labels with wide tracking for microcopy.
// ======================================================

val AppTypography = Typography(

    // HERO — For massive splash titles (Detail hero name, Browse hero)
    displayLarge = TextStyle(
        fontFamily = AppDisplayFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.4).sp,
        lineBreak = LineBreak.Heading
    ),
    displayMedium = TextStyle(
        fontFamily = AppDisplayFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-1.0).sp,
        lineBreak = LineBreak.Heading
    ),
    displaySmall = TextStyle(
        fontFamily = AppDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.6).sp,
        lineBreak = LineBreak.Heading
    ),

    // Section & Card Titles
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.1).sp
    ),

    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.1.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.5.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.2.sp
    ),

    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.3.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.5.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.8.sp
    )
)

/**
 * Premium extra styles not in Material 3.
 * Use via `MaterialTheme.typography` extensions or directly as `PremiumTextStyles.X`.
 */
object PremiumTextStyles {
    /** Larger than displayLarge — for the hero title on Detail screen. */
    val HeroSuper = TextStyle(
        fontFamily = AppDisplayFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 44.sp,
        lineHeight = 46.sp,
        letterSpacing = (-1.6).sp
    )

    /** All-caps badge style with wide tracking. */
    val Badge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 9.5.sp,
        lineHeight = 11.sp,
        letterSpacing = 0.9.sp
    )

    /** Oversized number for ranking lists ("Top 10" huge numerals). */
    val NumberHuge = TextStyle(
        fontFamily = AppDisplayFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 72.sp,
        lineHeight = 64.sp,
        letterSpacing = (-3).sp
    )

    /** Monospace caption for timestamps, durations. */
    val CaptionMono = TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp
    )

    /** Eyebrow label above big titles — small caps feel. */
    val Eyebrow = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 10.5.sp,
        lineHeight = 13.sp,
        letterSpacing = 1.4.sp
    )
}
