package com.nguoncflix.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nguoncflix.ui.theme.NetflixDarkGray
import com.nguoncflix.ui.theme.NetflixGray

@Composable
fun ShimmerLoadingRow(title: String) {
    Column {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.9f),
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            repeat(5) {
                ShimmerBox(
                    modifier = Modifier
                        .width(130.dp)
                        .height(190.dp),
                    shape = RoundedCornerShape(6.dp)
                )
            }
        }
    }
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = -200f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            NetflixGray,
            NetflixGray.copy(alpha = 0.4f),
            NetflixGray
        ),
        start = Offset(offset - 80f, 0f),
        end = Offset(offset + 80f, 0f)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}
