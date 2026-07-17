package com.aho.yunphim.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.aho.yunphim.data.model.EpisodeItem
import com.aho.yunphim.data.model.MovieDetail
import com.aho.yunphim.data.model.ServerGroup
import com.aho.yunphim.ui.components.FullScreenError
import com.aho.yunphim.ui.components.FullScreenLoading
import com.aho.yunphim.ui.theme.YunPhimColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    onPlayEpisode: (serverIndex: Int, episodeIndex: Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedServerIndex by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(YunPhimColors.Background)) {
        when {
            state.isLoading -> FullScreenLoading()

            state.error != null -> FullScreenError(
                message = state.error.orEmpty(),
                isSchemaMismatch = state.isSchemaMismatch,
                onRetry = viewModel::retry,
            )

            state.detail != null -> {
                val detail = state.detail!!
                val servers = detail.servers
                val currentEpisodes = servers.getOrNull(selectedServerIndex)?.episodes.orEmpty()

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item { HeroSection(detail = detail, onBack = onBack) }

                    item {
                        if (currentEpisodes.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            PlayCta(onClick = { onPlayEpisode(selectedServerIndex, 0) })
                        }
                    }

                    item { InfoSection(detail = detail) }

                    if (servers.isNotEmpty()) {
                        item {
                            ServerTabs(
                                servers = servers,
                                selectedIndex = selectedServerIndex,
                                onSelect = { selectedServerIndex = it },
                            )
                        }
                        item {
                            EpisodeGrid(
                                episodes = currentEpisodes,
                                onEpisodeClick = { episodeIndex ->
                                    onPlayEpisode(selectedServerIndex, episodeIndex)
                                },
                            )
                        }
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HeroSection(detail: MovieDetail, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f)) {
        AsyncImage(
            model = detail.displayBackdrop,
            contentDescription = detail.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to YunPhimColors.ScrimTop,
                        0.35f to Color.Transparent,
                        1f to YunPhimColors.Background,
                    ),
                ),
            ),
        )
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f)),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color.White,
            )
        }
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Text(
                text = detail.name.orEmpty(),
                style = MaterialTheme.typography.headlineLarge,
                color = YunPhimColors.TextPrimary,
            )
            if (!detail.originName.isNullOrBlank()) {
                Text(
                    text = detail.originName.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = YunPhimColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun PlayCta(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = YunPhimColors.Accent),
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
        Spacer(Modifier.width(8.dp))
        Text("Xem ngay", color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoSection(detail: MovieDetail) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            detail.quality?.takeIf { it.isNotBlank() }?.let { MetaChip(it) }
            detail.language?.takeIf { it.isNotBlank() }?.let { MetaChip(it) }
            detail.time?.takeIf { it.isNotBlank() }?.let { MetaChip(it) }
        }
        if (!detail.description.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            var expanded by remember { mutableStateOf(false) }
            Text(
                text = htmlToPlainText(detail.description),
                style = MaterialTheme.typography.bodyMedium,
                color = YunPhimColors.TextSecondary,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { expanded = !expanded },
            )
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    Surface(shape = RoundedCornerShape(4.dp), color = YunPhimColors.SurfaceVariant) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = YunPhimColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun ServerTabs(servers: List<ServerGroup>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(servers) { index, server ->
            val isSelected = index == selectedIndex
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) YunPhimColors.Accent else YunPhimColors.Surface,
                modifier = Modifier.clickable { onSelect(index) },
            ) {
                Text(
                    text = server.displayName?.takeIf { it.isNotBlank() } ?: "Server ${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Color.White else YunPhimColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EpisodeGrid(episodes: List<EpisodeItem>, onEpisodeClick: (Int) -> Unit) {
    // FlowRow thay vì LazyVerticalGrid lồng trong LazyColumn - tránh crash "vertically
    // scrollable component measured with infinity maximum height" khi lồng 2 lazy container.
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        episodes.forEachIndexed { index, ep ->
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = YunPhimColors.Surface,
                modifier = Modifier.clickable { onEpisodeClick(index) },
            ) {
                Text(
                    text = ep.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = YunPhimColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        }
    }
}

/** content từ API có thể chứa thẻ HTML (phổ biến ở họ API này) - parse an toàn, no-op nếu là text thuần. */
private fun htmlToPlainText(raw: String): String =
    HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim()
