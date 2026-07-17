package com.nguonc.stream.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nguonc.stream.data.local.FavoriteEntity
import com.nguonc.stream.data.local.HistoryEntity
import com.nguonc.stream.data.remote.dto.MovieItemDto
import com.nguonc.stream.ui.components.EmptyBox
import com.nguonc.stream.ui.components.MoviePosterCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LibraryScreen(
    onMovieClick: (String) -> Unit,
    onResumeClick: (slug: String, episodeSlug: String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Yêu thích (${favorites.size})") },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Lịch sử (${history.size})") },
            )
        }

        when (selectedTab) {
            0 -> FavoriteTab(favorites = favorites, onMovieClick = onMovieClick)
            else -> HistoryTab(
                history = history,
                onResumeClick = onResumeClick,
                onMovieClick = onMovieClick,
                onRemove = viewModel::removeHistory,
            )
        }
    }
}

@Composable
private fun FavoriteTab(
    favorites: List<FavoriteEntity>,
    onMovieClick: (String) -> Unit,
) {
    if (favorites.isEmpty()) {
        EmptyBox("Chưa có phim yêu thích nào.\nNhấn ♥ ở trang chi tiết để lưu phim.")
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(favorites, key = { it.slug }) { fav ->
            MoviePosterCard(
                movie = MovieItemDto(
                    id = fav.slug,
                    name = fav.name,
                    originName = fav.originName,
                    slug = fav.slug,
                    posterUrl = fav.posterUrl,
                    thumbUrl = fav.thumbUrl,
                    year = fav.year,
                    quality = fav.quality,
                ),
                onClick = { onMovieClick(fav.slug) },
            )
        }
    }
}

@Composable
private fun HistoryTab(
    history: List<HistoryEntity>,
    onResumeClick: (slug: String, episodeSlug: String) -> Unit,
    onMovieClick: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    if (history.isEmpty()) {
        EmptyBox("Bạn chưa xem phim nào.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(history, key = { it.slug }) { item ->
            HistoryRow(
                item = item,
                onResume = { onResumeClick(item.slug, item.episodeSlug) },
                onClick = { onMovieClick(item.slug) },
                onRemove = { onRemove(item.slug) },
            )
        }
    }
}

@Composable
private fun HistoryRow(
    item: HistoryEntity,
    onResume: () -> Unit,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(10.dp),
    ) {
        AsyncImage(
            model = item.posterUrl,
            contentDescription = item.name,
            modifier = Modifier
                .size(width = 52.dp, height = 72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Đang xem: ${item.episodeName} · ${formatPosition(item.positionMs)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                formatTime(item.updatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onResume) {
            Icon(
                Icons.Filled.PlayCircleOutline,
                contentDescription = "Xem tiếp",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Filled.DeleteOutline,
                contentDescription = "Xoá",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatPosition(ms: Long): String {
    val totalSeconds = ms / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, s)
    else String.format(Locale.US, "%02d:%02d", m, s)
}

private fun formatTime(epochMs: Long): String =
    SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(Date(epochMs))
