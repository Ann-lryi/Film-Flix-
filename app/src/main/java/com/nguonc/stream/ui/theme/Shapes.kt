package com.nguonc.stream.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium 3.0 Shape System
 *
 * Design language:
 *  - Cards        → 24dp (large)
 *  - Sheets/dialogs → 32dp (extraLarge)
 *  - Chips        → 100% (full pill)
 *  - Buttons      → 16dp (medium) or pill (100%)
 *  - Posters      → 20dp (large)
 *  - Squircle     → Custom superellipse shape for app-icon-style containers
 */
object AppShapes {
    val None = RoundedCornerShape(0.dp)
    val Micro = RoundedCornerShape(4.dp)
    val XSmall = RoundedCornerShape(8.dp)
    val Small = RoundedCornerShape(12.dp)
    val Medium = RoundedCornerShape(16.dp)
    val Large = RoundedCornerShape(20.dp)
    val XLarge = RoundedCornerShape(24.dp)
    val XXLarge = RoundedCornerShape(28.dp)
    val Huge = RoundedCornerShape(32.dp)
    val Pill = RoundedCornerShape(100.dp)
    val Circle = RoundedCornerShape(percent = 50)
    val Squircle = SquircleShape(curvature = 0.65f)
    val SquircleSoft = SquircleShape(curvature = 0.5f)
    val SquirclePillow = SquircleShape(curvature = 0.8f)
}

/** Material 3 shape tokens — used via MaterialTheme.shapes */
val AppMaterialShapes = Shapes(
    extraSmall = AppShapes.XSmall,
    small = AppShapes.Small,
    medium = AppShapes.Medium,
    large = AppShapes.Large,
    extraLarge = AppShapes.XXLarge
)

/**
 * Squircle (superellipse) shape — Apple-style "pillow" rounded square.
 * Replicates the iOS app-icon corner curve so square assets look polished,
 * not pixel-perfect rounded.
 *
 * @param curvature 0.0 = full circle, 1.0 = sharp square, ~0.6 = iOS app-icon feel
 */
class SquircleShape(
    private val curvature: Float = 0.6f
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): Outline {
        // We approximate the superellipse x^n + y^n = r^n with n = 2 / (1 - curvature + 0.0001)
        // by composing many small line segments. The result is a closed SmoothPath.
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return Outline.Rectangle(Rect(Offset.Zero, size))

        val n = (1.0f / (1.0f - curvature)).coerceIn(2.0f, 12.0f).toDouble()
        val rx = w / 2.0
        val ry = h / 2.0
        val cx = rx
        val cy = ry

        val path = androidx.compose.ui.graphics.Path().apply {
            val steps = 64
            for (i in 0..steps) {
                val theta = (Math.PI * 2.0 * i / steps)
                val cosT = Math.cos(theta)
                val sinT = Math.sin(theta)
                val x = (cx + rx * Math.signum(cosT) * Math.pow(Math.abs(cosT), 2.0 / n)).toFloat()
                val y = (cy + ry * Math.signum(sinT) * Math.pow(Math.abs(sinT), 2.0 / n)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean = other is SquircleShape && other.curvature == curvature
    override fun hashCode(): Int = curvature.hashCode()
    override fun toString(): String = "SquircleShape(curvature=$curvature)"
}
