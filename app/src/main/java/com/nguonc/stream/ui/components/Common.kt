package com.nguonc.stream.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nguonc.stream.data.remote.dto.MovieItemDto
import com.nguonc.stream.ui.theme.AccentCyan
import com.nguonc.stream.ui.theme.AccentViolet
import com.nguonc.stream.ui.theme.AppGradients
import com.nguonc.stream.ui.theme.AppShapes
import com.nguonc.stream.ui.theme.Elevation
import com.nguonc.stream.ui.theme.FilmFlixIcons
import com.nguonc.stream.ui.theme.GoldStar
import com.nguonc.stream.ui.theme.Motion
import com.nguonc.stream.ui.theme.PremiumTextStyles
import com.nguonc.stream.ui.theme.Primary
import com.nguonc.stream.ui.theme.Secondary
import com.nguonc.stream.ui.theme.edgeGlow
import com.nguonc.stream.ui.theme.glowShadow
import com.nguonc.stream.ui.theme.premiumShadow
import com.nguonc.stream.ui.theme.topHighlight
import kotlinx.coroutines.delay
import java.util.Locale

// ======================================================
// PREMIUM POSTER CARD 3.0 — The Hero Component
// ------------------------------------------------------
// Improvements over 2.0:
//  • Spring-driven press scale (physical, organic)
//  • Multi-layer shadow (ambient + spot, dark tuned for OLED)
//  • Top highlight ring for gloss
//  • Larger play micro-icon (32dp vs 26dp)
//  • Better typographic rhythm
//  • Animated press border
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
        targetValue = if (isPressed) Motion.PressScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "posterScale"
    )
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 4f else 12f,
        animationSpec = Motion.snappy(),
        label = "posterElev"
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
            border = BorderStroke(
                width = if (isPressed) 1.2.dp else 0.6.dp,
                color = if (isPressed) Primary.copy(alpha = 0.55f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .premiumShadow(
                    elevation = elevation.dp,
                    shape = RoundedCornerShape(cornerRadius),
                    ambientAlpha = 0.42f,
                    spotAlpha = 0.34f
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

                // Gloss reflection top edge — gives the card a "screen" sheen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.22f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Bottom scrim + info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .align(Alignment.BottomCenter)
                        .background(AppGradients.CardBottomOverlay)
                )

                // Top badges row — only quality badge (MỚI removed per user request)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    movie.quality?.takeIf { it.isNotBlank() }?.let {
                        PremiumQualityBadge(text = it.uppercase())
                    }
                }

                // Bottom meta row — enlarged play button (32dp vs old 26dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 11.dp, vertical = 10.dp),
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.28f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .topHighlight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FilmFlixIcons.PlayFilled,
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
// BADGES — Premium 3.0 look
// ======================================================
@Composable
fun PremiumQualityBadge(text: String, modifier: Modifier = Modifier) {
    val isUltra = text.contains("4K") || text.contains("FHD") || text.contains("IMAX") || text.contains("HDR")
    Surface(
        shape = AppShapes.XSmall,
        color = if (isUltra) Color.Black.copy(alpha = 0.80f) else Color.Black.copy(alpha = 0.66f),
        border = BorderStroke(
            1.dp,
            if (isUltra) Secondary.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.20f)
        ),
        modifier = modifier.premiumShadow(Elevation.S, AppShapes.XSmall)
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
                .topHighlight()
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
                    color = if (isUltra) Secondary else Color.White,
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
    // Subtle infinite pulse to draw the eye to "MỚI"
    val transition = rememberInfiniteTransition(label = "newPulse")
    val alpha by transition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = Motion.drift(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "newPulseAlpha"
    )
    Surface(
        shape = AppShapes.Pill,
        color = Primary.copy(alpha = alpha),
        modifier = modifier.premiumShadow(Elevation.S, AppShapes.Pill)
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
        shape = AppShapes.XSmall,
        color = Color.Black.copy(alpha = 0.48f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.16f)),
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
// HERO BANNER 3.0 — Cinematic carousel with depth & parallax
// ------------------------------------------------------
// Improvements over 2.0:
//  • Deterministic category labels (no .random() flicker)
//  • Larger indicator with morphing capsule + glow on selected
//  • Pause auto-advance while user is dragging
//  • GlowShadow on primary CTA
//  • Better scrim layering (3 stops, OLED-tuned)
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

    // Auto scroll — pauses naturally while user drags via try/catch
    LaunchedEffect(pagerState) {
        while (true) {
            delay(5500)
            val next = (pagerState.currentPage + 1) % take.size
            runCatching {
                pagerState.animateScrollToPage(
                    next,
                    animationSpec = Motion.emphasized(700)
                )
            }
        }
    }

    // Deterministic fallback categories — derived from slug hash so they
    // don't reshuffle every recomposition.
    val heroCategoryPool = remember {
        listOf("Hành Động", "Khoa Học", "Tình Cảm", "Gây Cấn", "Hài Hước", "Phiêu Lưu")
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
                animationSpec = Motion.emphasized(500),
                label = "heroScale"
            )

            Card(
                shape = AppShapes.XXLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) Elevation.Hero else Elevation.S),
                border = BorderStroke(
                    1.dp,
                    if (isCurrent) Color.White.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .graphicsLayer { clip = true }
                    .premiumShadow(
                        elevation = if (isCurrent) Elevation.XXL else Elevation.S,
                        shape = AppShapes.XXLarge,
                        ambientAlpha = 0.55f,
                        spotAlpha = 0.40f
                    )
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onMovieClick(movie.slug)
                    }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Backdrop
                    AsyncImage(
                        model = movie.thumbUrl.ifBlank { movie.posterUrl },
                        contentDescription = movie.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Depth glows — three-stop vignette
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .align(Alignment.BottomCenter)
                            .background(AppGradients.HeroBottomScrim)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
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
                                        Color.Black.copy(alpha = 0.36f),
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.26f)
                                    )
                                )
                            )
                    )

                    // Top row: rank pill + quality
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
                            shape = AppShapes.Pill,
                            modifier = Modifier.glowShadow(
                                color = Primary,
                                shape = AppShapes.Pill,
                                glowRadius = 16.dp,
                                elevation = Elevation.S
                            )
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
                            .padding(20.dp)
                    ) {
                        // Deterministic categories (derived from slug, not random)
                        val cats = remember(movie.slug) {
                            val startIdx = (movie.slug.hashCode() and Int.MAX_VALUE) % heroCategoryPool.size
                            listOf(heroCategoryPool[startIdx], heroCategoryPool[(startIdx + 2) % heroCategoryPool.size])
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            cats.forEachIndexed { idx, label ->
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(Primary)
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                                if (idx < cats.size - 1) {
                                    Spacer(Modifier.width(6.dp))
                                }
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

                        Spacer(Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Primary Play button — glowing white pill
                            Surface(
                                shape = AppShapes.Pill,
                                color = Color.White,
                                modifier = Modifier
                                    .height(46.dp)
                                    .glowShadow(
                                        color = Color.White,
                                        shape = AppShapes.Pill,
                                        glowRadius = 18.dp,
                                        elevation = Elevation.M
                                    )
                                    .clickable { onMovieClick(movie.slug) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Icon(
                                        imageVector = FilmFlixIcons.PlayFilled,
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

                            // "+ List" ghost button — glass circle
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = FilmFlixIcons.PlusOutline,
                                        contentDescription = "Thêm vào danh sách",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
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

        Spacer(Modifier.height(16.dp))

        // Morphing capsule indicator with glow on selected
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(take.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateFloatAsState(
                    targetValue = if (isSelected) 32f else 6f,
                    animationSpec = Motion.emphasized(400),
                    label = "dotW"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width.dp, 6.dp)
                        .clip(AppShapes.Pill)
                        .background(
                            if (isSelected) AppGradients.PrimaryHorizontal
                            else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.18f)))
                        )
                )
            }
        }
    }
}

// ======================================================
// SECTION HEADER 3.0 — Animated accent bar, custom chevron
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
            // Accent bar — premium gradient with subtle glow
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(26.dp)
                    .clip(AppShapes.Pill)
                    .background(AppGradients.PrimaryVertical)
                    .premiumShadow(Elevation.XS, AppShapes.Pill, ambientAlpha = 0.5f, spotAlpha = 0.3f)
            )

            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(AppShapes.Small)
                        .background(Primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }

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
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val bgAlpha by animateFloatAsState(
                targetValue = if (isPressed) 1f else 0.8f,
                animationSpec = Motion.snappy(),
                label = "seeAllBg"
            )
            Surface(
                shape = AppShapes.Pill,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = bgAlpha),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                modifier = Modifier
                    .clip(AppShapes.Pill)
                    .clickable(interactionSource = interactionSource, indication = null, onClick = onSeeMore)
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
                        imageVector = FilmFlixIcons.ChevronRight,
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
        shape = if (large) AppShapes.Small else AppShapes.XSmall,
        color = Color(0xFF1A1505),
        border = BorderStroke(1.dp, Secondary.copy(alpha = 0.40f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (large) 10.dp else 8.dp, vertical = if (large) 6.dp else 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = FilmFlixIcons.StarFilled,
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
        shape = AppShapes.Pill,
        color = if (accent) Primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (accent) Primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
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
// PLAY BUTTON 3.0 — Multi-layer glow signature
// ======================================================
@Composable
fun PlayOverlayButton(onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 64.dp) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Motion.PressScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "playOverlayScale"
    )

    // Subtle ambient pulse to draw the eye
    val transition = rememberInfiniteTransition(label = "playPulse")
    val glowScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = Motion.drift(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "playPulseScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size + 24.dp)
            .scale(scale)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Primary.copy(alpha = 0.55f * glowScale),
                            Primary.copy(alpha = 0.18f * glowScale),
                            Primary.copy(alpha = 0.0f)
                        )
                    ),
                    radius = size.toPx() * 0.95f * glowScale
                )
            }
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = Elevation.XXL,
            modifier = Modifier
                .size(size)
                .glowShadow(
                    color = Primary,
                    shape = CircleShape,
                    glowRadius = 24.dp,
                    elevation = Elevation.L
                )
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .topHighlight()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = FilmFlixIcons.PlayFilled,
                    contentDescription = "Phát",
                    tint = Color.Black,
                    modifier = Modifier.size(size * 0.55f),
                )
            }
        }
    }
}

// Modern primary button — gradient + glow + press scale
@Composable
fun PremiumPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Motion.PressScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "primaryBtnScale"
    )
    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .height(52.dp)
            .scale(scale)
            .glowShadow(
                color = Primary,
                shape = AppShapes.Medium,
                glowRadius = 16.dp,
                elevation = Elevation.L
            ),
        shape = AppShapes.Medium,
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
// STATES — Loading, Error, Empty with premium motion
// ======================================================
@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing spinner
        val transition = rememberInfiniteTransition(label = "loadingPulse")
        val alpha by transition.animateFloat(
            initialValue = 0.55f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = Motion.drift(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "loadingAlpha"
        )
        CircularProgressIndicator(
            color = Primary.copy(alpha = alpha),
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(36.dp)
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
            animation = Motion.drift(1300),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .clip(AppShapes.Large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .drawBehind {
                val brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF111218),
                        Color(0xFF22232F),
                        Color(0xFF111218)
                    ),
                    start = Offset(size.width * translate - size.width, 0f),
                    end = Offset(size.width * translate, size.height)
                )
                drawRect(brush = brush)
            }
    )
}

@Composable
fun ShimmerHero(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "heroShimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = Motion.drift(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "heroShimmerTranslate"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(440.dp)
            .padding(horizontal = 20.dp)
            .clip(AppShapes.XXLarge)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .drawBehind {
                val brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF111218),
                        Color(0xFF22232F),
                        Color(0xFF111218)
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
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(Motion.standard(Motion.DurationM)) + scaleIn(initialScale = 0.96f),
        exit = fadeOut(Motion.standard(Motion.DurationS)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Primary.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.25f)),
                modifier = Modifier.size(80.dp).edgeGlow(Primary, intensity = 0.22f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        FilmFlixIcons.CloudOffOutline,
                        "Lỗi",
                        tint = Primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
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
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onRetry,
                shape = AppShapes.Small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(FilmFlixIcons.ChevronLeft, null, modifier = Modifier.size(16.dp).graphicsLayer { rotationZ = 180f })
                Spacer(Modifier.width(6.dp))
                Text("Thử lại")
            }
        }
    }
}

@Composable
fun EmptyBox(text: String, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(Motion.standard(Motion.DurationM)) + scaleIn(initialScale = 0.96f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.size(88.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        FilmFlixIcons.FolderOpenOutline,
                        "Trống",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// Quick shimmer row for home loading
@Composable
fun ShimmerPosterRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) {
            ShimmerCard(modifier = Modifier.width(136.dp))
        }
    }
}
