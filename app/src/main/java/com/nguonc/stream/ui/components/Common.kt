package com.nguonc.stream.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nguonc.stream.data.remote.dto.MovieItemDto
import com.nguonc.stream.ui.theme.Aurora
import com.nguonc.stream.ui.theme.BrandCherry
import com.nguonc.stream.ui.theme.BrandCherrySoft
import com.nguonc.stream.ui.theme.Glow
import com.nguonc.stream.ui.theme.GoldStar
import com.nguonc.stream.ui.theme.OLED4
import com.nguonc.stream.ui.theme.OLED5
import com.nguonc.stream.ui.theme.OLED8
import com.nguonc.stream.ui.theme.OnDarkSurfaceTertiary
import com.nguonc.stream.ui.theme.OnDarkSurfaceVariant
import com.nguonc.stream.ui.theme.PremiumText
import com.nguonc.stream.ui.theme.Primary
import com.nguonc.stream.ui.theme.SunAmber
import com.nguonc.stream.ui.theme.SunGold
import kotlinx.coroutines.delay
import java.util.Locale

// ======================================================
// POSTER CARD 3.0 — Aurora Edition
// Glass tint, gradient stroke, micro-interaction, ratings
// ======================================================
@Composable
fun MoviePosterCard(
    movie: MovieItemDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    aspect: Float = 2f / 3f,
    cornerRadius: Dp = 22.dp,
    showRating: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "posterScale"
    )
    val elevation by animateFloatAsState(
        targetValue = if (pressed) 4f else 14f,
        animationSpec = tween(180),
        label = "elevation"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
    ) {
        Card(
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.cardColors(containerColor = OLED4),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.10f),
                        Color.White.copy(alpha = 0.04f),
                        Color.White.copy(alpha = 0.10f)
                    )
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = elevation.dp,
                    shape = RoundedCornerShape(cornerRadius),
                    ambientColor = Color.Black.copy(alpha = 0.55f),
                    spotColor = Color.Black.copy(alpha = 0.45f)
                )
        ) {
            Box {
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspect)
                        .background(OLED5),
                    contentScale = ContentScale.Crop,
                )

                // Top highlight — glass reflection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Bottom scrim — text safe area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .align(Alignment.BottomCenter)
                        .background(Aurora.CardBottomOverlay)
                )

                // Top badges row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        movie.quality?.takeIf { it.isNotBlank() }?.let {
                            PremiumQualityBadge(text = it.uppercase())
                        }
                        if (movie.year >= 2024) NewBadge()
                    }
                    if (showRating && (movie.tmdb?.voteAverage ?: 0.0) > 0) {
                        RatingPill(vote = movie.tmdb?.voteAverage ?: 0.0)
                    }
                }

                // Bottom — year + play
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (movie.year > 0) {
                        GlassChip(text = movie.year.toString())
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .border(
                                BorderStroke(0.5.dp, Color.White.copy(alpha = 0.18f)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (showTitle) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = movie.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.1).sp
                ),
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (movie.originName.isNotBlank() && movie.originName != movie.name) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = movie.originName,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnDarkSurfaceTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ======================================================
// BADGES
// ======================================================
@Composable
fun PremiumQualityBadge(text: String, modifier: Modifier = Modifier) {
    val isUltra = text.contains("4K") || text.contains("FHD") || text.contains("IMAX") ||
            text.contains("HDR") || text.contains("2K")
    val border = if (isUltra) SunGold.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.22f)
    val textColor = if (isUltra) SunGold else Color.White
    val bg = if (isUltra) Color(0xFF1A1505).copy(alpha = 0.78f)
             else Color.Black.copy(alpha = 0.55f)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = modifier.shadow(6.dp, RoundedCornerShape(8.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isUltra) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Aurora.SunLinear)
                )
            }
            Text(
                text = text,
                style = PremiumText.Badge,
                color = textColor,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun CapsuleBadge(text: String, modifier: Modifier = Modifier) = PremiumQualityBadge(text, modifier)

@Composable
fun NewBadge(modifier: Modifier = Modifier) {
    val breath = rememberBreath(minScale = 0.95f, maxScale = 1.05f, durationMs = 1500)
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color.Transparent,
        modifier = modifier
            .scale(breath)
            .shadow(8.dp, RoundedCornerShape(100.dp), ambientColor = BrandCherry.copy(alpha = 0.7f))
            .clip(RoundedCornerShape(100.dp))
            .background(Aurora.BrandLinear)
    ) {
        Text(
            text = "MỚI",
            style = PremiumText.Badge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun GlassChip(text: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.50f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.18f)),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp)
        )
    }
}

@Composable
fun RatingPill(vote: Double, modifier: Modifier = Modifier) {
    if (vote <= 0) return
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.65f),
        border = BorderStroke(0.5.dp, SunGold.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = SunGold,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = String.format(Locale.US, "%.1f", vote),
                style = PremiumText.Badge,
                color = SunGold,
            )
        }
    }
}

// ======================================================
// HERO BANNER — 3.0 với parallax, 3D depth
// ======================================================
@Composable
fun HeroBannerCarousel(
    movies: List<MovieItemDto>,
    onMovieClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (movies.isEmpty()) return
    val take = movies.take(6)
    val pagerState = rememberPagerState(pageCount = { take.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(5500)
            val next = (pagerState.currentPage + 1) % take.size
            try {
                pagerState.animateScrollToPage(
                    page = next,
                    animationSpec = tween(700, easing = FastOutSlowInEasing)
                )
            } catch (_: Exception) {}
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 14.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp)
        ) { page ->
            val movie = take[page]
            val isCurrent = pagerState.currentPage == page
            val scale by animateFloatAsState(
                targetValue = if (isCurrent) 1f else 0.92f,
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "heroScale"
            )

            Card(
                shape = RoundedCornerShape(30.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 24.dp else 6.dp),
                border = BorderStroke(
                    1.dp,
                    if (isCurrent) Color.White.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.06f)
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .graphicsLayer { clip = true }
                    .clickable { onMovieClick(movie.slug) }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Backdrop image
                    AsyncImage(
                        model = movie.thumbUrl.ifBlank { movie.posterUrl },
                        contentDescription = movie.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Bottom scrim
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .align(Alignment.BottomCenter)
                            .background(Aurora.HeroBottomScrim)
                    )
                    // Top scrim
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .align(Alignment.TopCenter)
                            .background(Aurora.HeroTopScrim)
                    )
                    // Side vignette
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.35f),
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.18f)
                                    )
                                )
                            )
                    )

                    // Top row: rank + quality
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Top10Badge(rank = page + 1)
                        PremiumQualityBadge(text = (movie.quality ?: "FHD").uppercase())
                    }

                    // Bottom content
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Rating row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (movie.tmdb?.voteAverage ?: 0.0).takeIf { it > 0 }?.let { vote ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = SunGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%.1f", vote),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = SunGold
                                    )
                                    Text(
                                        text = "/10",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.55f)
                                    )
                                }
                            }
                            if (movie.year > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.4f))
                                )
                                Text(
                                    text = "${movie.year}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = movie.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (movie.originName.isNotBlank()) {
                            Text(
                                text = movie.originName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.70f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // CTA row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HeroPlayButton(
                                onClick = { onMovieClick(movie.slug) }
                            )
                            HeroGhostButton(
                                onClick = { onMovieClick(movie.slug) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Pill indicator — animated
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            take.forEachIndexed { index, _ ->
                val isSelected = pagerState.currentPage == index
                val width by animateFloatAsState(
                    targetValue = if (isSelected) 32f else 6f,
                    animationSpec = tween(420, easing = FastOutSlowInEasing),
                    label = "dotW"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width.dp, 6.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            if (isSelected) Aurora.BrandLinear
                            else Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.18f))
                            )
                        )
                        .graphicsLayer {
                            if (isSelected) {
                                shadowElevation = 8f
                                shape = RoundedCornerShape(100.dp)
                                clip = true
                            }
                        }
                )
            }
        }
    }
}

@Composable
private fun Top10Badge(rank: Int) {
    val breath = rememberBreath(minScale = 0.95f, maxScale = 1.05f, durationMs = 1500)
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color.Transparent,
        modifier = Modifier
            .scale(breath)
            .shadow(10.dp, RoundedCornerShape(100.dp), ambientColor = Glow.cherry)
            .clip(RoundedCornerShape(100.dp))
            .background(Aurora.BrandLinear)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Text(
                text = "#${rank} TOP HÔM NAY",
                style = PremiumText.Badge,
                color = Color.White
            )
        }
    }
}

@Composable
private fun HeroPlayButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(180),
        label = "heroPlayScale"
    )
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color.White,
        modifier = Modifier
            .scale(scale)
            .shadow(16.dp, RoundedCornerShape(100.dp), ambientColor = Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Xem ngay",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                color = Color.Black
            )
        }
    }
}

@Composable
private fun HeroGhostButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(180),
        label = "heroGhostScale"
    )
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
        modifier = Modifier
            .size(46.dp)
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ======================================================
// SECTION HEADER — Minimal premium
// ======================================================
@Composable
fun SectionHeader(
    title: String,
    onSeeMore: (() -> Unit)?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    accent: Brush = Aurora.BrandLinear,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(accent)
                    .shadow(6.dp, RoundedCornerShape(100.dp), ambientColor = BrandCherry.copy(alpha = 0.6f))
            )
            if (icon != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BrandCherry.copy(alpha = 0.16f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = BrandCherry,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = OnDarkSurfaceVariant
                    )
                }
            }
        }

        if (onSeeMore != null) {
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .clickable(onClick = onSeeMore)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Tất cả",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ======================================================
// RATING / META CHIPS
// ======================================================
@Composable
fun TmdbRating(vote: Double, modifier: Modifier = Modifier, large: Boolean = false) {
    if (vote <= 0.0) return
    Surface(
        shape = RoundedCornerShape(if (large) 12.dp else 10.dp),
        color = Color(0xFF1A1705).copy(alpha = 0.85f),
        border = BorderStroke(1.dp, SunGold.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = if (large) 10.dp else 8.dp,
                vertical = if (large) 6.dp else 4.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = GoldStar,
                modifier = Modifier.size(if (large) 16.dp else 14.dp),
            )
            Text(
                text = String.format(Locale.US, "%.1f", vote),
                style = if (large) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelMedium,
                color = GoldStar,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "/10",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
fun MetaChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Boolean = false,
) {
    val container = if (accent) BrandCherry.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (accent) BrandCherry.copy(alpha = 0.4f)
                      else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val textColor = if (accent) BrandCherry else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = container,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = textColor
            )
        }
    }
}

// ======================================================
// PLAY BUTTON — 3D Glow
// ======================================================
@Composable
fun PlayOverlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size + 32.dp)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrandCherry.copy(alpha = 0.50f),
                            BrandCherry.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    radius = size.toPx() * 0.9f
                )
            }
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 24.dp,
            modifier = Modifier
                .size(size)
                .clickable(onClick = onClick)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Phát",
                    tint = Color.Black,
                    modifier = Modifier.size(size * 0.55f),
                )
            }
        }
    }
}

// ======================================================
// PRIMARY BUTTON — Aurora mesh
// ======================================================
@Composable
fun PremiumPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(54.dp)
            .shadow(16.dp, RoundedCornerShape(18.dp), ambientColor = BrandCherry.copy(alpha = 0.55f)),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandCherry,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
        )
    }
}

// ======================================================
// STATES — Loading, Error, Empty
// ======================================================
@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Primary,
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun ShimmerCard(modifier: Modifier = Modifier, aspect: Float = 2f / 3f) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "translate"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .clip(RoundedCornerShape(22.dp))
            .background(OLED4)
            .drawBehind {
                val brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A1D2A),
                        Color(0xFF2B2E40),
                        Color(0xFF3A3D52),
                        Color(0xFF2B2E40),
                        Color(0xFF1A1D2A)
                    ),
                    start = Offset(size.width * translate - size.width, 0f),
                    end = Offset(size.width * translate, size.height)
                )
                drawRect(brush = brush)
            }
    )
}

@Composable
fun ErrorBox(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = BrandCherry.copy(alpha = 0.12f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.CloudOff,
                    contentDescription = null,
                    tint = BrandCherry,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Ối! Có chút trục trặc",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text("Thử lại", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmptyBox(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ShimmerPosterRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) {
            ShimmerCard(modifier = Modifier.width(140.dp))
        }
    }
}
