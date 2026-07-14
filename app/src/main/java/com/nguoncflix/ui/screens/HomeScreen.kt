package com.nguoncflix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nguoncflix.data.models.Movie
import com.nguoncflix.ui.components.PremiumMovieCard
import com.nguoncflix.ui.components.PullToRefreshBox
import com.nguoncflix.ui.components.ShimmerHeroBanner
import com.nguoncflix.ui.components.ShimmerMovieCard
import com.nguoncflix.ui.components.animateScaleOnPress
import com.nguoncflix.ui.navigation.Screen
import com.nguoncflix.ui.theme.*
import com.nguoncflix.viewmodel.HomeViewModel

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
                uiState.isLoading && uiState.newMovies.isEmpty() -> {
                    LoadingHomeContent()
                }
                uiState.error != null && uiState.newMovies.isEmpty() -> {
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { viewModel.refresh() }
                    )
                }
                else -> {
                    RealHomeContent(
                        uiState = uiState,
                        navController = navController
                    )
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
}

@Composable
private fun RealHomeContent(
    uiState: com.nguoncflix.viewmodel.HomeUiState,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Hero Banner
        item {
            uiState.featuredMovie?.let { featured ->
                HeroBanner(
                    movie = featured,
                    onPlayClick = {
                        navController.navigate(Screen.MovieDetail.createRoute(featured.slug))
                    },
                    onInfoClick = {
                        navController.navigate(Screen.MovieDetail.createRoute(featured.slug))
                    }
                )
            }
        }

        // Sections
        item {
            Spacer(modifier = Modifier.height(12.dp))
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
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun LoadingHomeContent() {
    LazyColumn {
        item { ShimmerHeroBanner() }
        item {
            Spacer(Modifier.height(16.dp))
            Text(
                "Phim Mới Cập Nhật",
                color = NetflixWhite,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(6) { ShimmerMovieCard() }
            }
        }
        item {
            Spacer(Modifier.height(20.dp))
            Text(
                "Phim Bộ",
                color = NetflixWhite,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
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
        Text("Không thể tải nội dung", color = NetflixWhite, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(message, color = NetflixTextSecondary, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = NetflixRed)) {
            Text("Thử lại")
        }
    }
}

@Composable
fun HeroBanner(
    movie: Movie,
    onPlayClick: (Movie) -> Unit,
    onInfoClick: (Movie) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp)
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
                        colors = listOf(Color.Transparent, Color.Transparent, NetflixDark.copy(alpha = 0.92f)),
                        startY = 180f
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = movie.name,
                style = MaterialTheme.typography.displayLarge,
                color = NetflixWhite,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                movie.year?.let { Text(it.toString(), color = NetflixTextSecondary) }
                movie.quality?.let { Text(it, color = NetflixRed, fontWeight = FontWeight.Bold) }
                movie.episodeCurrent?.let { Text(it, color = NetflixTextSecondary) }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onPlayClick(movie) },
                    colors = ButtonDefaults.buttonColors(containerColor = NetflixWhite, contentColor = NetflixDark),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(50.dp).weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Phát", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onInfoClick(movie) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NetflixWhite),
                    border = BorderStroke(1.dp, NetflixWhite.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(50.dp).weight(1f)
                ) {
                    Text("Thông tin", fontWeight = FontWeight.Bold)
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
            modifier = Modifier.padding(start = 16.dp, bottom = 10.dp, top = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
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
