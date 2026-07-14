package com.nguoncflix.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nguoncflix.ui.theme.NetflixDarkGray
import com.nguoncflix.ui.theme.NetflixGray

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val offset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    val shimmerColors = listOf(
        NetflixGray,
        NetflixGray.copy(alpha = 0.3f),
        NetflixGray
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(offset - 100f, 0f),
                    end = Offset(offset + 100f, 0f)
                )
            )
    )
}

@Composable
fun ShimmerMovieCard(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier
            .width(130.dp)
            .height(190.dp),
        shape = RoundedCornerShape(4.dp)
    )
}

@Composable
fun ShimmerHeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
            .background(NetflixDarkGray)
    ) {
        ShimmerBox(
            modifier = Modifier.fillMaxSize(),
            height = 520.dp,
            shape = RoundedCornerShape(0.dp)
        )
    }
}
