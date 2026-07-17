package com.nguonc.stream.ui.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.nguonc.stream.ui.theme.PrimaryRed
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ---- Header Thư Viện ----
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Thư Viện Phim Của Bạn",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Quản lý phim đã yêu thích và tiếp tục xem lại lịch sử",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = PrimaryRed,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    height = 3.dp,
                    color = PrimaryRed
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "♥ Yêu Thích (${favorites.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                    )
                },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "🕒 Lịch Sử Xem (${history.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                    )
                },
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
        EmptyBox("Chưa có phim yêu thích nào.\nNhấn nút ♥ ở trang chi tiết để lưu vào bộ sưu tập.")
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 126.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
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
        EmptyBox("Bạn chưa xem bộ phim nào gần đây.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
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
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Poster Nhỏ
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .size(width = 64.dp, height = 96.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = PrimaryRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.episodeName,
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "• ${formatPosition(item.positionMs)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Linear Progress Bar cho Lịch Sử Xem
                val progress = remember(item.positionMs) {
                    // Giả định tiến độ 40-80% nếu chưa có duration chính xác để hiển thị trực quan
                    if (item.positionMs <= 0) 0.05f else 0.45f
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = PrimaryRed,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                )
                
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Lần xem cuối: ${formatTime(item.updatedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryRed,
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(onClick = onResume) {
                        Icon(
                            Icons.Filled.PlayCircleOutline,
                            contentDescription = "Xem tiếp",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Xoá",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
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
