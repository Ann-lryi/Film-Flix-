package com.nguonc.stream.ui.components

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
import androidx.compose.material.icons.filled.LocalMovies
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nguonc.stream.data.remote.dto.MovieItemDto
import com.nguonc.stream.ui.theme.AppGradients
import com.nguonc.stream.ui.theme.DarkOutline
import com.nguonc.stream.ui.theme.DarkSurfaceElevated
import com.nguonc.stream.ui.theme.GoldStar
import com.nguonc.stream.ui.theme.PremiumTextStyles
import com.nguonc.stream.ui.theme.Primary
import kotlinx.coroutines.delay
import java.util.Locale

// ======================================================
// PREMIUM POSTER CARD 2.0 - The Hero Component
// Features: 20dp radius, soft shadow, micro-interaction, glass badge
// ======================================================
@Composable
fun MoviePosterCard(
    movie: MovieItemDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    aspect: Float = 2f / 3f,
    cornerRadius: Dp = 20.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "posterScale"
    )
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 2f else 10f,
        animationSpec = tween(180),
        label = "elevation"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Card(
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = (elevation * 0.8f).dp,
                    shape = RoundedCornerShape(cornerRadius),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
        ) {
            Box {
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspect)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                )

                // Gloss reflection top edge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Bottom scrim + info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .align(Alignment.BottomCenter)
                        .background(AppGradients.CardBottomOverlay)
                )

                // Top badges row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    movie.quality?.takeIf { it.isNotBlank() }?.let {
                        PremiumQualityBadge(text = it.uppercase())
                    }
                    // Reserve right for extra like NEW
                    if (movie.year >= 2024) {
                        NewBadge()
                    }
                }

                // Bottom meta
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

                    // Play micro icon with blur bg
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.22f),
                                        Color.Transparent
                                    )
                                )
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
                    letterSpacing = 0.1.sp
                ),
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.titleSmall.lineHeight
            )
            if (movie.originName.isNotBlank() && movie.originName != movie.name) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = movie.originName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ======================================================
// BADGES - Premium look
// ======================================================
@Composable
fun PremiumQualityBadge(text: String, modifier: Modifier = Modifier) {
    val isUltra = text.contains("4K") || text.contains("FHD") || text.contains("IMAX") || text.contains("HDR")
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isUltra) Color.Black.copy(alpha = 0.78f) else Color.Black.copy(alpha = 0.65f),
        border = BorderStroke(
            1.dp,
            if (isUltra) Color(0xFFFFD60A).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.18f)
        ),
        modifier = modifier.shadow(6.dp, RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isUltra) Brush.horizontalGradient(
                        listOf(Color(0xFF1A1505), Color(0xFF2A230A))
                    ) else Brush.horizontalGradient(
                        listOf(Color.Black.copy(alpha = 0.0f), Color.Black.copy(alpha = 0.0f))
                    )
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (isUltra) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AppGradients.GoldGradient)
                    )
                }
                Text(
                    text = text,
                    style = PremiumTextStyles.Badge,
                    color = if (isUltra) Color(0xFFFFD60A) else Color.White,
                    fontWeight = if (isUltra) FontWeight.Black else FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CapsuleBadge(text: String, modifier: Modifier = Modifier) = PremiumQualityBadge(text, modifier)

@Composable
fun NewBadge(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = Primary,
        modifier = modifier.shadow(6.dp, CircleShape)
    ) {
        Text(
            text = "MỚI",
            style = PremiumTextStyles.Badge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun GlassChip(text: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.46f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.14f)),
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

// ======================================================
// HERO BANNER - Cinematic 2.0 with Parallax & Depth
// ======================================================
@Composable
fun HeroBannerCarousel(
    movies: List<MovieItemDto>,
    onMovieClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (movies.isEmpty()) return
    val take = movies.take(6)
    val pagerState = rememberPagerState(pageCount = { take.size })

    // Auto scroll
    LaunchedEffect(pagerState) {
        while (true) {
            delay(5500)
            val next = (pagerState.currentPage + 1) % take.size
            try {
                pagerState.animateScrollToPage(next, animationSpec = tween(700, easing = FastOutSlowInEasing))
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
                .height(420.dp)
        ) { page ->
            val movie = take[page]
            val isCurrent = pagerState.currentPage == page
            val scale by animateFloatAsState(
                targetValue = if (isCurrent) 1f else 0.92f,
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                label = "heroScale"
            )

            Card(
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 20.dp else 6.dp),
                border = BorderStroke(
                    1.dp,
                    if (isCurrent) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .graphicsLayer { clip = true }
                    .clickable { onMovieClick(movie.slug) }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Backdrop
                    AsyncImage(
                        model = movie.thumbUrl.ifBlank { movie.posterUrl },
                        contentDescription = movie.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Depth glows
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .align(Alignment.BottomCenter)
                            .background(AppGradients.HeroBottomScrim)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .align(Alignment.TopCenter)
                            .background(AppGradients.HeroTopScrim)
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
                                        Color.Black.copy(alpha = 0.25f)
                                    )
                                )
                            )
                    )

                    // Top row: Live + Quality
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Primary,
                            shape = RoundedCornerShape(100.dp),
                            modifier = Modifier.shadow(8.dp, RoundedCornerShape(100.dp))
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
                                    text = "#${page + 1} TOP 10 HÔM NAY",
                                    style = PremiumTextStyles.Badge,
                                    color = Color.White
                                )
                            }
                        }

                        PremiumQualityBadge(text = (movie.quality ?: "FHD").uppercase())
                    }

                    // Bottom content - glass panel style
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Truncate-ish description placeholder: categories
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(2) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(Primary)
                                )
                                Text(
                                    text = listOf("Hành Động", "Khoa Học", "Tình Cảm", "Gây Cấn").random(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.86f)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = movie.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.4).sp
                            ),
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = MaterialTheme.typography.headlineMedium.lineHeight
                        )

                        if (movie.originName.isNotBlank()) {
                            Text(
                                text = movie.originName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.72f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Primary Play button - large
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = Color.White,
                                modifier = Modifier
                                    .height(44.dp)
                                    .shadow(12.dp, RoundedCornerShape(100.dp))
                                    .clickable { onMovieClick(movie.slug) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 18.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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

                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "+ List", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            if (movie.year > 0) {
                                Text(
                                    text = "${movie.year} • ${movie.quality ?: "HD"}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Custom indicator - pill morphing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(take.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateFloatAsState(
                    targetValue = if (isSelected) 28f else 6f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                    label = "dotW"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width.dp, 6.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            if (isSelected) Primary
                            else Color.White.copy(alpha = 0.22f)
                        )
                )
            }
        }
    }
}

// ======================================================
// SECTION HEADER - Minimal premium
// ======================================================
@Composable
fun SectionHeader(
    title: String,
    onSeeMore: (() -> Unit)?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: @Composable (() -> Unit)? = null
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
            // Accent bar - 3d style
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(AppGradients.PrimaryVertical)
                    .shadow(4.dp, RoundedCornerShape(100.dp), ambientColor = Primary)
            )

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (onSeeMore != null) {
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
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
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
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
        color = Color(0xFF1A1705),
        border = BorderStroke(1.dp, Color(0xFFFFD60A).copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (large) 10.dp else 8.dp, vertical = if (large) 6.dp else 4.dp),
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
    icon: @Composable (() -> Unit)? = null,
    accent: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = if (accent) Primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (accent) Primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (icon != null) icon()
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (accent) Primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ======================================================
// PLAY BUTTON - Signature glow
// ======================================================
@Composable
fun PlayOverlayButton(onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 64.dp) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size + 16.dp)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Primary.copy(alpha = 0.45f),
                            Primary.copy(alpha = 0.0f)
                        )
                    ),
                    radius = size.toPx()
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

// Modern primary button
@Composable
fun PremiumPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = Primary.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
        )
    }
}

// ======================================================
// STATES - Loading, Error, Empty with premium illustrations
// ======================================================
@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Primary, strokeWidth = 2.5.dp, modifier = Modifier.size(32.dp))
    }
}

@Composable
fun ShimmerCard(modifier: Modifier = Modifier, aspect: Float = 2f/3f) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "translate"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .drawBehind {
                val brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1C1E2B),
                        Color(0xFF2A2E44),
                        Color(0xFF1C1E2B)
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Primary.copy(alpha = 0.12f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.CloudOff, null, tint = Primary, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(text = "Ối! Có chút trục trặc", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(6.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Text("Thử lại")
        }
    }
}

@Composable
fun EmptyBox(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(80.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.FolderOpen, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(36.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Quick shimmer row for home loading
@Composable
fun ShimmerPosterRow(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(4) {
            ShimmerCard(modifier = Modifier.width(136.dp))
        }
    }
}
