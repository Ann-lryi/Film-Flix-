package com.nguonc.stream.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Premium 3.0 Motion System
 *
 * Central catalog of animation specs so screens stop using ad-hoc magic numbers.
 *
 * Design language:
 *  - Press / hover scale  → spring (physical, organic)
 *  - Page-level reveals   → tween with EmphasizedDecel (Material 3 standard)
 *  - Continuous loops     → tween with LinearOutSlowInEasing
 *
 * All easings follow Material 3 motion tokens:
 *  https://m3.material.io/styles/motion/easing-and-duration
 */
object Motion {

    // ---------- Durations (ms) ----------
    const val DurationInstant = 50
    const val DurationXS = 100
    const val DurationS = 180
    const val DurationM = 280
    const val DurationL = 420
    const val DurationXL = 640
    const val DurationXXL = 900

    // ---------- Easings (Material 3 tokens) ----------
    /** Standard ease — incoming elements. Equivalent to Material's "Standard". */
    val Standard: Easing = CubicBezierEasing(0.20f, 0.00f, 0.00f, 1.00f)

    /** Emphasized decelerate — incoming elements that need attention (hero, modal). */
    val EmphasizedDecel: Easing = CubicBezierEasing(0.05f, 0.70f, 0.10f, 1.00f)

    /** Emphasized accelerate — outgoing elements. */
    val EmphasizedAccel: Easing = CubicBezierEasing(0.30f, 0.00f, 0.80f, 0.15f)

    /** Linear out / slow in — for ambient motion, skeleton shimmer drift. */
    val Drift: Easing = LinearOutSlowInEasing

    /** Classic fast-out-slow-in — for any quick toggle. */
    val Snappy: Easing = FastOutSlowInEasing

    // ---------- Spring stiffness presets ----------
    /** Spring for tap-press scale (organically settles back). */
    val PressSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /** Spring for hover/selection state changes (no bounce, snappy). */
    val SnappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /** Spring for ambient pulsing (slow, gentle). */
    val SoftSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    /** Spring used by Pager auto-advance (less aggressive than default). */
    val PagerSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // ---------- Tween spec factories (typed for generic Animatable) ----------
    fun <T> standard(duration: Int = DurationM) = tween<T>(duration, easing = Standard)
    fun <T> emphasized(duration: Int = DurationL) = tween<T>(duration, easing = EmphasizedDecel)
    fun <T> emphasizedAccel(duration: Int = DurationM) = tween<T>(duration, easing = EmphasizedAccel)
    fun <T> drift(duration: Int = DurationL) = tween<T>(duration, easing = Drift)
    fun <T> snappy(duration: Int = DurationS) = tween<T>(duration, easing = Snappy)

    // ---------- Press / hover tokens ----------
    const val PressScale = 0.96f
    const val HoverScale = 1.04f
    const val PressAlpha = 0.86f
    const val SelectedScale = 1.06f
}
