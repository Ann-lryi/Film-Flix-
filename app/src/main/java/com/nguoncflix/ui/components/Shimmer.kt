package com.nguoncflix.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    shape: RoundedCornerShape = RoundedCornerShape(14.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val offset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    val shimmerColors = listOf(
        NetflixGray.copy(alpha = 0.6f),
        NetflixGray.copy(alpha = 0.25f),
        NetflixGray.copy(alpha = 0.6f)
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(offset - 120f, 0f),
                    end = Offset(offset + 120f, 0f)
                )
            )
    )
}

@Composable
fun ShimmerMovieCard(modifier: Modifier = Modifier) {
    // Matches PremiumMovieCard size
    ShimmerBox(
        modifier = modifier
            .width(140.dp)
            .height(208.dp),
        shape = RoundedCornerShape(14.dp)
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
        
        // Fake hero text shimmer
        Column(
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomStart)
                .padding(24.dp)
        ) {
            ShimmerBox(
                modifier = Modifier
                    .width(220.dp)
                    .height(32.dp),
                shape = RoundedCornerShape(6.dp)
            )
            Spacer(Modifier.height(12.dp))
            ShimmerBox(
                modifier = Modifier
                    .width(140.dp)
                    .height(20.dp),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShimmerBox(
                    modifier = Modifier
                        .width(110.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .width(110.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}
