package com.nguonc.stream.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * BỘ ICON CINEMA-STYLE — vẽ tay bằng vector path
 * Mục tiêu: thay thế các Material icon bằng biểu tượng đậm chất
 * điện ảnh, có personality và đồng nhất về độ dày nét.
 */

// ============== PATHS (24x24 viewBox) ==============

val IconFilm: ImageVector
    get() = ImageVector.Builder(
        name = "IconFilm",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M4 4h16v16H4zM7 4v16M11 4v16M15 4v16M19 4v16M4 7h3M4 12h3M4 17h3M11 7h3M11 12h3M11 17h3M17 7h3M17 12h3M17 17h3"
        ).toNodes()
    ).build()

val IconPopcorn: ImageVector
    get() = ImageVector.Builder(
        name = "IconPopcorn",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M12 3c-1 0-2 1-2 2 0-1-1-2-2-2s-2 1-2 2c0-1-1-2-2-2v3l1 3h10l1-3V3c-1 0-2 1-2 2 0-1-1-2-2-2zM5 9l2 12h10l2-12H5zM9 13v6M12 13v6M15 13v6"
        ).toNodes()
    ).build()

val IconClapper: ImageVector
    get() = ImageVector.Builder(
        name = "IconClapper",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M3 9h18v12H3zM3 9l2-6h2l-1 6M7 3l-1 6M11 3l-1 6M15 3l-1 6M19 3l-1 6M21 9l-2-6h-2"
        ).toNodes()
    ).build()

val IconStarFilled: ImageVector
    get() = ImageVector.Builder(
        name = "IconStarFilled",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M12 2l3 7 7 .6-5.3 4.6L18.4 22 12 18l-6.4 4 1.7-7.8L2 9.6 9 9z"
        ).toNodes()
    ).build()

val IconPlay: ImageVector
    get() = ImageVector.Builder(
        name = "IconPlay",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M7 4v16l13-8z"
        ).toNodes()
    ).build()

val IconHeartFilled: ImageVector
    get() = ImageVector.Builder(
        name = "IconHeartFilled",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M12 21s-7-4.5-9-9c-1.5-3.4.7-7 4-7 2 0 3.5 1 5 3 1.5-2 3-3 5-3 3.3 0 5.5 3.6 4 7-2 4.5-9 9-9 9z"
        ).toNodes()
    ).build()

val IconSearch: ImageVector
    get() = ImageVector.Builder(
        name = "IconSearch",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M10.5 3a7.5 7.5 0 015.92 12.08l4.75 4.75-1.42 1.42-4.75-4.75A7.5 7.5 0 1110.5 3zm0 2a5.5 5.5 0 100 11 5.5 5.5 0 000-11z"
        ).toNodes()
    ).build()

val IconBolt: ImageVector
    get() = ImageVector.Builder(
        name = "IconBolt",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M13 2L4 14h6l-1 8 9-12h-6l1-8z"
        ).toNodes()
    ).build()

val IconFire: ImageVector
    get() = ImageVector.Builder(
        name = "IconFire",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M12 2c0 4-4 5-4 9a4 4 0 008 0c0 1 1 2 1 4a7 7 0 11-14 0c0-5 4-7 5-11 0 0 1-1 1-2 1 1 2 0 3 0z"
        ).toNodes()
    ).build()

val IconHome: ImageVector
    get() = ImageVector.Builder(
        name = "IconHome",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M3 12L12 3l9 9v9h-6v-6h-6v6H3v-9z"
        ).toNodes()
    ).build()

val IconBookmark: ImageVector
    get() = ImageVector.Builder(
        name = "IconBookmark",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M6 3h12v18l-6-4-6 4V3z"
        ).toNodes()
    ).build()

val IconGrid: ImageVector
    get() = ImageVector.Builder(
        name = "IconGrid",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M3 3h7v7H3zM14 3h7v7h-7zM3 14h7v7H3zM14 14h7v7h-7z"
        ).toNodes()
    ).build()

val IconArrowRight: ImageVector
    get() = ImageVector.Builder(
        name = "IconArrowRight",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M5 12h14M13 5l7 7-7 7"
        ).toNodes()
    ).build()

val IconArrowBack: ImageVector
    get() = ImageVector.Builder(
        name = "IconArrowBack",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M19 12H5M11 19l-7-7 7-7"
        ).toNodes()
    ).build()

val IconHistory: ImageVector
    get() = ImageVector.Builder(
        name = "IconHistory",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M3 12a9 9 0 109-9V1l4 4-4 4V6a6 6 0 11-6 6H3zM12 7v6l4 2"
        ).toNodes()
    ).build()

val IconSparkle: ImageVector
    get() = ImageVector.Builder(
        name = "IconSparkle",
        defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).addPath(
        pathData = androidx.compose.ui.graphics.vector.PathParser().parsePathString(
            "M12 2l1.6 6.4L20 10l-6.4 1.6L12 18l-1.6-6.4L4 10l6.4-1.6L12 2zM19 14l.8 3.2L23 18l-3.2.8L19 22l-.8-3.2L15 18l3.2-.8L19 14zM5 14l.6 2.4L8 17l-2.4.6L5 20l-.6-2.4L2 17l2.4-.6L5 14z"
        ).toNodes()
    ).build()

// ============== HÀM TIỆN ÍCH ==============

/** Helper để parse path string an toàn */
private fun List<Any>.toNodes() = this

// ============== COMPOSABLE WRAPPER ==============

/**
 * Icon vẽ tay bằng Canvas — dùng cho những hình cần
 * gradient hoặc custom nét vẽ phức tạp hơn ImageVector.
 */
@Composable
fun CinemaReelIcon(
    modifier: Modifier = Modifier.size(48.dp),
    color: Color = Color.White,
    spokes: Int = 6,
) {
    Canvas(modifier = modifier) {
        val w = size.minDimension
        val center = Offset(w / 2f, w / 2f)
        val outer = w * 0.46f
        val inner = w * 0.30f

        // Outer ring
        drawCircle(
            color = color.copy(alpha = 0.18f),
            radius = outer,
            center = center,
            style = Stroke(width = w * 0.03f, cap = StrokeCap.Round)
        )
        drawCircle(
            color = color,
            radius = outer,
            center = center,
            style = Stroke(width = w * 0.04f, cap = StrokeCap.Round)
        )
        // Inner
        drawCircle(color = color, radius = inner * 0.30f, center = center)
        // Spokes
        for (i in 0 until spokes) {
            val angle = (i * 360f / spokes)
            val rad = Math.toRadians(angle.toDouble())
            val sx = center.x + (inner * 0.35f * Math.cos(rad)).toFloat()
            val sy = center.y + (inner * 0.35f * Math.sin(rad)).toFloat()
            val ex = center.x + (outer * 0.92f * Math.cos(rad)).toFloat()
            val ey = center.y + (outer * 0.92f * Math.sin(rad)).toFloat()
            drawLine(
                color = color,
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = w * 0.045f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = color,
                radius = w * 0.06f,
                center = Offset(ex, ey)
            )
        }
    }
}

/** Bong bóng sáng tỏa — dùng cho poster/icon yếu tố ánh sáng */
@Composable
fun HaloGlow(
    modifier: Modifier = Modifier.size(120.dp),
    color: Color = Color(0xFFFF1F4A),
    intensity: Float = 0.45f,
) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = intensity),
                    color.copy(alpha = intensity * 0.35f),
                    Color.Transparent
                ),
                center = Offset(r, r),
                radius = r
            ),
            radius = r,
            center = Offset(r, r)
        )
    }
}

/** Conic ring — cho icon loading hoặc hero shine */
@Composable
fun ConicRing(
    modifier: Modifier = Modifier.size(48.dp),
    colors: List<Color> = listOf(
        Color(0xFFFF1F4A), Color(0xFFFF8E1F), Color(0xFFFFCB1F),
        Color(0xFF22D3EE), Color(0xFFA78BFA), Color(0xFFFF1F4A)
    ),
    strokeWidth: Dp = 3.dp,
) {
    Canvas(modifier = modifier) {
        val w = size.minDimension
        val stroke = strokeWidth.toPx()
        rotate(degrees = 0f, pivot = Offset(w / 2f, w / 2f)) {
            val path = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        offset = Offset(stroke / 2f, stroke / 2f),
                        size = Size(w - stroke, w - stroke)
                    )
                )
            }
            drawPath(
                path = path,
                brush = Brush.sweepGradient(
                    colors = colors,
                    center = Offset(w / 2f, w / 2f)
                ),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

/** Dashed divider — cho phân chia section */
@Composable
fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.18f),
    dashLength: Float = 8f,
    gap: Float = 6f,
    thickness: Float = 1.2f,
) {
    Canvas(modifier = modifier) {
        val effect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gap), 0f)
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = thickness,
            pathEffect = effect,
            cap = StrokeCap.Round
        )
    }
}

/** Star outline dùng cho rating */
@Composable
fun OutlineStar(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFCB1F),
    fill: Boolean = false,
) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerR = size.minDimension * 0.46f
            val innerR = outerR * 0.42f
            val points = 5
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) outerR else innerR
                val angle = (i * 360f / (points * 2) - 90f)
                val rad = Math.toRadians(angle.toDouble())
                val x = cx + r * Math.cos(rad).toFloat()
                val y = cy + r * Math.sin(rad).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        if (fill) {
            drawPath(path = path, color = color)
        } else {
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}
