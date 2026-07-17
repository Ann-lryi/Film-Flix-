package com.nguonc.stream.ui.detail

import android.text.Html
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
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
import com.nguonc.stream.ui.components.TmdbRating
import com.nguonc.stream.ui.theme.AppGradients
import com.nguonc.stream.ui.theme.PrimaryRed

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

        // Action Bar Trát trên cùng
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.size(42.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.White,
                    )
                }
            }

            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.size(42.dp)
            ) {
                IconButton(onClick = { /* Share action */ }) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "Chia sẻ",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
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
            .verticalScroll(rememberScrollState()),
    ) {
        // ---- Backdrop siêu rộng + Play Overlay ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            AsyncImage(
                model = movie.thumbUrl.ifBlank { movie.posterUrl },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.2f to Color.Transparent,
                            0.7f to MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                            1f to MaterialTheme.colorScheme.background,
                        )
                    ),
            )
            PlayOverlayButton(
                onClick = { onPlay(state.lastWatchedEpisode ?: firstEpisode) },
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // ---- Header Card Panel (Thẻ thông tin chính, loại bỏ chồng chéo lỗi) ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-36).dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                // Poster Phim
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .width(130.dp)
                        .aspectRatio(2f / 3f)
                ) {
                    AsyncImage(
                        model = movie.posterUrl,
                        contentDescription = movie.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                
                Spacer(Modifier.width(16.dp))
                
                // Title + Rating
                Column(
                    modifier = Modifier.padding(bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = movie.name,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (movie.originName.isNotBlank() && movie.originName != movie.name) {
                        Text(
                            text = movie.originName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    TmdbRating(vote = movie.tmdb?.voteAverage ?: 0.0)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Meta Chips ----
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (movie.year > 0) MetaChip("${movie.year}")
                if (movie.quality.isNotBlank()) MetaChip(movie.quality)
                if (movie.episodeCurrent.isNotBlank()) MetaChip(movie.episodeCurrent)
                if (movie.time.isNotBlank()) MetaChip(movie.time)
            }

            Spacer(Modifier.height(20.dp))

            // ---- Action Buttons (Xem Ngay + Yêu Thích) ----
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onPlay(state.lastWatchedEpisode ?: firstEpisode) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = PrimaryRed),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (state.lastWatchedEpisode != null) "XEM TIẾP TẬP CŨ" else "XEM PHIM NGAY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (state.isFavorite) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (state.isFavorite) PrimaryRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .size(52.dp)
                        .clickable(onClick = onToggleFavorite)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Yêu thích",
                            tint = if (state.isFavorite) PrimaryRed else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---- Nội dung tóm tắt ----
            val contentText = rememberPlainText(movie.content)
            if (contentText.isNotBlank()) {
                Text(
                    text = "Nội dung phim",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = contentText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (isExpandedDescription) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { isExpandedDescription = !isExpandedDescription }
                )
                Text(
                    text = if (isExpandedDescription) "Thu gọn ▲" else "Xem thêm ▼",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryRed,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { isExpandedDescription = !isExpandedDescription }
                )
                Spacer(Modifier.height(24.dp))
            }

            // ---- Thể loại ----
            if (movie.categories.isNotEmpty()) {
                Text(
                    text = "Thể loại",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(movie.categories) { category ->
                        FilterChip(
                            selected = false,
                            onClick = { onCategoryClick(category.slug, category.name) },
                            label = { Text(category.name, fontWeight = FontWeight.SemiBold) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // ---- Diễn viên / Đạo diễn ----
            if (movie.directors.any { it.isNotBlank() }) {
                Text(
                    text = "🎬 Đạo diễn: " + movie.directors.filter { it.isNotBlank() }.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
            }
            if (movie.actors.any { it.isNotBlank() }) {
                Text(
                    text = "👥 Diễn viên: " + movie.actors.filter { it.isNotBlank() }.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
            }

            // ---- Danh sách tập phim ----
            if (bundle.episodes.isNotEmpty()) {
                Text(
                    text = "Chọn tập phim",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))

                if (bundle.episodes.size > 1) {
                    ScrollableTabRow(
                        selectedTabIndex = state.selectedServer,
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        bundle.episodes.forEachIndexed { index, srv ->
                            val isSelected = state.selectedServer == index
                            Tab(
                                selected = isSelected,
                                onClick = { onSelectServer(index) },
                                text = {
                                    Text(
                                        text = srv.serverName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(server?.serverData.orEmpty(), key = { it.slug }) { episode ->
                        val isLast = episode.slug == state.lastWatchedEpisode
                        FilterChip(
                            selected = isLast,
                            onClick = { onPlay(episode.slug) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isLast) {
                                        Icon(
                                            Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.White
                                        )
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(episode.name, fontWeight = FontWeight.Bold)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryRed,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            } else {
                Text(
                    text = "Phim hiện đang cập nhật tập mới.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun rememberPlainText(html: String): String =
    remember(html) {
        if (html.isBlank()) ""
        else Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim()
    }
