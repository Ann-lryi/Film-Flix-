package com.nguonc.stream.ui.detail

import android.text.Html
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nguonc.stream.ui.components.ErrorBox
import com.nguonc.stream.ui.components.LoadingBox
import com.nguonc.stream.ui.components.MetaChip
import com.nguonc.stream.ui.components.PlayOverlayButton
import com.nguonc.stream.ui.components.PremiumPrimaryButton
import com.nguonc.stream.ui.components.PremiumQualityBadge
import com.nguonc.stream.ui.components.TmdbRating
import com.nguonc.stream.ui.theme.AppGradients
import com.nguonc.stream.ui.theme.Primary

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

        // Floating top bar - glass morphism
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                modifier = Modifier.size(44.dp).shadow(8.dp, CircleShape)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.White,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                    modifier = Modifier.size(44.dp).shadow(8.dp, CircleShape)
                ) {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Chia sẻ",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
                    modifier = Modifier.size(44.dp).shadow(8.dp, CircleShape)
                ) {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ---- BACKDROP WITH PARALLAX GLASS ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp)
        ) {
            AsyncImage(
                model = movie.thumbUrl.ifBlank { movie.posterUrl },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            // Ambient blur layer to avoid hard edge
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.2f),
                            0.35f to Color.Transparent,
                            0.7f to MaterialTheme.colorScheme.background.copy(alpha = 0.12f),
                            0.85f to MaterialTheme.colorScheme.background.copy(alpha = 0.86f),
                            1f to MaterialTheme.colorScheme.background
                        )
                    )
            )

            // Central play
            PlayOverlayButton(
                onClick = { onPlay(state.lastWatchedEpisode ?: firstEpisode) },
                modifier = Modifier.align(Alignment.Center),
                size = 72.dp
            )

            // Bottom gradient info holder
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-8).dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PremiumQualityBadge(text = (movie.quality ?: "FHD").uppercase())
                    if (movie.year > 0) {
                        Surface(color = Color.White.copy(alpha = 0.14f), shape = RoundedCornerShape(8.dp), border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.16f))) {
                            Text("${movie.year}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (movie.time.isNotBlank()) {
                        Text("• ${movie.time}", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // ---- MAIN INFO CARD - overlapping ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-24).dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                // Poster with deep shadow
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier
                        .width(128.dp)
                        .aspectRatio(2f / 3f)
                        .shadow(20.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.8f))
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
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (movie.originName.isNotBlank() && movie.originName != movie.name) {
                        Text(
                            text = movie.originName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        TmdbRating(vote = movie.tmdb?.voteAverage ?: 0.0, large = true)
                        if (movie.tmdb?.voteAverage ?: 0.0 > 0) {
                            Icon(Icons.Filled.Star, null, tint = Primary.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                            Text(
                                text = "${(movie.tmdb?.season ?: movie.episodeCurrent ?: "")}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Action buttons - full width primary
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PremiumPrimaryButton(
                    text = if (state.lastWatchedEpisode != null) "Tiếp tục xem" else "Xem ngay",
                    onClick = { onPlay(state.lastWatchedEpisode ?: firstEpisode) },
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(20.dp)) }
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier.size(52.dp).clickable { onToggleFavorite() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint = if (state.isFavorite) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
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

            // Description with glass card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Giới thiệu",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    val cleanContent = Html.fromHtml(movie.content ?: "", Html.FROM_HTML_MODE_LEGACY).toString()
                    Text(
                        text = cleanContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (isExpandedDescription) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (isExpandedDescription) "Thu gọn" else "Xem thêm",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Primary,
                        modifier = Modifier.clickable { isExpandedDescription = !isExpandedDescription }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Categories
            if (movie.category.isNotEmpty()) {
                Text("Thể loại", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(movie.category) { cat ->
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { onCategoryClick(cat.slug, cat.name) }
                        ) {
                            Text(cat.name, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Server selector tabs
            if (bundle.episodes.size > 1) {
                ScrollableTabRow(
                    selectedTabIndex = state.selectedServer,
                    containerColor = Color.Transparent,
                    contentColor = Primary,
                    divider = {},
                    edgePadding = 0.dp
                ) {
                    bundle.episodes.forEachIndexed { index, ep ->
                        Tab(
                            selected = state.selectedServer == index,
                            onClick = { onSelectServer(index) },
                            text = {
                                Text(
                                    ep.serverName,
                                    fontWeight = if (state.selectedServer == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Episode list - grid like pills
            server?.let { srv ->
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Danh sách tập (${srv.serverData.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (state.lastWatchedEpisode != null) {
                        Text(
                            text = "Đang xem: ${state.lastWatchedEpisode}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Episode chips - premium
                val episodes = srv.serverData
                val chunked = episodes.chunked(4)

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    chunked.forEach { rowEps ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            rowEps.forEach { ep ->
                                val isWatching = state.lastWatchedEpisode == ep.slug
                                val selected = false // keep neutral
                                FilterChip(
                                    selected = isWatching,
                                    onClick = { onPlay(ep.slug) },
                                    label = {
                                        Text(
                                            text = ep.name,
                                            fontWeight = if (isWatching) FontWeight.Bold else FontWeight.Medium,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    },
                                    leadingIcon = if (isWatching) {
                                        { Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = if (isWatching) Primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = if (isWatching) Primary else MaterialTheme.colorScheme.onSurface,
                                        selectedContainerColor = Primary,
                                        selectedLabelColor = Color.White
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = if (isWatching) Primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        selectedBorderColor = Primary,
                                        enabled = true,
                                        selected = isWatching
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining space if row not full
                            repeat(4 - rowEps.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}
