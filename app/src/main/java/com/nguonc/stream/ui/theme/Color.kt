package com.nguonc.stream.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ======================================================
// FILM FLIX PREMIUM 3.0 — CINEMATIC DESIGN SYSTEM
// Inspired by Apple TV+ × Netflix × Linear × Stripe
// ======================================================
// Principles:
//  - True OLED black background (pure 0x000000) for dark mode
//  - Layered surfaces using "container" steps from M3 spec
//  - Cherry Pulse primary kept (great brand recognition)
//  - New "Spotlight" gradient for hero accents
//  - Refined gold gradient (more amber, less yellow) for ratings
// ======================================================

// --- Primary: Cherry Pulse ---
val Primary = Color(0xFFFF234B)
val PrimaryLight = Color(0xFFFF4D6D)
val PrimaryDark = Color(0xFFCC1A3B)
val PrimaryMuted = Color(0xFFFF234B).copy(alpha = 0.12f)
val PrimaryContainer = Color(0xFF2A0A12)
val OnPrimaryContainer = Color(0xFFFFD9DE)

// --- Secondary: Amber Gold (more sophisticated than pure yellow) ---
val Secondary = Color(0xFFFFC94A)
val SecondaryLight = Color(0xFFFFE082)
val SecondaryMuted = Color(0xFFFFC94A).copy(alpha = 0.15f)
val SecondaryContainer = Color(0xFF2B2310)

// --- Tertiary / Accent ---
val AccentCyan = Color(0xFF30E7F0)
val AccentViolet = Color(0xFF8B5CF6)
val AccentGreen = Color(0xFF22FF88)
val AccentPink = Color(0xFFFF6AC1)

// --- Backwards Compatibility Aliases ---
val PrimaryRed = Primary
val PrimaryRedLight = PrimaryLight
val PrimaryRedDark = PrimaryDark
val PrimaryRedContainer = PrimaryContainer
val OnPrimaryRedContainer = OnPrimaryContainer
val GoldStar = Secondary
val GoldVip = Color(0xFFFFC94A)

// ======================================================
// DARK THEME — TRUE OLED CINEMATIC
// 0x00 base for pixel-perfect black on OLED panels.
// Surfaces step up by ~6% lightness per M3 container spec.
// ======================================================
val DarkBackground = Color(0xFF000000)           // Absolute OLED black — pixel-perfect for AMOLED
val DarkBackgroundSoft = Color(0xFF07070B)       // Lifted black for layered depth
val DarkSurface = Color(0xFF0C0C12)              // Primary surface
val DarkSurfaceAlt = Color(0xFF11121A)           // Alternate
val DarkSurfaceVariant = Color(0xFF171823)       // Chip / search / card bg
val DarkSurfaceCard = Color(0xFF141520)          // Poster card
val DarkSurfaceElevated = Color(0xFF1B1D2B)      // Modal, BottomSheet, Dialog
val DarkSurfaceGlass = Color(0xFF1A1D2A).copy(alpha = 0.72f) // Glass morphism base
val DarkSurfaceHighlight = Color(0xFF252836)     // Hover / focus ring

val DarkOutline = Color(0xFF262838)              // Subtle border
val DarkOutlineVariant = Color(0xFF3A3D52)       // Strong border
val DarkOutlineGlow = Primary.copy(alpha = 0.32f) // Glow border

val OnDarkBackground = Color(0xFFF7F8FC)
val OnDarkSurface = Color(0xFFEAEBF2)
val OnDarkSurfaceVariant = Color(0xFF9CA0B8)     // Secondary text — cool gray
val OnDarkSurfaceTertiary = Color(0xFF6A708B)    // Tertiary — muted

// ======================================================
// LIGHT THEME — Paper & Ink (Modern Minimal)
// ======================================================
val LightBackground = Color(0xFFFAFAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F2F8)
val LightSurfaceCard = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF6F7FB)
val LightOutline = Color(0xFFE3E5EE)
val LightOutlineVariant = Color(0xFFCCCFE2)

val OnLightBackground = Color(0xFF0F1117)
val OnLightSurface = Color(0xFF181A22)
val OnLightSurfaceVariant = Color(0xFF686E8C)

// ======================================================
// SEMANTIC COLORS
// ======================================================
val Success = Color(0xFF22C55E)
val Warning = Color(0xFFFFB400)
val Error = Color(0xFFFF3B5C)
val Info = AccentCyan

// Rating gradient colors — amber → orange for warmer gold feel
val RatingGoldStart = Color(0xFFFFC94A)
val RatingGoldEnd = Color(0xFFFF8A00)

// ======================================================
// GRADIENTS — Signature FilmFlix 3.0
// ======================================================
object AppGradients {
    // Primary CTA gradient — used for FAB, Play button, indicators
    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF234B), Color(0xFFFF4D6D), Color(0xFFFF8A9A))
    )
    val PrimaryHorizontal = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF234B), Color(0xFFFF5C7A))
    )
    val PrimaryVertical = Brush.verticalGradient(
        colors = listOf(Color(0xFFFF5C7A), Color(0xFFB81A36))
    )
    val PrimaryDiagonal = Brush.linearGradient(
        colors = listOf(Color(0xFFFF234B), Color(0xFFFF6D8C), Color(0xFF8B5CF6))
    )

    // Spotlight — for hero accents, "Featured" pill, etc.
    val Spotlight = Brush.radialGradient(
        colors = listOf(
            Color(0xFFFF234B).copy(alpha = 0.55f),
            Color(0xFF8B5CF6).copy(alpha = 0.30f),
            Color.Transparent
        )
    )

    // Gold / Premium — for VIP, 4K, Awards (warmer than 2.0)
    val GoldGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFC94A), Color(0xFFFF9F0A))
    )
    val GoldShimmer = Brush.linearGradient(
        colors = listOf(Color(0xFFFFC94A), Color(0xFFFFEFB5), Color(0xFFFFC94A))
    )

    // Hero Overlays — cinematic vignette (deepened for true OLED)
    val HeroTopScrim = Brush.verticalGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.85f),
            Color.Black.copy(alpha = 0.42f),
            Color.Black.copy(alpha = 0.05f),
            Color.Transparent
        )
    )
    val HeroBottomScrim = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.30f),
            Color.Black.copy(alpha = 0.82f),
            Color.Black.copy(alpha = 0.98f)
        )
    )
    val HeroSideScrim = Brush.horizontalGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.85f),
            Color.Black.copy(alpha = 0.3f),
            Color.Transparent
        )
    )

    // Card bottom overlay — deep scrim for text readability
    val CardBottomOverlay = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.15f),
            Color.Black.copy(alpha = 0.60f),
            Color.Black.copy(alpha = 0.94f)
        )
    )

    // Glass reflection
    val GlassBorder = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.20f),
            Color.White.copy(alpha = 0.06f),
            Color.White.copy(alpha = 0.02f)
        )
    )

    // Shimmer skeleton loader — pure OLED-friendly
    val Shimmer = Brush.linearGradient(
        colors = listOf(
            Color(0xFF111218),
            Color(0xFF1F2030),
            Color(0xFF111218)
        )
    )

    // Background Ambient Glow — decorative blob
    val AmbientRedGlow = Brush.radialGradient(
        colors = listOf(
            Primary.copy(alpha = 0.32f),
            Primary.copy(alpha = 0.08f),
            Color.Transparent
        )
    )
    val AmbientVioletGlow = Brush.radialGradient(
        colors = listOf(
            AccentViolet.copy(alpha = 0.24f),
            AccentViolet.copy(alpha = 0.06f),
            Color.Transparent
        )
    )
    val AmbientCyanGlow = Brush.radialGradient(
        colors = listOf(
            AccentCyan.copy(alpha = 0.20f),
            AccentCyan.copy(alpha = 0.05f),
            Color.Transparent
        )
    )

    // Button press state — slightly desaturated for tactile feel
    val ButtonPressed = Brush.linearGradient(
        colors = listOf(Color(0xFFD81F43), Color(0xFFB81A36))
    )

    // Premium border mesh — for hero pill / featured borders
    val MeshGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF234B).copy(alpha = 0.85f),
            Color(0xFF8B5CF6).copy(alpha = 0.65f),
            Color(0xFF30E7F0).copy(alpha = 0.65f)
        )
    )

    // Bottom-nav blur gradient — fades nav into background
    val BottomNavFade = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.55f),
            Color.Black.copy(alpha = 0.92f)
        )
    )

    // Premium progress bar
    val ProgressGradient = Brush.horizontalGradient(
        colors = listOf(Primary, PrimaryLight, AccentPink)
    )
}

// Extra surface helpers
val DarkSurfaceGlassAlt = DarkSurfaceVariant.copy(alpha = 0.55f)
