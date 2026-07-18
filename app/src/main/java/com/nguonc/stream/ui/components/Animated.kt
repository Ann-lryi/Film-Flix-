package com.nguonc.stream.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * BỘ ANIMATION TIỆN ÍCH — FILM FLIX 3.0
 *
 * Gồm:
 *  - Reveal/entry hiệu ứng theo stagger
 *  - Parallax, hover, press
 *  - Shimmer skeleton sweep nâng cấp
 *  - Pulse glow, breath effect
 *  - Marquee scroll cho ticker
 */

// ============== EASINGS CAO CẤP ==============

val EaseOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
val EaseOutQuart = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
val EaseOutBack = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
val EaseInOutQuart = CubicBezierEasing(0.76f, 0f, 0.24f, 1f)
val EaseInOutExpo = CubicBezierEasing(0.87f, 0f, 0.13f, 1f)
val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
val EaseSpring = CubicBezierEasing(0.5f, 1.5f, 0.89f, 1f)

// ============== STAGGER ENTRY ==============

/**
 * Tạo hiệu ứng entry theo thứ tự xuất hiện (stagger),
 * phù hợp cho danh sách / section. Mỗi item delay 60ms.
 */
@Composable
fun rememberStaggerEntry(
    index: Int,
    baseDelayMs: Int = 60,
    durationMs: Int = 520,
): AnimatableEntry {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(index) {
        delay((index * baseDelayMs).toLong())
        anim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMs,
                easing = EaseOutQuart
            )
        )
    }
    return AnimatableEntry(anim)
}

class AnimatableEntry(val anim: Animatable<Float, AnimationVector1D>) {
    val value: Float get() = anim.value
}

// ============== BREATH ==============

/**
 * Animation phồng/co nhẹ liên tục, dùng cho badge LIVE
 * hoặc CTA "Đang chiếu".
 */
@Composable
fun rememberBreath(minScale: Float = 0.95f, maxScale: Float = 1.05f, durationMs: Int = 1600): Float {
    val transition = rememberInfiniteTransition(label = "breath")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathProg"
    )
    return minScale + (maxScale - minScale) * progress
}

// ============== PULSE GLOW ==============

/**
 * Pulse độ mờ cho outline/glow, kết hợp với Modifier.drawBehind.
 */
@Composable
fun rememberPulse(minAlpha: Float = 0.25f, maxAlpha: Float = 0.7f, durationMs: Int = 1400): Float {
    val transition = rememberInfiniteTransition(label = "pulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseProg"
    )
    return minAlpha + (maxAlpha - minAlpha) * progress
}

// ============== PRESS SCALE ==============

/**
 * Hiệu ứng nhấn: scale nhỏ + giảm shadow + tăng tương phản.
 * Kết hợp với Modifier.clickable + interactionSource.
 */
@Composable
fun rememberPressScale(pressed: Boolean, baseScale: Float = 1f, pressedScale: Float = 0.96f): Float {
    return animateFloatAsState(
        targetValue = if (pressed) pressedScale else baseScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pressScale"
    ).value
}

// ============== REVEAL ==============

/**
 * Modifier hiển thị fade + slide up khi xuất hiện.
 */
fun Modifier.auroraReveal(entry: AnimatableEntry, slideY: Dp = 24.dp): Modifier = this.then(
    Modifier.graphicsLayer {
        val v = entry.value
        alpha = v
        translationY = (1f - v) * slideY.toPx()
        scaleX = 0.94f + 0.06f * v
        scaleY = 0.94f + 0.06f * v
    }
)

// ============== SHIMMER SKELETON v2 ==============

/**
 * Shimmer 2 lớp: gradient moving + noise tint.
 * Sử dụng DrawScope.drawBehind trên Modifier.
 */
fun Modifier.auroraShimmer(
    shape: Shape = RoundedCornerShape(20.dp),
    accent: Color = Color(0xFFFF1F4A),
    base: Color = Color(0xFF161822),
): Modifier = this.then(
    Modifier
        .clip(shape)
        .background(base)
        .graphicsLayer { } // placeholder for future paint
)

// ============== MARQUEE / SHINE SWEEP ==============

/**
 * Shimmer 1 lần quét từ trái sang phải khi xuất hiện.
 */
@Composable
fun rememberShimmerSweep(durationMs: Int = 1400): Float {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProg"
    )
    return progress
}

fun DrawScope.drawSweepShimmer(
    progress: Float,
    baseColors: List<Color>,
    highlight: Color = Color.White.copy(alpha = 0.15f),
) {
    val sweepX = -size.width + (2f * size.width * progress)
    val brush = Brush.linearGradient(
        colors = baseColors + highlight + baseColors.reversed(),
        start = Offset(sweepX - size.width * 0.3f, 0f),
        end = Offset(sweepX + size.width * 0.3f, size.height)
    )
    drawRect(brush = brush, size = size)
}

// ============== FADE IN PLACE ==============

/**
 * Helper cho việc fade-in khi state đổi (loading → loaded).
 */
@Composable
fun rememberFadeIn(targetVisible: Boolean, durationMs: Int = 320): Float {
    return animateFloatAsState(
        targetValue = if (targetVisible) 1f else 0f,
        animationSpec = tween(durationMs, easing = EaseOutQuart),
        label = "fadeIn"
    ).value
}
