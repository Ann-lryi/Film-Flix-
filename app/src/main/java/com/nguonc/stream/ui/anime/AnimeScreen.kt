package com.nguonc.stream.ui.anime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nguonc.stream.data.remote.AnimeVietsubApi
import com.nguonc.stream.ui.components.ErrorBox
import com.nguonc.stream.ui.components.LoadingBox
import com.nguonc.stream.ui.components.SectionHeader
import com.nguonc.stream.ui.components.ShimmerCard
import com.nguonc.stream.ui.theme.AccentViolet
import com.nguonc.stream.ui.theme.AppShapes
import com.nguonc.stream.ui.theme.GoldStar
import com.nguonc.stream.ui.theme.Primary

@Composable
fun AnimeScreen(
    onAnimeClick: (String) -> Unit,
    viewModel: AnimeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(AppShapes.Medium)
                            .background(Brush.linearGradient(listOf(AccentViolet, Primary))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("A", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    Text(
                        "ANIME",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    "Khám phá thế giới anime",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        when {
            state.isLoading -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { ShimmerCard(modifier = Modifier.fillMaxWidth().height(200.dp)) }
                    items(3) { ShimmerCard(modifier = Modifier.fillMaxWidth().height(140.dp)) }
                }
            }
            state.error != null -> ErrorBox(state.error!!, onRetry = viewModel::load)
            else -> LazyColumn(
                contentPadding = PaddingValues(bottom = 130.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (state.newAnime.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Anime Mới",
                            subtitle = "Cập nhật mới nhất",
                            onSeeMore = null,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                        Spacer(Modifier.height(14.dp))
                        AnimeRow(state.newAnime, onAnimeClick)
                    }
                }
                if (state.seriesAnime.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Anime Bộ",
                            subtitle = "TV Series",
                            onSeeMore = null,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                        Spacer(Modifier.height(14.dp))
                        AnimeRow(state.seriesAnime, onAnimeClick)
                    }
                }
                if (state.movieAnime.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Anime Lẻ",
                            subtitle = "Movie / OVA",
                            onSeeMore = null,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                        Spacer(Modifier.height(14.dp))
                        AnimeRow(state.movieAnime, onAnimeClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimeRow(
    animes: List<AnimeVietsubApi.AVSAnime>,
    onAnimeClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(animes, key = { it.slug }) { anime ->
            AnimeCard(anime = anime, onClick = { onAnimeClick(anime.slug) })
        }
    }
}

@Composable
private fun AnimeCard(
    anime: AnimeVietsubApi.AVSAnime,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(152.dp)
            .clickable(onClick = onClick)
    ) {
        Card(
            shape = AppShapes.Large,
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
        ) {
            Box {
                AsyncImage(
                    model = anime.posterUrl,
                    contentDescription = anime.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                // Bottom gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )
                // Score badge
                if (anime.score.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(AppShapes.XSmall)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            "★ ${anime.score}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GoldStar
                        )
                    }
                }
                // Episode badge
                if (anime.epCurrent.isNotBlank()) {
                    Surface(
                        shape = AppShapes.XSmall,
                        color = Primary,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            "Tập ${anime.epCurrent}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = anime.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (anime.year.isNotBlank()) {
            Text(
                anime.year,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
