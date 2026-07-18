package com.nguonc.stream.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium 3.0 Shadow System
 *
 * Principles (lifted from Apple TV+ / Linear / Stripe):
 *  - One ambient shadow (soft, no direction) for grounding
 *  - One key shadow (offset, directional) for depth
 *  - Optional glow shadow (colored, blurred) for primary CTAs / focus
 *
 * Compose's default `Modifier.shadow()` gives us a single shadow. We layer
 * multiple `drawBehind` passes underneath to fake a multi-light setup.
 */
object Elevation {
    val None = 0.dp
    val Micro = 1.dp
    val XS = 2.dp
    val S = 4.dp
    val M = 8.dp
    val L = 12.dp
    val XL = 18.dp
    val XXL = 24.dp
    val Hero = 32.dp
}

/**
 * Soft shadow with tuned ambient + spot colors.
 * Use on cards, surfaces, posters.
 */
fun Modifier.premiumShadow(
    elevation: Dp = Elevation.M,
    shape: Shape = AppShapes.Large,
    ambientAlpha: Float = 0.36f,
    spotAlpha: Float = 0.30f,
    spotY: Dp = 4.dp
): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = Color.Black.copy(alpha = ambientAlpha),
    spotColor = Color.Black.copy(alpha = spotAlpha),
    clip = false
)

/**
 * Multi-layer glow shadow — used on primary CTAs, focused posters, hero.
 *
 * Renders N concentric blurred rectangles beneath the content to simulate
 * a colored key-light + black ambient.
 */
fun Modifier.glowShadow(
    color: Color,
    shape: Shape = AppShapes.Pill,
    glowRadius: Dp = 24.dp,
    elevation: Dp = Elevation.S,
    layers: Int = 3
): Modifier = composed {
    val baseAlpha = 0.45f / layers
    this
        .shadow(elevation, shape, ambientColor = Color.Black.copy(alpha = 0.3f), spotColor = Color.Black.copy(alpha = 0.2f))
        .drawBehind {
            val radiusPx = glowRadius.toPx()
            // Soft colored bloom layers using drawRect with corner radius.
            // This avoids the unstable drawOutline(Paint) API and works on all Compose versions.
            val cornerRadiusPx = if (shape === AppShapes.Pill) size.minDimension / 2f else 16.dp.toPx()
            repeat(layers) { i ->
                val t = (i + 1f) / layers
                val alpha = baseAlpha * (1f - t * 0.5f)
                val inset = radiusPx * (layers - i) / layers * 0.5f
                drawRoundRect(
                    color = color.copy(alpha = alpha),
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(
                        size.width - inset * 2,
                        size.height - inset * 2
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = radiusPx / layers)
                )
            }
        }
}

/**
 * Ambient edge glow — subtle gradient ring around a poster / card.
 * Best on dark backgrounds to add separation without heavy shadow.
 */
fun Modifier.edgeGlow(
    color: Color,
    inset: Dp = 0.dp,
    intensity: Float = 0.18f
): Modifier = composed {
    this.drawBehind {
        val insetPx = inset.toPx()
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = intensity),
                    Color.Transparent
                ),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = size.minDimension / 1.6f
            ),
            topLeft = Offset(insetPx, insetPx),
            size = androidx.compose.ui.geometry.Size(size.width - insetPx * 2, size.height - insetPx * 2)
        )
    }
}

/**
 * Subtle inner top-highlight — gives cards a glossy edge.
 * Use on glass surfaces for that "Apple TV+" sheen.
 */
fun Modifier.topHighlight(
    color: Color = Color.White.copy(alpha = 0.12f),
    height: Dp = 1.dp
): Modifier = composed {
    this.drawBehind {
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    color,
                    Color.Transparent
                )
            ),
            topLeft = Offset.Zero,
            size = androidx.compose.ui.geometry.Size(size.width, height.toPx())
        )
    }
}
