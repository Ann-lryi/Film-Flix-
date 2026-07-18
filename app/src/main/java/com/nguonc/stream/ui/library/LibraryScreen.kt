package com.nguonc.stream.ui.library

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nguonc.stream.data.local.FavoriteEntity
import com.nguonc.stream.data.local.HistoryEntity
import com.nguonc.stream.data.remote.dto.MovieItemDto
import com.nguonc.stream.ui.components.EmptyBox
import com.nguonc.stream.ui.components.MoviePosterCard
import com.nguonc.stream.ui.theme.Aurora
import com.nguonc.stream.ui.theme.AuroraViolet
import com.nguonc.stream.ui.theme.BrandCherry
import com.nguonc.stream.ui.theme.BrandCherryDeep
import com.nguonc.stream.ui.theme.BrandCherrySoft
import com.nguonc.stream.ui.theme.OnDarkSurfaceVariant
import com.nguonc.stream.ui.theme.SunAmber
import com.nguonc.stream.ui.theme.SunGold
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-60).dp)
                .drawBehind {
                    drawCircle(brush = Aurora.AmbientCherry, radius = 340.dp.toPx())
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = SunGold.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, SunGold.copy(alpha = 0.32f))
                    ) {
                        Text(
                            "CỦA TÔI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            ),
                            color = SunAmber,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .width(60.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.16f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Bộ sưu tập",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.6).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Phim yêu thích & lịch sử xem của bạn",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnDarkSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))

                AuroraSegmentedControl(
                    selected = selectedTab,
                    onSelect = { selectedTab = it },
                    favoriteCount = favorites.size,
                    historyCount = history.size,
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
}

@Composable
private fun AuroraSegmentedControl(
    selected: Int,
    onSelect: (Int) -> Unit,
    favoriteCount: Int,
    historyCount: Int,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = Color.Black.copy(alpha = 0.4f))
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            SegmentTab(
                selected = selected == 0,
                icon = Icons.Filled.Favorite,
                text = "Yêu thích",
                count = favoriteCount,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(0) }
            )
            SegmentTab(
                selected = selected == 1,
                icon = Icons.Filled.History,
                text = "Lịch sử",
                count = historyCount,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(1) }
            )
        }
    }
}

@Composable
private fun SegmentTab(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    count: Int,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "segScale"
    )
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) BrandCherry else Color.Transparent,
        shadowElevation = if (selected) 8.dp else 0.dp,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 11.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(16.dp)
                    .scale(iconScale)
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Surface(
                shape = CircleShape,
                color = if (selected) Color.White.copy(alpha = 0.22f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                modifier = Modifier
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                )
            }
        }
    }
}

@Composable
private fun FavoriteTab(
    favorites: List<FavoriteEntity>,
    onMovieClick: (String) -> Unit,
) {
    if (favorites.isEmpty()) {
        EmptyBox("Chưa có phim yêu thích nào.\nNhấn ♥ ở trang chi tiết để lưu lại.")
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 138.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.fillMaxSize()
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
        EmptyBox("Bạn chưa xem phim nào gần đây.")
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
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
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp), ambientColor = Color.Black.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(width = 76.dp, height = 102.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Crop,
                )
                // Play badge overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(34.dp)
                        .shadow(10.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.6f))
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                        .border(
                            BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.1).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = BrandCherry.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, BrandCherry.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = item.episodeName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = BrandCherry,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                        )
                    }
                    Text(
                        text = "• ${formatPosition(item.positionMs)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(10.dp))

                val progress = remember(item.positionMs) {
                    if (item.positionMs <= 0) 0.05f else 0.52f
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Aurora.BrandLinear)
                    )
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Xem cuối: ${formatTime(item.updatedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(10.dp, RoundedCornerShape(14.dp), ambientColor = BrandCherry.copy(alpha = 0.55f))
                        .clip(RoundedCornerShape(14.dp))
                        .background(Aurora.BrandLinear)
                        .clickable(onClick = onResume)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Xem tiếp",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Xoá",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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
