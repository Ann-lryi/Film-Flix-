package com.nguoncflix.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Framer Motion-style "whileTap" press animation.
 *
 * THIS IS THE ONLY CORRECT VERSION.
 *
 * - The function is **NOT** annotated with @Composable
 * - We use `composed {}` to get a Compose scope
 *
 * If you see "Unresolved reference 'Composable'" on this file,
 * it means an old/wrong version with @Composable on the function is still in the repo.
 */
fun Modifier.animateScaleOnPress(): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 400f
        ),
        label = "press_scale"
    )

    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable { isPressed = true }
}
