package com.nguonc.stream.ui.detail

import android.text.Html
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nguonc.stream.ui.components.ErrorBox
import com.nguonc.stream.ui.components.LoadingBox
import com.nguonc.stream.ui.components.MetaChip
import com.nguonc.stream.ui.components.PlayOverlayButton
import com.nguonc.stream.ui.components.TmdbRating

@Composable
fun DetailScreen(
    slug: String,
    onBack: () -> Unit,
    onPlay: (episodeSlug: String?) -> Unit,
    onCategoryClick: (slug: String, name: String) -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
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

        // Nút back nổi trên backdrop
        Surface(
            color = Color.Black.copy(alpha = 0.35f),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .statusBarsPadding()
                .padding(12.dp)
                .align(Alignment.TopStart),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.White,
                )
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // ---- Backdrop + nút play ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
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
                            0.4f to Color.Transparent,
                            1f to MaterialTheme.colorScheme.background,
                        )
                    ),
            )
            PlayOverlayButton(
                onClick = { onPlay(state.lastWatchedEpisode ?: firstEpisode) },
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // ---- Poster + tiêu đề ----
            Row(modifier = Modifier.offset(y = (-40).dp)) {
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.name,
                    modifier = Modifier
                        .width(110.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.padding(top = 44.dp)) {
                    Text(
                        movie.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    if (movie.originName.isNotBlank() && movie.originName != movie.name) {
                        Text(
                            movie.originName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    TmdbRating(vote = movie.tmdb?.voteAverage ?: 0.0)
                }
            }

            // ---- Hàng meta + nút hành động ----
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.offset(y = (-24).dp),
            ) {
                if (movie.year > 0) MetaChip(movie.year.toString())
                if (movie.quality.isNotBlank()) MetaChip(movie.quality)
                if (movie.episodeCurrent.isNotBlank()) MetaChip(movie.episodeCurrent)
                if (movie.time.isNotBlank()) MetaChip(movie.time)
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-12).dp),
            ) {
                Button(
                    onClick = { onPlay(state.lastWatchedEpisode ?: firstEpisode) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (state.lastWatchedEpisode != null) "Xem tiếp" else "Xem ngay")
                }
                Button(
                    onClick = onToggleFavorite,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isFavorite)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (state.isFavorite)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (state.isFavorite) "Đã thích" else "Yêu thích")
                }
            }

            // ---- Nội dung ----
            val contentText = rememberPlainText(movie.content)
            if (contentText.isNotBlank()) {
                Text(
                    "Nội dung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    contentText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(18.dp))
            }

            // ---- Thể loại ----
            if (movie.categories.isNotEmpty()) {
                Text(
                    "Thể loại",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    movie.categories.take(4).forEach { category ->
                        FilterChip(
                            selected = false,
                            onClick = { onCategoryClick(category.slug, category.name) },
                            label = { Text(category.name) },
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            // ---- Diễn viên / đạo diễn ----
            if (movie.directors.any { it.isNotBlank() }) {
                Text(
                    "Đạo diễn: " + movie.directors.filter { it.isNotBlank() }.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
            }
            if (movie.actors.any { it.isNotBlank() }) {
                Text(
                    "Diễn viên: " + movie.actors.filter { it.isNotBlank() }.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(18.dp))
            }

            // ---- Danh sách tập ----
            if (bundle.episodes.isNotEmpty()) {
                Text(
                    "Danh sách tập",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))

                if (bundle.episodes.size > 1) {
                    ScrollableTabRow(
                        selectedTabIndex = state.selectedServer,
                        edgePadding = 0.dp,
                    ) {
                        bundle.episodes.forEachIndexed { index, srv ->
                            Tab(
                                selected = state.selectedServer == index,
                                onClick = { onSelectServer(index) },
                                text = { Text(srv.serverName) },
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(server?.serverData.orEmpty(), key = { it.slug }) { episode ->
                        val isLast = episode.slug == state.lastWatchedEpisode
                        FilterChip(
                            selected = isLast,
                            onClick = { onPlay(episode.slug) },
                            label = { Text(episode.name) },
                        )
                    }
                }
            } else {
                Text(
                    "Phim chưa có tập nào.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun rememberPlainText(html: String): String =
    androidx.compose.runtime.remember(html) {
        if (html.isBlank()) ""
        else Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim()
    }
