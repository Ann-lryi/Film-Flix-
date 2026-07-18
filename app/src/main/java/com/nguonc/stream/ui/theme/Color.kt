package com.nguonc.stream.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ======================================================
// FILM FLIX 3.0 — AURORA PALETTE
// Hệ màu được tái cấu trúc với 9 cấp OLED + accent brand.
// File này giữ lại các alias tương thích ngược để code cũ
// không bị gãy, đồng thời xuất ra bảng token mới ở Tokens.kt.
// ======================================================

// --- Brand: Cherry Pulse ---
val Primary = BrandCherry
val PrimaryLight = BrandCherrySoft
val PrimaryDark = BrandCherryDeep
val PrimaryMuted = BrandCherryGhost
val PrimaryContainer = BrandCherryInk
val OnPrimaryContainer = BrandCherryLight

// Backwards-compat
val PrimaryRed = Primary
val PrimaryRedLight = PrimaryLight
val PrimaryRedDark = PrimaryDark
val PrimaryRedContainer = PrimaryContainer
val OnPrimaryRedContainer = OnPrimaryContainer
val GoldStar = SunGold
val GoldVip = SunAmber

// --- Secondary: Sun ---
val Secondary = SunGold
val SecondaryMuted = SunGold.copy(alpha = 0.15f)
val SecondaryContainer = Color(0xFF2B2310)

// --- Tertiary / Accent ---
val AccentCyan = AuroraCyan
val AccentViolet = AuroraViolet
val AccentGreen = AuroraLime

// --- DARK THEME — TRUE OLED ---
val DarkBackground = OLED1
val DarkBackgroundSoft = OLED2
val DarkSurface = OLED3
val DarkSurfaceAlt = OLED4
val DarkSurfaceVariant = OLED5
val DarkSurfaceCard = OLED4
val DarkSurfaceElevated = OLED6
val DarkSurfaceGlass = Color(0xFF1A1D2A).copy(alpha = 0.82f)

val DarkOutline = Color(0xFF282A3A)
val DarkOutlineVariant = Color(0xFF3A3D52)
val DarkOutlineGlow = Primary.copy(alpha = 0.25f)

val OnDarkBackground = Ink90
val OnDarkSurface = Ink80
val OnDarkSurfaceVariant = Ink60
val OnDarkSurfaceTertiary = Ink50

// --- LIGHT THEME ---
val LightBackground = Paper1
val LightSurface = Paper0
val LightSurfaceVariant = Paper2
val LightSurfaceCard = Paper0
val LightSurfaceElevated = Paper2
val LightOutline = Paper3
val LightOutlineVariant = Color(0xFFC9CCD8)

val OnLightBackground = PaperInk
val OnLightSurface = Color(0xFF181A22)
val OnLightSurfaceVariant = Color(0xFF6A6E8A)

// --- SEMANTIC ---
val Success = StatusSuccess
val Warning = StatusWarning
val Error = StatusDanger
val Info = StatusInfo

// --- Rating gradient colors ---
val RatingGoldStart = SunGold
val RatingGoldEnd = SunAmber

// ======================================================
// GRADIENTS — giữ tên gọi cũ để Common.kt không vỡ
// (Các gradient mới nằm ở Tokens.kt / object Aurora)
// ======================================================
object AppGradients {
    val PrimaryGradient = Aurora.BrandLinear
    val PrimaryHorizontal = Aurora.BrandLinear
    val PrimaryVertical = Brush.verticalGradient(
        colors = listOf(Color(0xFFFF5470), Color(0xFFB81A36))
    )
    val GoldGradient = Aurora.SunLinear
    val GoldShimmer = Aurora.SunShimmer
    val HeroTopScrim = Aurora.HeroTopScrim
    val HeroBottomScrim = Aurora.HeroBottomScrim
    val HeroSideScrim = Aurora.HeroLeftScrim
    val CardBottomOverlay = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.10f),
            Color.Black.copy(alpha = 0.45f),
            Color.Black.copy(alpha = 0.95f)
        )
    )
    val GlassBorder = Aurora.GlassEdge
    val Shimmer = Aurora.ShimmerSweep
    val AmbientRedGlow = Aurora.AmbientCherry
    val AmbientVioletGlow = Aurora.AmbientViolet
    val ButtonPressed = Brush.linearGradient(
        colors = listOf(Color(0xFFD81F43), Color(0xFFB81A36))
    )
    val MeshGradient = Aurora.MeshPrimary
}

val DarkSurfaceGlassAlt = DarkSurfaceVariant.copy(alpha = 0.6f)
