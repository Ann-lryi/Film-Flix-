package com.nguoncflix.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import com.nguoncflix.ui.components.SectionHeader
import com.nguoncflix.ui.components.ShimmerHeroBanner
import com.nguoncflix.ui.components.ShimmerMovieCard
import com.nguoncflix.ui.navigation.Screen
import com.nguoncflix.ui.theme.*
import com.nguoncflix.viewmodel.HomeViewModel
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.delay

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
                .padding(top = 48.dp)   // Status bar breathing room
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
        // Premium Cinematic Hero
        item {
            uiState.featuredMovie?.let { movie ->
                CinematicHeroBanner(
                    movie = movie,
                    onPlayClick = { navController.navigate(Screen.MovieDetail.createRoute(movie.slug)) },
                    onInfoClick = { navController.navigate(Screen.MovieDetail.createRoute(movie.slug)) }
                )
            }
        }

        item { Spacer(Modifier.height(32.dp)) }

        // Sections with beautiful headers
        item {
            PremiumSection(
                title = "Phim Mới Cập Nhật",
                movies = uiState.newMovies,
                onMovieClick = { navController.navigate(Screen.MovieDetail.createRoute(it.slug)) }
            )
        }

        item { Spacer(Modifier.height(36.dp)) }

        item {
            PremiumSection(
                title = "Phim Bộ",
                movies = uiState.seriesMovies,
                onMovieClick = { navController.navigate(Screen.MovieDetail.createRoute(it.slug)) }
            )
        }

        item { Spacer(Modifier.height(36.dp)) }

        item {
            PremiumSection(
                title = "Phim Lẻ",
                movies = uiState.singleMovies,
                onMovieClick = { navController.navigate(Screen.MovieDetail.createRoute(it.slug)) }
            )
        }

        item { Spacer(Modifier.height(36.dp)) }

        item {
            PremiumSection(
                title = "TV Shows & Anime",
                movies = uiState.tvShows,
                onMovieClick = { navController.navigate(Screen.MovieDetail.createRoute(it.slug)) }
            )
        }

        item { Spacer(Modifier.height(160.dp)) }
    }
}

@Composable
private fun PremiumSection(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column {
        SectionHeader(title = title)

        if (movies.isEmpty()) {
            // Graceful empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Đang tải thêm nội dung...",
                    color = NetflixTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                itemsIndexed(movies) { index, movie ->
                    // Staggered entrance animation (Framer Motion style)
                    var visible by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        delay(index * 45L) // Staggered delay
                        visible = true
                    }

                    val animatedAlpha by animateFloatAsState(
                        targetValue = if (visible) 1f else 0f,
                        animationSpec = tween(380, easing = FastOutSlowInEasing),
                        label = "card_alpha"
                    )

                    val animatedOffset by animateFloatAsState(
                        targetValue = if (visible) 0f else 30f,
                        animationSpec = spring(
                            dampingRatio = 0.78f,
                            stiffness = 180f
                        ),
                        label = "card_offset"
                    )

                    Box(
                        modifier = Modifier.graphicsLayer {
                            alpha = animatedAlpha
                            translationY = animatedOffset
                        }
                    ) {
                        PremiumMovieCard(
                            movie = movie,
                            onClick = { onMovieClick(movie) }
                        )
                    }
                }
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
            .height(88.dp)   // Compact hero — does NOT dominate the screen
    ) {
        // Background image
        AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Rich multi-layer gradient (iQIYI / Tencent premium style)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.75f),
                            Color.Black.copy(alpha = 0.96f)
                        ),
                        startY = 90f
                    )
                )
        )

        // Bottom content area
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
        ) {
            // Title - compact cinematic (fits small hero)
            Text(
                text = movie.name,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    letterSpacing = (-0.6).sp,
                    lineHeight = 25.sp
                ),
                color = NetflixWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            // Metadata row (premium)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                movie.year?.let {
                    Text(
                        text = it.toString(),
                        color = NetflixTextSecondary,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
                    )
                }

                movie.quality?.let {
                    Text(
                        text = "•",
                        color = NetflixTextSecondary.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                    Box(
                        modifier = Modifier
                            .background(NetflixRed.copy(alpha = 0.9f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = it,
                            color = NetflixWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                movie.episodeCurrent?.let {
                    Text(
                        text = "•",
                        color = NetflixTextSecondary.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = it,
                        color = NetflixTextSecondary,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Premium action buttons (Apple + Samsung polish)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Play button - dominant white (Apple style)
                Button(
                    onClick = { onPlayClick(movie) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetflixWhite,
                        contentColor = NetflixDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .weight(1f)
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "PHÁT NGAY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Info button - elegant outline
                OutlinedButton(
                    onClick = { onInfoClick(movie) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NetflixWhite
                    ),
                    border = BorderStroke(1.8.dp, NetflixWhite.copy(alpha = 0.75f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .weight(1f)
                ) {
                    Text(
                        "CHI TIẾT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
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
            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Phim Mới Cập Nhật")
            Spacer(Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(6) { ShimmerMovieCard() }
            }
        }

        item {
            Spacer(Modifier.height(30.dp))
            SectionHeader(title = "Phim Bộ")
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
        Text("⚠️", fontSize = 52.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Đã xảy ra lỗi",
            color = NetflixWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(message, color = NetflixTextSecondary)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Thử lại")
        }
    }
}
