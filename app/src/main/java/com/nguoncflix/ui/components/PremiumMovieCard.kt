package com.nguoncflix.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nguoncflix.data.models.Movie
import com.nguoncflix.ui.theme.NetflixRed
import com.nguoncflix.ui.theme.NetflixWhite
import com.nguoncflix.ui.theme.NetflixTextSecondary
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Premium movie card inspired by iQIYI, Tencent Video, Netflix.
 */
@Composable
fun PremiumMovieCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 420f),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .width(140.dp)
            .height(208.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 6.dp else 12.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black.copy(0.45f),
                spotColor = Color.Black.copy(0.55f)
            )
            .clickable {
                isPressed = true
                onClick()
                scope.launch {
                    delay(130)
                    isPressed = false
                }
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616))
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = movie.thumbUrl.takeIf { it.isNotBlank() } ?: movie.posterUrl,
                contentDescription = movie.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Cinematic gradient overlay (strong at bottom for text)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.55f),
                                Color.Black.copy(alpha = 0.92f)
                            ),
                            startY = 60f
                        )
                    )
            )

            // Top-right quality badge
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                movie.quality?.takeIf { it.isNotBlank() }?.let { q ->
                    Box(
                        modifier = Modifier
                            .background(NetflixRed, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            q,
                            color = NetflixWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }

            // Top-left series badge
            if (movie.episodeCurrent?.contains("Tập", ignoreCase = true) == true ||
                movie.type == "series"
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(0.6f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        "SERIES",
                        color = NetflixWhite,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Bottom info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = movie.name,
                    color = NetflixWhite,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        lineHeight = 15.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    movie.year?.let {
                        Text(
                            "$it",
                            color = NetflixTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (movie.year != null && !movie.episodeCurrent.isNullOrBlank()) {
                        Text(
                            "  •  ",
                            color = NetflixTextSecondary.copy(0.5f),
                            fontSize = 9.sp
                        )
                    }
                    movie.episodeCurrent?.let { ep ->
                        Text(
                            ep,
                            color = NetflixTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
