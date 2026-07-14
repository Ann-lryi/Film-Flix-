package com.nguoncflix.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nguoncflix.data.models.Movie
import com.nguoncflix.ui.navigation.Screen
import com.nguoncflix.ui.theme.*
import com.nguoncflix.viewmodel.HomeViewModel

@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixDark)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Hero Banner
            item {
                HeroBanner(
                    movie = uiState.featuredMovie,
                    onPlayClick = { movie ->
                        navController.navigate(Screen.MovieDetail.createRoute(movie.slug))
                    },
                    onInfoClick = { movie ->
                        navController.navigate(Screen.MovieDetail.createRoute(movie.slug))
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                MovieRow(
                    title = "Phim Mới Cập Nhật",
                    movies = uiState.newMovies,
                    onMovieClick = { movie ->
                        navController.navigate(Screen.MovieDetail.createRoute(movie.slug))
                    }
                )
            }

            item {
                MovieRow(
                    title = "Phim Bộ",
                    movies = uiState.seriesMovies,
                    onMovieClick = { movie ->
                        navController.navigate(Screen.MovieDetail.createRoute(movie.slug))
                    }
                )
            }

            item {
                MovieRow(
                    title = "Phim Lẻ",
                    movies = uiState.singleMovies,
                    onMovieClick = { movie ->
                        navController.navigate(Screen.MovieDetail.createRoute(movie.slug))
                    }
                )
            }

            item {
                MovieRow(
                    title = "TV Shows",
                    movies = uiState.tvShows,
                    onMovieClick = { movie ->
                        navController.navigate(Screen.MovieDetail.createRoute(movie.slug))
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        BottomNavBar(
            currentRoute = "home",
            onNavigate = { route ->
                when (route) {
                    "home" -> {}
                    "search" -> navController.navigate(Screen.Search.route)
                    "my_list" -> navController.navigate(Screen.MyList.route)
                    "profile" -> navController.navigate(Screen.Profile.route)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun HeroBanner(
    movie: Movie?,
    onPlayClick: (Movie) -> Unit,
    onInfoClick: (Movie) -> Unit
) {
    val imageUrl = movie?.posterUrl ?: "https://via.placeholder.com/800x450"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = movie?.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            NetflixDark.copy(alpha = 0.85f)
                        ),
                        startY = 200f
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            movie?.let {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.displayLarge,
                    color = NetflixWhite,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    it.year?.let { year ->
                        Text(
                            text = year.toString(),
                            color = NetflixTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    it.quality?.let { quality ->
                        Text(
                            text = quality,
                            color = NetflixRed,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    it.episodeCurrent?.let { ep ->
                        Text(
                            text = ep,
                            color = NetflixTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onPlayClick(it) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NetflixWhite,
                            contentColor = NetflixDark
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Phát",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    OutlinedButton(
                        onClick = { onInfoClick(it) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NetflixWhite
                        ),
                        border = BorderStroke(1.dp, NetflixWhite.copy(0.7f)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = "Thông tin",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MovieRow(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column {
        Text(
            text = title,
            color = NetflixWhite,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(movies.take(12)) { movie ->
                AnimatedMovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie) }
                )
            }
        }
    }
}

@Composable
fun AnimatedMovieCard(
    movie: Movie,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(130.dp)
            .height(190.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box {
            AsyncImage(
                model = movie.thumbUrl.takeIf { it.isNotBlank() } ?: movie.posterUrl,
                contentDescription = movie.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(0.6f)),
                            startY = 120f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = movie.name,
                    color = NetflixWhite,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Simple press animation (safe version - compatible with current Compose)
@Composable
fun Modifier.animateScaleOnPress(): Modifier {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable {
            isPressed = true
        }
}
