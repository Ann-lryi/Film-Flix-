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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nguoncflix.data.models.EpisodeData
import com.nguoncflix.ui.components.animateScaleOnPress
import com.nguoncflix.ui.navigation.Screen
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

    Box(modifier = Modifier.fillMaxSize().background(NetflixDark)) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = NetflixRed
                )
            }
            uiState.error != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Lỗi: ${uiState.error}", color = NetflixWhite)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Quay lại")
                    }
                }
            }
            uiState.movie != null -> {
                val movie = uiState.movie!!

                LazyColumn {
                    // Hero Banner
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                        ) {
                            AsyncImage(
                                model = movie.posterUrl,
                                contentDescription = movie.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, NetflixDark)
                                        )
                                    )
                            )

                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.TopStart)
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = NetflixWhite
                                )
                            }
                        }
                    }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            // Title + Info
                            Text(
                                text = movie.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = NetflixWhite,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(4.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                movie.year?.let {
                                    Text("$it", color = NetflixTextSecondary)
                                }
                                movie.quality?.let {
                                    Text(it, color = NetflixRed, fontWeight = FontWeight.Bold)
                                }
                                movie.episodeCurrent?.let {
                                    Text(it, color = NetflixTextSecondary)
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Play Button (with spring animation)
                            Button(
                                onClick = {
                                    // Play the first episode or movie
                                    val firstEpisode = uiState.episodes.firstOrNull()
                                    val url = firstEpisode?.linkM3u8 ?: firstEpisode?.linkEmbed
                                    if (!url.isNullOrEmpty()) {
                                        val intent = Intent(context, PlayerActivity::class.java).apply {
                                            putExtra("video_url", url)
                                            putExtra("title", movie.name)
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NetflixRed
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .animateScaleOnPress()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("PHÁT", fontWeight = FontWeight.Bold)
                            }

                            Spacer(Modifier.height(16.dp))

                            // Description
                            movie.content?.let {
                                Text(
                                    text = it,
                                    color = NetflixTextSecondary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(Modifier.height(20.dp))

                            // Episodes (Framer Motion staggered animation)
                            if (uiState.episodes.isNotEmpty()) {
                                Text(
                                    text = "Tập phim",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = NetflixWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }

                    // Episode list
                    if (uiState.episodes.isNotEmpty()) {
                        itemsIndexed(uiState.episodes) { index, episode ->
                            EpisodeItem(
                                episode = episode,
                                index = index,
                                movieTitle = movie.name,
                                onPlayClick = { url ->
                                    val intent = Intent(context, PlayerActivity::class.java).apply {
                                        putExtra("video_url", url)
                                        putExtra("title", "${movie.name} - ${episode.name}")
                                    }
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(100.dp))
                    }
                }
            }
        }

        BottomNavBar(
            currentRoute = "detail",
            onNavigate = { },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun EpisodeItem(
    episode: EpisodeData,
    index: Int,
    movieTitle: String,
    onPlayClick: (String) -> Unit
) {
    // Staggered entrance animation (simulate variants + staggeredChildren)
    val delay = (index * 35).toLong()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn(
            animationSpec = tween(280, delayMillis = delay.toInt(), easing = EaseOut)
        ),
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .animateScaleOnPress()
                .clickable {
                    val url = episode.linkM3u8 ?: episode.linkEmbed
                    if (!url.isNullOrEmpty()) {
                        onPlayClick(url)
                    }
                },
            colors = CardDefaults.cardColors(containerColor = NetflixDarkGray),
            shape = RoundedCornerShape(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(NetflixGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = NetflixWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = episode.name,
                        color = NetflixWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = movieTitle,
                        color = NetflixTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
