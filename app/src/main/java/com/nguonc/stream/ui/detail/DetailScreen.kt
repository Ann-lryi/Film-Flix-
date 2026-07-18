package com.nguonc.stream.ui.detail

import android.text.Html
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nguonc.stream.ui.components.ErrorBox
import com.nguonc.stream.ui.components.LoadingBox
import com.nguonc.stream.ui.components.MetaChip
import com.nguonc.stream.ui.components.PremiumPrimaryButton
import com.nguonc.stream.ui.components.PremiumQualityBadge
import com.nguonc.stream.ui.components.TmdbRating
import com.nguonc.stream.ui.theme.Aurora
import com.nguonc.stream.ui.theme.BrandCherry
import com.nguonc.stream.ui.theme.OnDarkSurfaceVariant
import com.nguonc.stream.ui.theme.SunGold
import com.nguonc.stream.ui.theme.SunAmber

@Composable
fun DetailScreen(
    slug: String,
    onBack: () -> Unit,
    onPlay: (episodeSlug: String?) -> Unit,
    onCategoryClick: (slug: String, name: String) -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            state.isLoading -> LoadingBox()
            state.error != null -> ErrorBox(state.error!!, onRetry = viewModel::load)
            state.bundle != null -> DetailContent(
                state = state,
                onPlay = onPlay,
                onToggleFavorite = viewModel::toggleFavorite,
                onSelectServer = viewModel::selectServer,
                onCategoryClick = onCategoryClick,
            )
        }

        // Floating top bar — glass morph
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassCircleButton(
                onClick = onBack,
                icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = Color.White) }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassCircleButton(
                    onClick = { },
                    icon = { Icon(Icons.Filled.Share, "Chia sẻ", tint = Color.White, modifier = Modifier.size(20.dp)) }
                )
                GlassCircleButton(
                    onClick = { },
                    icon = { Icon(Icons.Filled.Bookmark, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                )
            }
        }
    }
}

@Composable
private fun GlassCircleButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Surface(
        color = Color.Black.copy(alpha = 0.45f),
        shape = CircleShape,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
        modifier = Modifier
            .size(44.dp)
            .shadow(10.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.4f))
    ) {
        IconButton(onClick = onClick) { icon() }
    }
}

@Composable
private fun DetailContent(
    state: DetailUiState,
    onPlay: (episodeSlug: String?) -> Unit,
    onToggleFavorite: () -> Unit,
    onSelectServer: (Int) -> Unit,
    onCategoryClick: (slug: String, name: String) -> Unit,
) {
    val bundle = state.bundle ?: return
    val movie = bundle.movie
    val server = bundle.episodes.getOrNull(state.selectedServer)
    val firstEpisode = server?.serverData?.firstOrNull()?.slug
    var isExpandedDescription by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val parallaxProgress by remember {
        derivedStateOf {
            (scrollState.value.coerceAtMost(800) / 800f).coerceIn(0f, 1f)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // ---- BACKDROP WITH PARALLAX ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .graphicsLayer { translationY = parallaxProgress * -60f }
        ) {
            AsyncImage(
                model = movie.thumbUrl.ifBlank { movie.posterUrl },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { scaleX = 1.1f + parallaxProgress * 0.05f; scaleY = 1.1f + parallaxProgress * 0.05f },
                contentScale = ContentScale.Crop,
            )

            // Multi-layer cinematic scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.30f),
                            0.20f to Color.Transparent,
                            0.55f to Color.Black.copy(alpha = 0.10f),
                            0.78f to Color.Black.copy(alpha = 0.65f),
                            0.92f to MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                            1f to MaterialTheme.colorScheme.background
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            0f to Color.Black.copy(alpha = 0.40f),
                            0.4f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.30f)
                        )
                    )
            )

            // Ambient glow blob (vignette)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    BrandCherry.copy(alpha = 0.18f),
                                    Color.Transparent
                                ),
                                radius = size.minDimension * 0.6f,
                                center = center.copy(y = size.height * 0.5f)
                            )
                        )
                    }
            )

            // Quality + meta on backdrop
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .offset(y = (-12).dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumQualityBadge(text = (movie.quality ?: "FHD").uppercase())
                    if (movie.year > 0) {
                        Surface(
                            color = Color.White.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.24f))
                        ) {
                            Text(
                                "${movie.year}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    if (movie.time.isNotBlank()) {
                        Text(
                            "• ${movie.time}",
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }

        // ---- MAIN INFO CARD - overlapping ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-64).dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                // Poster with deep shadow + glow
                Card(
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                    modifier = Modifier
                        .width(140.dp)
                        .aspectRatio(2f / 3f)
                        .shadow(24.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.7f))
                ) {
                    AsyncImage(
                        model = movie.posterUrl,
                        contentDescription = movie.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = movie.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.4).sp,
                            lineHeight = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (movie.originName.isNotBlank() && movie.originName != movie.name) {
                        Text(
                            text = movie.originName,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = OnDarkSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if ((movie.tmdb?.voteAverage ?: 0.0) > 0) {
                            TmdbRating(vote = movie.tmdb?.voteAverage ?: 0.0, large = true)
                        }
                        if (movie.episodeCurrent.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandCherry.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, BrandCherry.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = movie.episodeCurrent,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = BrandCherry,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PremiumPrimaryButton(
                    text = if (state.lastWatchedEpisode != null) "Tiếp tục xem" else "Xem ngay",
                    onClick = { onPlay(state.lastWatchedEpisode ?: firstEpisode) },
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.PlayArrow,
                )

                val favScale by animateFloatAsState(
                    targetValue = if (state.isFavorite) 1.1f else 1f,
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    label = "favScale"
                )
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (state.isFavorite) BrandCherry.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (state.isFavorite) BrandCherry.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .size(54.dp)
                        .scale(favScale)
                        .shadow(
                            elevation = if (state.isFavorite) 12.dp else 4.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = if (state.isFavorite) BrandCherry.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.3f)
                        )
                        .clickable { onToggleFavorite() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint = if (state.isFavorite) BrandCherry else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Meta chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (movie.lang.isNotBlank()) MetaChip(text = movie.lang)
                if (movie.quality.isNotBlank()) MetaChip(text = movie.quality.uppercase(), accent = true)
                if (movie.episodeCurrent.isNotBlank()) MetaChip(text = movie.episodeCurrent)
            }

            Spacer(Modifier.height(18.dp))

            // Description card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(16.dp)
                                .clip(CircleShape)
                                .background(Aurora.BrandLinear)
                        )
                        Text(
                            "Giới thiệu",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    val cleanContent = Html.fromHtml(movie.content ?: "", Html.FROM_HTML_MODE_LEGACY).toString()
                    Text(
                        text = cleanContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (isExpandedDescription) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (cleanContent.length > 100) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (isExpandedDescription) "Thu gọn ▲" else "Xem thêm ▼",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = BrandCherry,
                            modifier = Modifier.clickable { isExpandedDescription = !isExpandedDescription }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Categories
            if (movie.category.isNotEmpty()) {
                SectionLabel("Thể loại", BrandCherry)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(movie.category) { cat ->
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { onCategoryClick(cat.slug, cat.name) }
                        ) {
                            Text(
                                cat.name,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Server selector
            if (bundle.episodes.size > 1) {
                SectionLabel("Máy chủ", SunAmber)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    bundle.episodes.forEachIndexed { index, ep ->
                        AuroraServerChip(
                            name = ep.serverName,
                            selected = state.selectedServer == index,
                            onClick = { onSelectServer(index) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Episode list
            server?.let { srv ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Danh sách tập",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${srv.serverData.size} tập",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                val episodes = srv.serverData
                val chunked = episodes.chunked(4)

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    chunked.forEach { rowEps ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowEps.forEach { ep ->
                                val isWatching = state.lastWatchedEpisode == ep.slug
                                AuroraEpisodePill(
                                    name = ep.name,
                                    isWatching = isWatching,
                                    onClick = { onPlay(ep.slug) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            repeat(4 - rowEps.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String, accent: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AuroraServerChip(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = if (selected) SunGold.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (selected) SunGold.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (selected) SunAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                name,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (selected) SunAmber else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AuroraEpisodePill(
    name: String,
    isWatching: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (isWatching) 1.04f else 1f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "epScale"
    )
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isWatching) BrandCherry.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isWatching) BrandCherry.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isWatching) 8.dp else 0.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = if (isWatching) BrandCherry.copy(alpha = 0.5f) else Color.Transparent
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
        ) {
            if (isWatching) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Aurora.BrandLinear),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isWatching) FontWeight.Black else FontWeight.SemiBold
                ),
                color = if (isWatching) BrandCherry else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
