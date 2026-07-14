package com.nguoncflix.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nguoncflix.data.models.Movie
import com.nguoncflix.ui.components.PremiumMovieCard
import com.nguoncflix.ui.components.PullToRefreshBox
import com.nguoncflix.ui.components.ShimmerHeroBanner
import com.nguoncflix.ui.components.ShimmerMovieCard
import com.nguoncflix.ui.navigation.Screen
import com.nguoncflix.ui.theme.*
import com.nguoncflix.viewmodel.HomeViewModel
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NetflixDark)
        ) {
            when {
                uiState.isLoading && uiState.newMovies.isEmpty() -> LoadingHomeContent()
                uiState.error != null && uiState.newMovies.isEmpty() -> ErrorState(uiState.error!!) { viewModel.refresh() }
                else -> RealHomeContent(uiState, navController)
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
}

@Composable
private fun RealHomeContent(uiState: com.nguoncflix.viewmodel.HomeUiState, navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Premium Hero Banner (iQIYI / Tencent style)
        item {
            uiState.featuredMovie?.let { movie ->
                CinematicHeroBanner(
                    movie = movie,
                    onPlayClick = { navController.navigate(Screen.MovieDetail.createRoute(movie.slug)) },
                    onInfoClick = { navController.navigate(Screen.MovieDetail.createRoute(movie.slug)) }
                )
            }
        }

        item { Spacer(Modifier.height(28.dp)) }

        // Sections with elegant headers
        item {
            ElegantSection(
                title = "Phim Mới Cập Nhật",
                movies = uiState.newMovies,
                onMovieClick = { navController.navigate(Screen.MovieDetail.createRoute(it.slug)) }
            )
        }

        item { Spacer(Modifier.height(32.dp)) }

        item {
            ElegantSection(
                title = "Phim Bộ",
                movies = uiState.seriesMovies,
                onMovieClick = { navController.navigate(Screen.MovieDetail.createRoute(it.slug)) }
            )
        }

        item { Spacer(Modifier.height(32.dp)) }

        item {
            ElegantSection(
                title = "Phim Lẻ",
                movies = uiState.singleMovies,
                onMovieClick = { navController.navigate(Screen.MovieDetail.createRoute(it.slug)) }
            )
        }

        item { Spacer(Modifier.height(32.dp)) }

        item {
            ElegantSection(
                title = "TV Shows & Anime",
                movies = uiState.tvShows,
                onMovieClick = { navController.navigate(Screen.MovieDetail.createRoute(it.slug)) }
            )
        }

        item { Spacer(Modifier.height(140.dp)) }
    }
}

@Composable
private fun ElegantSection(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = NetflixWhite,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Text(
                "Tất cả",
                color = NetflixRed,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            items(movies) { movie ->
                PremiumMovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie) }
                )
            }
        }
    }
}

@Composable
private fun CinematicHeroBanner(
    movie: Movie,
    onPlayClick: (Movie) -> Unit,
    onInfoClick: (Movie) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(580.dp)
    ) {
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Ultra rich cinematic overlay (Apple + Chinese premium drama style)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.95f)
                        ),
                        startY = 80f
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 24.dp, vertical = 42.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = movie.name,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.5).sp,
                    fontSize = 32.sp
                ),
                color = NetflixWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                movie.year?.let {
                    Text(
                        it.toString(),
                        color = NetflixTextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                movie.quality?.let {
                    Text(
                        it,
                        color = NetflixRed,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                movie.episodeCurrent?.let {
                    Text(
                        it,
                        color = NetflixTextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                // Play button - Apple style
                Button(
                    onClick = { onPlayClick(movie) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetflixWhite,
                        contentColor = NetflixDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("PHÁT NGAY", fontWeight = FontWeight.Bold)
                }

                // Info button - Samsung/Apple outline style
                OutlinedButton(
                    onClick = { onInfoClick(movie) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NetflixWhite),
                    border = BorderStroke(1.5.dp, NetflixWhite.copy(alpha = 0.65f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .weight(1f)
                ) {
                    Text("CHI TIẾT", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LoadingHomeContent() {
    LazyColumn {
        item { ShimmerHeroBanner() }
        item {
            Spacer(Modifier.height(28.dp))
            Text("Phim Mới Cập Nhật", color = NetflixWhite, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(6) { ShimmerMovieCard() }
            }
        }
        item {
            Spacer(Modifier.height(32.dp))
            Text("Phim Bộ", color = NetflixWhite, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(6) { ShimmerMovieCard() }
            }
        }
        item { Spacer(Modifier.height(100.dp)) }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚠️", fontSize = MaterialTheme.typography.displayLarge.fontSize)
        Spacer(Modifier.height(16.dp))
        Text("Đã xảy ra lỗi", color = NetflixWhite, fontWeight = FontWeight.Bold)
        Text(message, color = NetflixTextSecondary)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = NetflixRed)) {
            Text("Thử lại")
        }
    }
}
