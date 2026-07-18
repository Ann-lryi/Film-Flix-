package com.aho.yunphim.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Bảng màu lấy cảm hứng từ app xem phim Trung Quốc (iQIYI/WeTV/Youku): nền gần như đen tuyệt
 * đối để video là tâm điểm, accent đỏ rực cho CTA/tiêu điểm, vàng ánh kim cho chi tiết "cao cấp"
 * (rating, badge chất lượng). Toàn app chỉ có 1 theme tối - không cần theo system light/dark vì
 * bản chất app xem phim luôn tối để tập trung vào hình ảnh.
 */
object YunPhimColors {
    val Background = Color(0xFF0A0A0D)
    val Surface = Color(0xFF16161C)
    val SurfaceVariant = Color(0xFF1F1F27)
    val Accent = Color(0xFFE8384F)
    val AccentVariant = Color(0xFFFF5A6E)
    val Gold = Color(0xFFF0B429)
    val TextPrimary = Color(0xFFF5F5F7)
    val TextSecondary = Color(0xFF9A9AA5)
    val Divider = Color(0xFF2A2A33)
    val Error = Color(0xFFFF6B6B)
    val ScrimTop = Color(0xCC0A0A0D)
    val ScrimTransparent = Color(0x000A0A0D)
}

private val YunPhimDarkScheme = darkColorScheme(
    primary = YunPhimColors.Accent,
    onPrimary = Color.White,
    secondary = YunPhimColors.Gold,
    onSecondary = Color.Black,
    background = YunPhimColors.Background,
    onBackground = YunPhimColors.TextPrimary,
    surface = YunPhimColors.Surface,
    onSurface = YunPhimColors.TextPrimary,
    surfaceVariant = YunPhimColors.SurfaceVariant,
    onSurfaceVariant = YunPhimColors.TextSecondary,
    error = YunPhimColors.Error,
    outline = YunPhimColors.Divider,
)

private val YunPhimTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp),
)

@Composable
fun YunPhimTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = YunPhimDarkScheme,
        typography = YunPhimTypography,
        content = content,
    )
}
