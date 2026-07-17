package com.nguonc.stream.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// BẢNG MÀU CHỦ ĐẠO SIÊU CẤP ĐIỆN ẢNH (CINEMATIC)
// ==========================================

// Màu Đỏ Rạp Chiếu Phim (Cinematic Red & Neon Accents)
val PrimaryRed = Color(0xFFE50914)
val PrimaryRedLight = Color(0xFFFF3B46)
val PrimaryRedDark = Color(0xFFB80710)
val PrimaryRedContainer = Color(0xFF3B070B)
val OnPrimaryRedContainer = Color(0xFFFFDAD6)

// Điểm nhấn Vàng Kim & Cyan (VIP & Chất lượng cao 4K/FHD)
val GoldStar = Color(0xFFFFC107)
val GoldVip = Color(0xFFFFD700)
val AccentCyan = Color(0xFF00E5FF)
val AccentGreen = Color(0xFF00E676)
val AccentOrange = Color(0xFFFF9100)

// ==========================================
// DARK THEME (Chế độ tối chuẩn rạp phim AMOLED)
// ==========================================
val DarkBackground = Color(0xFF0B0C0E)        // Nền tối sâu chuẩn AMOLED
val DarkSurface = Color(0xFF14161A)           // Bề mặt chính
val DarkSurfaceVariant = Color(0xFF1E2128)    // Bề mặt phụ (Card, Chip, Search)
val DarkSurfaceCard = Color(0xFF181B20)       // Nền thẻ phim Poster
val DarkSurfaceElevated = Color(0xFF242832)   // Nền nổi (Modal, Dropdown, Dialog)
val DarkOutline = Color(0xFF2E333F)           // Viền Card & Divider
val DarkOutlineVariant = Color(0xFF3F4554)    // Viền nhấn nháy khi hover/focus

val OnDarkBackground = Color(0xFFF1F3F7)      // Chữ chính sáng rõ
val OnDarkSurface = Color(0xFFE4E7ED)         // Chữ trên bề mặt
val OnDarkSurfaceVariant = Color(0xFFA6ADC0)  // Chữ phụ, metadata, thời gian

// ==========================================
// LIGHT THEME (Chế độ sáng hiện đại & thanh lịch)
// ==========================================
val LightBackground = Color(0xFFF8F9FB)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFECEFF5)
val LightSurfaceCard = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF2F5FB)
val LightOutline = Color(0xFFD3D8E2)
val LightOutlineVariant = Color(0xFFB8C0D0)

val OnLightBackground = Color(0xFF121418)
val OnLightSurface = Color(0xFF1C1E24)
val OnLightSurfaceVariant = Color(0xFF5E6575)

// ==========================================
// GRADIENT BRUSHES (Hiệu ứng dải màu sang trọng)
// ==========================================
object AppGradients {
    val CinematicRedGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFE50914), Color(0xFFFF3B46))
    )
    
    val CinematicVerticalRedGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFF3B46), Color(0xFFB80710))
    )

    val GoldBadgeGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFC107), Color(0xFFFF9100))
    )

    val CardBottomOverlay = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.4f),
            Color.Black.copy(alpha = 0.85f),
            Color.Black.copy(alpha = 0.95f)
        )
    )

    val HeroTopOverlay = Brush.verticalGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.8f),
            Color.Black.copy(alpha = 0.3f),
            Color.Transparent
        )
    )

    val HeroBottomOverlay = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.5f),
            Color(0xFF0B0C0E)
        )
    )

    val ShimmerGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1E2128),
            Color(0xFF2C303B),
            Color(0xFF1E2128)
        )
    )
}
