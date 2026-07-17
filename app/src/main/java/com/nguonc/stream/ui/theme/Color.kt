package com.nguonc.stream.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ======================================================
// FILM FLIX PREMIUM 2.0 - CINEMATIC DESIGN SYSTEM
// Inspired by Apple TV+ x Netflix x Linear - Ultra Modern
// ======================================================

// --- Primary: Cherry Pulse - More sophisticated than pure red ---
val Primary = Color(0xFFFF234B)           // Cherry pulse - main CTA
val PrimaryLight = Color(0xFFFF4D6D)      // Soft cherry
val PrimaryDark = Color(0xFFCC1A3B)       // Deep cherry
val PrimaryMuted = Color(0xFFFF234B).copy(alpha = 0.12f)
val PrimaryContainer = Color(0xFF2A0A12)
val OnPrimaryContainer = Color(0xFFFFD9DE)

// --- Secondary: Solar - Premium gold ---
val Secondary = Color(0xFFFFD60A)
val SecondaryMuted = Color(0xFFFFD60A).copy(alpha = 0.15f)
val SecondaryContainer = Color(0xFF2B2310)

// --- Tertiary / Accent ---
val AccentCyan = Color(0xFF30E7F0)
val AccentViolet = Color(0xFF8B5CF6)
val AccentGreen = Color(0xFF22FF88)

// --- Backwards Compatibility Aliases ---
val PrimaryRed = Primary
val PrimaryRedLight = PrimaryLight
val PrimaryRedDark = PrimaryDark
val PrimaryRedContainer = PrimaryContainer
val OnPrimaryRedContainer = OnPrimaryContainer
val GoldStar = Secondary
val GoldVip = Color(0xFFFFC94A)

// ======================================================
// DARK THEME - TRUE OLED CINEMATIC (0 0 0 = Pure Cinema)
// ======================================================
val DarkBackground = Color(0xFF050507)        // Absolute cinema black
val DarkBackgroundSoft = Color(0xFF0A0A0F)    // Lifted black for layered depth
val DarkSurface = Color(0xFF11111A)           // Primary surface
val DarkSurfaceAlt = Color(0xFF16171F)        // Alternate
val DarkSurfaceVariant = Color(0xFF1C1E2B)    // Chip / search / card bg
val DarkSurfaceCard = Color(0xFF181A27)       // Poster card
val DarkSurfaceElevated = Color(0xFF222436)   // Modal, BottomSheet, Dialog
val DarkSurfaceGlass = Color(0xFF1A1D2A).copy(alpha = 0.8f) // Glass morphism base

val DarkOutline = Color(0xFF282A3A)           // Subtle border
val DarkOutlineVariant = Color(0xFF3A3D52)    // Strong border
val DarkOutlineGlow = Primary.copy(alpha = 0.25f) // Glow border

val OnDarkBackground = Color(0xFFF5F6FB)
val OnDarkSurface = Color(0xFFE8E9F0)
val OnDarkSurfaceVariant = Color(0xFF9AA0B7)  // Secondary text - cool gray
val OnDarkSurfaceTertiary = Color(0xFF6A708B) // Tertiary - muted

// ======================================================
// LIGHT THEME - Paper & Ink (Modern Minimal)
// ======================================================
val LightBackground = Color(0xFFF9F9FB)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF0F1F7)
val LightSurfaceCard = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF6F7FB)
val LightOutline = Color(0xFFE2E4ED)
val LightOutlineVariant = Color(0xFFCACDE0)

val OnLightBackground = Color(0xFF0F1117)
val OnLightSurface = Color(0xFF181A22)
val OnLightSurfaceVariant = Color(0xFF686E8C)

// ======================================================
// SEMANTIC COLORS
// ======================================================
val Success = Color(0xFF00E676)
val Warning = Color(0xFFFFB400)
val Error = Color(0xFFFF234B)
val Info = AccentCyan

// Rating gradient colors
val RatingGoldStart = Color(0xFFFFD60A)
val RatingGoldEnd = Color(0xFFFF8A00)

// ======================================================
// GRADIENTS - Signature FilmFlix
// ======================================================
object AppGradients {
    // Primary CTA gradient - used for FAB, Play button, indicators
    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF234B), Color(0xFFFF4D6D), Color(0xFFFF8A9A))
    )
    val PrimaryHorizontal = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF234B), Color(0xFFFF5C7A))
    )
    val PrimaryVertical = Brush.verticalGradient(
        colors = listOf(Color(0xFFFF5C7A), Color(0xFFB81A36))
    )

    // Gold / Premium - for VIP, 4K, Awards
    val GoldGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFD60A), Color(0xFFFF9F0A))
    )
    val GoldShimmer = Brush.linearGradient(
        colors = listOf(Color(0xFFFFD60A), Color(0xFFFFEFB5), Color(0xFFFFD60A))
    )

    // Hero Overlays - cinematic vignette
    val HeroTopScrim = Brush.verticalGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.85f),
            Color.Black.copy(alpha = 0.45f),
            Color.Black.copy(alpha = 0.05f),
            Color.Transparent
        )
    )
    val HeroBottomScrim = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.25f),
            Color.Black.copy(alpha = 0.78f),
            Color.Black.copy(alpha = 0.96f)
        )
    )
    val HeroSideScrim = Brush.horizontalGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.85f),
            Color.Black.copy(alpha = 0.3f),
            Color.Transparent
        )
    )

    // Card bottom overlay - deep scrim for text readability
    val CardBottomOverlay = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.15f),
            Color.Black.copy(alpha = 0.55f),
            Color.Black.copy(alpha = 0.92f)
        )
    )

    // Glass reflection
    val GlassBorder = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.18f),
            Color.White.copy(alpha = 0.05f),
            Color.White.copy(alpha = 0.02f)
        )
    )

    // Shimmer skeleton loader
    val Shimmer = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1C1E2B),
            Color(0xFF282B3D),
            Color(0xFF1C1E2B)
        )
    )

    // Background Ambient Glow - decorative blob
    val AmbientRedGlow = Brush.radialGradient(
        colors = listOf(
            Primary.copy(alpha = 0.28f),
            Primary.copy(alpha = 0.08f),
            Color.Transparent
        )
    )
    val AmbientVioletGlow = Brush.radialGradient(
        colors = listOf(
            AccentViolet.copy(alpha = 0.22f),
            AccentViolet.copy(alpha = 0.06f),
            Color.Transparent
        )
    )

    // Button press state
    val ButtonPressed = Brush.linearGradient(
        colors = listOf(Color(0xFFD81F43), Color(0xFFB81A36))
    )

    // Premium border mesh
    val MeshGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF234B).copy(alpha = 0.8f),
            Color(0xFF8B5CF6).copy(alpha = 0.6f),
            Color(0xFF30E7F0).copy(alpha = 0.6f)
        )
    )
}

// Extra surface helpers
val DarkSurfaceGlassAlt = DarkSurfaceVariant.copy(alpha = 0.6f)
