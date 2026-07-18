package com.nguonc.stream.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * FILM FLIX 3.0 — AURORA CINEMATIC
 *
 * Hệ thống token nâng cấp toàn diện, lấy cảm hứng từ:
 *  - Linear / Apple TV+ (typography & density)
 *  - Disney+ / HBO Max (cinematic depth)
 *  - Behance / Vercel (precision micro-detail)
 *
 * Triết lý:
 *  1. Mỗi gradient phải kể câu chuyện
 *  2. Bóng đổ có hướng và tầng, không lan tràn
 *  3. Mọi chuyển động đều có "trọng lực" — easing cong, không thẳng
 *  4. Glass morphism có 3 lớp (highlight → tint → edge)
 *  5. Màu sắc tạo phân cấp từ đen OLED → đỏ ánh cherry → vàng mặt trời
 */

// ============== BẢNG MÀU MỞ RỘNG ==============

// Brand
val BrandCherry       = Color(0xFFFF1F4A)
val BrandCherrySoft   = Color(0xFFFF5470)
val BrandCherryDeep   = Color(0xFFCC0E36)
val BrandCherryGhost  = Color(0xFFFF1F4A).copy(alpha = 0.10f)
val BrandCherryInk    = Color(0xFF1A050D)
val BrandCherryLight  = Color(0xFFFFD1D8)

// Sun
val SunGold           = Color(0xFFFFCB1F)
val SunAmber         = Color(0xFFFF8E1F)
val SunCream         = Color(0xFFFFF1C2)

// Aurora
val AuroraCyan        = Color(0xFF22D3EE)
val AuroraViolet      = Color(0xFFA78BFA)
val AuroraPink        = Color(0xFFEC4899)
val AuroraLime        = Color(0xFF34F5A8)

// Status
val StatusSuccess     = Color(0xFF00E08A)
val StatusWarning     = Color(0xFFFFB020)
val StatusInfo        = AuroraCyan
val StatusDanger      = BrandCherry

// Dark surfaces — 9 cấp độ sáng tinh tế
val OLED0             = Color(0xFF000000)   // Đen tuyệt đối
val OLED1             = Color(0xFF050608)   // Nền cinema
val OLED2             = Color(0xFF0A0B10)   // Soft lift
val OLED3             = Color(0xFF101218)   // Surface 1
val OLED4             = Color(0xFF161822)   // Surface 2 — Card
val OLED5             = Color(0xFF1C1E2B)   // Surface 3 — Chip
val OLED6             = Color(0xFF232636)   // Surface 4 — Elevated
val OLED7             = Color(0xFF2B2E40)   // Modal BG
val OLED8             = Color(0xFF3A3D52)   // Outline strong
val OLED9             = Color(0xFF4F536B)   // Outline bright

// Text
val InkPure           = Color(0xFFFFFFFF)
val Ink90             = Color(0xFFF3F4F9)
val Ink80             = Color(0xFFDEE0EC)
val Ink70             = Color(0xFFB8BBD0)
val Ink60             = Color(0xFF8A8FA8)
val Ink50             = Color(0xFF616680)
val Ink40             = Color(0xFF454962)
val Ink30             = Color(0xFF2E3146)

// Light
val Paper0            = Color(0xFFFFFFFF)
val Paper1            = Color(0xFFFBFBFD)
val Paper2            = Color(0xFFF4F4F8)
val Paper3            = Color(0xFFE9EAF0)
val PaperInk          = Color(0xFF0B0D14)

// ============== HỆ GRADIENT AURORA ==============

object Aurora {
    // Hero/Brand — đỏ cherry ánh tím
    val BrandLinear = Brush.linearGradient(
        colors = listOf(Color(0xFFFF1F4A), Color(0xFFFF5470), Color(0xFFFF8FA8))
    )
    val BrandSweep = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF1F4A),
            Color(0xFFFF3A6A),
            Color(0xFFFF6B8B),
            Color(0xFFFF3A6A),
            Color(0xFFFF1F4A)
        )
    )
    val BrandRadial = Brush.radialGradient(
        colors = listOf(Color(0xFFFF1F4A).copy(alpha = 0.45f), Color.Transparent),
        radius = 900f
    )
    val BrandGlow = Brush.radialGradient(
        colors = listOf(
            Color(0xFFFF1F4A).copy(alpha = 0.55f),
            Color(0xFFFF1F4A).copy(alpha = 0.15f),
            Color.Transparent
        )
    )

    // Sun — vàng champagne
    val SunLinear = Brush.linearGradient(
        colors = listOf(Color(0xFFFFCB1F), Color(0xFFFF8E1F))
    )
    val SunShimmer = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFCB1F),
            Color(0xFFFFF1C2),
            Color(0xFFFFCB1F)
        )
    )

    // Cinematic scrims
    val HeroTopScrim = Brush.verticalGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.92f),
            Color.Black.copy(alpha = 0.55f),
            Color.Black.copy(alpha = 0.10f),
            Color.Transparent
        )
    )
    val HeroBottomScrim = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.05f),
            Color.Black.copy(alpha = 0.55f),
            Color.Black.copy(alpha = 0.88f),
            Color.Black.copy(alpha = 0.98f)
        )
    )
    val HeroLeftScrim = Brush.horizontalGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.85f),
            Color.Black.copy(alpha = 0.35f),
            Color.Transparent,
            Color.Transparent
        )
    )
    val HeroRadialSpot = Brush.radialGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.0f),
            Color.Black.copy(alpha = 0.65f)
        )
    )

    // Glass — 3 lớp
    val GlassEdge = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.28f),
            Color.White.copy(alpha = 0.06f),
            Color.White.copy(alpha = 0.02f)
        )
    )
    val GlassTop = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.10f),
            Color.White.copy(alpha = 0.02f),
            Color.Transparent
        )
    )
    val GlassBottom = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.20f),
            Color.Black.copy(alpha = 0.40f)
        )
    )
    val GlassBody = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A1D2A).copy(alpha = 0.85f),
            Color(0xFF0E1019).copy(alpha = 0.92f)
        )
    )

    // Mesh gradient — cho hero CTAs
    val MeshPrimary = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF1F4A).copy(alpha = 0.95f),
            Color(0xFFA78BFA).copy(alpha = 0.85f),
            Color(0xFF22D3EE).copy(alpha = 0.85f)
        )
    )
    val MeshWarm = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF1F4A),
            Color(0xFFFF8E1F),
            Color(0xFFFFCB1F)
        )
    )
    val MeshCool = Brush.linearGradient(
        colors = listOf(
            Color(0xFF22D3EE),
            Color(0xFFA78BFA),
            Color(0xFFEC4899)
        )
    )

    // Ambient — background blobs
    val AmbientCherry = Brush.radialGradient(
        colors = listOf(
            Color(0xFFFF1F4A).copy(alpha = 0.30f),
            Color(0xFFFF1F4A).copy(alpha = 0.06f),
            Color.Transparent
        )
    )
    val AmbientViolet = Brush.radialGradient(
        colors = listOf(
            Color(0xFFA78BFA).copy(alpha = 0.25f),
            Color(0xFFA78BFA).copy(alpha = 0.05f),
            Color.Transparent
        )
    )
    val AmbientCyan = Brush.radialGradient(
        colors = listOf(
            Color(0xFF22D3EE).copy(alpha = 0.22f),
            Color(0xFF22D3EE).copy(alpha = 0.04f),
            Color.Transparent
        )
    )

    // Shimmer skeleton
    val ShimmerSweep = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1C1E2B),
            Color(0xFF2A2E44),
            Color(0xFF383D58),
            Color(0xFF2A2E44),
            Color(0xFF1C1E2B)
        )
    )

    // Conic
    fun conic(sizePx: Float, sweep: Float = 360f, colors: List<Color>): Brush {
        return Brush.sweepGradient(
            colors = colors,
            center = androidx.compose.ui.geometry.Offset(sizePx / 2f, sizePx / 2f)
        )
    }
}

// ============== SHADOW TIERS — Hệ bóng có hướng ==============

object Shadow {
    val None = androidx.compose.ui.graphics.shadow.Shadow(
        color = Color.Transparent, offsetX = 0f, offsetY = 0f, blurRadius = 0f
    )
}

/** Bóng đổ cao cấp — soft, layered, có hướng */
data class LuxShadow(
    val elevation: Dp,
    val color: Color,
    val spread: Dp = 0.dp,
    val yOffset: Dp = elevation / 2f
)

// ============== MOTION TOKENS ==============

object Motion {
    const val FAST = 180
    const val NORMAL = 260
    const val SLOW = 420
    const val SLOWER = 600
    const val PAGE = 700

    // Cubic-bezier-ish easing constants
    const val EASE_OUT_EXPO = 1
    const val EASE_OUT_BACK = 2
    const val EASE_IN_OUT = 3
}

/** Shapes hệ thống — dùng nhất quán toàn app */
object FilmShapes {
    val xs = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    val sm = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    val md = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    val lg = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    val xl = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
    val xxl = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
    val xxxl = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
    val pill = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
    val asymmetric = androidx.compose.foundation.shape.RoundedCornerShape(
        topStart = 24.dp, topEnd = 24.dp, bottomEnd = 0.dp, bottomStart = 0.dp
    )
    val card = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
}

// ============== SPACING ==============

object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 48.dp
    val mega = 64.dp
}

// ============== GLOW / NEON EFFECTS ==============

object Glow {
    val cherry = Color(0xFFFF1F4A).copy(alpha = 0.55f)
    val cherrySoft = Color(0xFFFF1F4A).copy(alpha = 0.30f)
    val gold = Color(0xFFFFCB1F).copy(alpha = 0.45f)
    val violet = Color(0xFFA78BFA).copy(alpha = 0.40f)
    val cyan = Color(0xFF22D3EE).copy(alpha = 0.35f)
}

// ============== PREMIUM NOISE PATTERN ==============

/** Pattern noise tinh tế cho glass — texture mịn 4% opacity */
val NoisePattern: Brush = Brush.linearGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.025f),
        Color.White.copy(alpha = 0.012f),
        Color.White.copy(alpha = 0.025f)
    ),
    tileMode = TileMode.Repeated
)
