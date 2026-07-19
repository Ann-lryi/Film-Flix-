package com.nguonc.stream.ui.library

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.scale
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
import com.nguonc.stream.ui.theme.AppGradients
import com.nguonc.stream.ui.theme.AppShapes
import com.nguonc.stream.ui.theme.Elevation
import com.nguonc.stream.ui.theme.FilmFlixIcons
import com.nguonc.stream.ui.theme.Motion
import com.nguonc.stream.ui.theme.PremiumTextStyles
import com.nguonc.stream.ui.theme.Primary
import com.nguonc.stream.ui.theme.glowShadow
import com.nguonc.stream.ui.theme.premiumShadow
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
            .statusBarsPadding()
    ) {
        // Premium header
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Bộ sưu tập",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Surface(
                    shape = AppShapes.Pill,
                    color = Primary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.25f))
                ) {
                    Text(
                        "${favorites.size + history.size}",
                        style = PremiumTextStyles.Eyebrow,
                        color = Primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = "Phim yêu thích & lịch sử xem của bạn",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))

            // Segmented control — pill style with sliding indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.Medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SegmentTab(
                    selected = selectedTab == 0,
                    icon = FilmFlixIcons.HeartFilled,
                    text = "Yêu thích (${favorites.size})",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 0 }
                )
                SegmentTab(
                    selected = selectedTab == 1,
                    icon = FilmFlixIcons.ClockOutline,
                    text = "Lịch sử (${history.size})",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 1 }
                )
            }
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
private fun SegmentTab(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) Motion.SelectedScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "segmentScale"
    )
    Surface(
        shape = AppShapes.Small,
        color = if (selected) Color.White else Color.Transparent,
        shadowElevation = if (selected) Elevation.S else Elevation.None,
        modifier = modifier
            .scale(scale)
            .clip(AppShapes.Small)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
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
        verticalArrangement = Arrangement.spacedBy(14.dp),
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
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) Motion.PressScale else 1f,
        animationSpec = Motion.PressSpring,
        label = "historyRowScale"
    )

    Card(
        shape = AppShapes.Large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f)),
        border = BorderStroke(
            1.dp,
            if (isPressed) Primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.XS),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(AppShapes.Large)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .premiumShadow(Elevation.XS, AppShapes.Large, ambientAlpha = 0.28f, spotAlpha = 0.22f)
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
                        .size(width = 76.dp, height = 100.dp)
                        .clip(AppShapes.Small)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                )
                // Play badge on poster — glowing white pill
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.62f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(30.dp)
                        .glowShadow(
                            color = Primary,
                            shape = CircleShape,
                            glowRadius = 8.dp,
                            elevation = Elevation.None
                        )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            FilmFlixIcons.PlayFilled, null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Primary.copy(alpha = 0.16f),
                        shape = AppShapes.XSmall,
                        border = BorderStroke(0.5.dp, Primary.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = item.episodeName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "• ${formatPosition(item.positionMs)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Estimate progress: positionMs / assumed-duration
                // We don't store duration, so we estimate based on typical runtime:
                //  - < 30min → likely cartoon short
                //  - 30-90min → movie
                //  - > 90min → long movie or drama episode
                // Cap progress visually at 95% to indicate "in progress" vs "done".
                val progress = remember(item.positionMs) {
                    val minutes = item.positionMs / 60_000
                    // Heuristic: assume episode length scales with minutes-watched.
                    val assumedTotalMin = when {
                        minutes < 5 -> 30.0
                        minutes < 30 -> 45.0
                        minutes < 60 -> 100.0
                        minutes < 120 -> 150.0
                        else -> 200.0
                    }
                    val ratio = (item.positionMs / 60_000.0) / assumedTotalMin
                    ratio.coerceIn(0.02, 0.95).toFloat()
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(AppGradients.ProgressGradient)
                    )
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Xem cuối: ${formatTime(item.updatedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val resumeInteraction = remember { MutableInteractionSource() }
                val resumePressed by resumeInteraction.collectIsPressedAsState()
                val resumeScale by animateFloatAsState(
                    targetValue = if (resumePressed) Motion.PressScale else 1f,
                    animationSpec = Motion.PressSpring,
                    label = "resumeScale"
                )
                Surface(
                    shape = AppShapes.Small,
                    color = Primary,
                    modifier = Modifier
                        .size(46.dp)
                        .scale(resumeScale)
                        .glowShadow(
                            color = Primary,
                            shape = AppShapes.Small,
                            glowRadius = 10.dp,
                            elevation = Elevation.S
                        )
                        .clickable(interactionSource = resumeInteraction, indication = null, onClick = onResume)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            FilmFlixIcons.PlayFilled,
                            contentDescription = "Xem tiếp",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(
                        FilmFlixIcons.TrashOutline,
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
