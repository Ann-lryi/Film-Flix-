package com.nguoncflix.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nguoncflix.data.models.EpisodeData
import com.nguoncflix.ui.components.animateScaleOnPress
import com.nguoncflix.ui.player.PlayerActivity
import com.nguoncflix.ui.theme.*
import com.nguoncflix.viewmodel.MovieDetailViewModel

@Composable
fun MovieDetailScreen(
    slug: String,
    navController: NavController
) {
    val viewModel: MovieDetailViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(slug) {
        viewModel.fetchMovieDetail(slug)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixDark)
    ) {
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = NetflixRed,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Đang tải chi tiết phim...",
                            color = NetflixTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚠️", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Không thể tải phim",
                        color = NetflixWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        uiState.error ?: "",
                        color = NetflixTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.fetchMovieDetail(slug) },
                        colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Thử lại", fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Quay lại", color = NetflixTextSecondary)
                    }
                }
            }

            uiState.movie != null -> {
                val movie = uiState.movie!!

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Cinematic hero
                    item {
                        CinematicHero(
                            posterUrl = movie.posterUrl,
                            thumbUrl = movie.thumbUrl,
                            name = movie.name,
                            originName = movie.originName,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // Title + actions
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(top = 20.dp)
                        ) {
                            // Origin name (above main title)
                            movie.originName?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    it,
                                    color = NetflixTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                            }

                            Text(
                                text = movie.name,
                                color = NetflixWhite,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 32.sp,
                                letterSpacing = (-0.6).sp
                            )

                            Spacer(Modifier.height(14.dp))

                            // Metadata row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                movie.year?.let {
                                    Text(
                                        "$it",
                                        color = NetflixTextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                movie.quality?.takeIf { it.isNotBlank() }?.let { q ->
                                    Box(
                                        modifier = Modifier
                                            .background(NetflixRed, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            q,
                                            color = NetflixWhite,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                                movie.episodeCurrent?.let {
                                    Text(
                                        it,
                                        color = NetflixTextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                movie.lang?.let {
                                    Text(
                                        it,
                                        color = NetflixTextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            // Action row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        val firstEpisode = uiState.episodes.firstOrNull()
                                        val url = firstEpisode?.linkM3u8
                                            ?: firstEpisode?.linkEmbed
                                        if (!url.isNullOrEmpty()) {
                                            val intent = Intent(
                                                context, PlayerActivity::class.java
                                            ).apply {
                                                putExtra("video_url", url)
                                                putExtra("title", movie.name)
                                            }
                                            context.startActivity(intent)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NetflixRed,
                                        contentColor = NetflixWhite
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.4f)
                                        .height(50.dp)
                                        .shadow(6.dp, RoundedCornerShape(12.dp))
                                        .animateScaleOnPress()
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (uiState.episodes.isEmpty()) "PHÁT" else "XEM PHIM",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                }
                                CircleIconButton(icon = Icons.Default.Add, contentDescription = "Thêm")
                                CircleIconButton(icon = Icons.Default.Share, contentDescription = "Chia sẻ")
                            }
                        }
                    }

                    // Description (expandable)
                    if (!movie.content.isNullOrBlank()) {
                        item {
                            ExpandableDescription(
                                content = movie.content!!,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
                            )
                        }
                    }

                    // Categories
                    if (!movie.categories.isNullOrEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                SectionTitle("Thể loại")
                                Spacer(Modifier.height(10.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    itemsIndexed(
                                        items = movie.categories!!,
                                        key = { _, c -> c.slug }
                                    ) { _, cat ->
                                        CategoryChip(cat.name)
                                    }
                                }
                            }
                        }
                    }

                    // Countries
                    if (!movie.countries.isNullOrEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                SectionTitle("Quốc gia")
                                Spacer(Modifier.height(10.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    itemsIndexed(
                                        items = movie.countries!!,
                                        key = { _, c -> c.slug }
                                    ) { _, c ->
                                        CategoryChip(c.name)
                                    }
                                }
                            }
                        }
                    }

                    // Cast & Director
                    if (!movie.actor.isNullOrEmpty() || !movie.director.isNullOrEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                movie.director?.takeIf { it.isNotEmpty() }?.let { dirs ->
                                    PeopleRow(label = "Đạo diễn", names = dirs)
                                }
                                movie.actor?.takeIf { it.isNotEmpty() }?.let { actors ->
                                    PeopleRow(label = "Diễn viên", names = actors)
                                }
                            }
                        }
                    }

                    // Episodes grid
                    if (uiState.episodes.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    SectionTitle("Tập phim")
                                    Spacer(Modifier.weight(1f))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(NetflixGray.copy(alpha = 0.4f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "${uiState.episodes.size} tập",
                                            color = NetflixTextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Spacer(Modifier.height(14.dp))
                            }
                        }

                        // Episode grid (3 cols)
                        item {
                            val chunked = uiState.episodes.chunked(3)
                            chunked.forEachIndexed { rowIdx, row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEachIndexed { colIdx, episode ->
                                        val globalIdx = rowIdx * 3 + colIdx
                                        EpisodeGridItem(
                                            episode = episode,
                                            index = globalIdx,
                                            movieTitle = movie.name,
                                            modifier = Modifier.weight(1f),
                                            onPlayClick = { url ->
                                                val intent = Intent(
                                                    context, PlayerActivity::class.java
                                                ).apply {
                                                    putExtra("video_url", url)
                                                    putExtra(
                                                        "title",
                                                        "${movie.name} - ${episode.name}"
                                                    )
                                                }
                                                context.startActivity(intent)
                                            }
                                        )
                                    }
                                    // Fill remaining if row is incomplete
                                    repeat(3 - row.size) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                            Spacer(Modifier.height(20.dp))
                        }
                    }

                    // Bottom info
                    item {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            SectionTitle("Thông tin phim")
                            Spacer(Modifier.height(12.dp))
                            InfoRow("Tên gốc", movie.originName ?: "—")
                            InfoRow("Năm phát hành", movie.year?.toString() ?: "—")
                            InfoRow("Chất lượng", movie.quality ?: "—")
                            InfoRow("Ngôn ngữ", movie.lang ?: "—")
                            InfoRow("Trạng thái", movie.episodeCurrent ?: "—")
                            if (movie.episodeTotal != null && movie.episodeTotal!! > 0) {
                                InfoRow("Tổng số tập", movie.episodeTotal.toString())
                            }
                        }
                    }

                    item { Spacer(Modifier.height(120.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CinematicHero(
    posterUrl: String,
    thumbUrl: String,
    name: String,
    originName: String?,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        AsyncImage(
            model = thumbUrl.takeIf { it.isNotBlank() } ?: posterUrl,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Strong cinematic gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.85f),
                            NetflixDark
                        ),
                        startY = 80f
                    )
                )
        )

        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 40.dp, start = 12.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = NetflixWhite
            )
        }
    }
}

@Composable
private fun ExpandableDescription(
    content: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var canExpand by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = content,
            color = NetflixWhite.copy(alpha = 0.88f),
            fontSize = 14.sp,
            lineHeight = 21.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!canExpand && result.hasVisualOverflow) canExpand = true
            }
        )
        if (canExpand) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (expanded) "Thu gọn" else "Xem thêm",
                    color = NetflixRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    if (expanded) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = NetflixRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = NetflixWhite,
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.2).sp
    )
}

@Composable
private fun CategoryChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NetflixDarkGray.copy(alpha = 0.7f))
            .border(
                width = 0.8.dp,
                color = NetflixGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = NetflixWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PeopleRow(label: String, names: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = NetflixTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            names.joinToString(" • "),
            color = NetflixWhite,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            color = NetflixTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            color = NetflixWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun CircleIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?
) {
    IconButton(
        onClick = { /* future */ },
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(NetflixDarkGray.copy(alpha = 0.7f))
            .border(
                width = 0.8.dp,
                color = NetflixGray.copy(alpha = 0.5f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = NetflixWhite,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun EpisodeGridItem(
    episode: EpisodeData,
    index: Int,
    movieTitle: String,
    modifier: Modifier = Modifier,
    onPlayClick: (String) -> Unit
) {
    // Subtle staggered entrance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(episode.slug) {
        kotlinx.coroutines.delay((index * 25L).coerceAtMost(400))
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { 30 }),
        exit = fadeOut()
    ) {
        Box(
            modifier = modifier
                .height(54.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NetflixDarkGray.copy(alpha = 0.7f))
                .border(
                    width = 0.8.dp,
                    color = NetflixGray.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable {
                    val url = episode.linkM3u8 ?: episode.linkEmbed
                    if (!url.isNullOrEmpty()) onPlayClick(url)
                }
                .animateScaleOnPress(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = NetflixRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = episode.name,
                    color = NetflixWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
