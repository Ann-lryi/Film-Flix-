package com.nguoncflix.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        ) {
            when {
                uiState.isLoading && uiState.newMovies.isEmpty() -> LoadingHomeContent()
                uiState.error != null && uiState.newMovies.isEmpty() ->
                    ErrorState(uiState.error!!) { viewModel.refresh() }
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
private fun RealHomeContent(
    uiState: com.nguoncflix.viewmodel.HomeUiState,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Cinematic hero carousel
        item {
            CinematicHeroCarousel(
                movies = uiState.featuredMovies,
                onPlayClick = { movie ->
                    navController.navigate(Screen.MovieDetail.createRoute(movie.slug))
                },
                onInfoClick = { movie ->
                    navController.navigate(Screen.MovieDetail.createRoute(movie.slug))
                }
            )
        }

        item { Spacer(Modifier.height(24.dp)) }

        item {
            PremiumSection(
                title = "Phim Mới Cập Nhật",
                movies = uiState.newMovies,
                onMovieClick = { navController.navigate(Screen.MovieDetail.createRoute(it.slug)) }
            )
        }

        item { Spacer(Modifier.height(32.dp)) }

        item {
            PremiumSection(
                title = "Phim Bộ",
                movies = uiState.seriesMovies,
                onMovieClick = { navController.navigate(Screen.MovieDetail.createRoute(it.slug)) }
            )
        }

        item { Spacer(Modifier.height(32.dp)) }

        item {
            PremiumSection(
                title = "Phim Lẻ",
                movies = uiState.singleMovies,
                onMovieClick = { navController.navigate(Screen.MovieDetail.createRoute(it.slug)) }
            )
        }

        item { Spacer(Modifier.height(32.dp)) }

        item {
            PremiumSection(
                title = "TV Shows & Anime",
                movies = uiState.tvShows,
                onMovieClick = { navController.navigate(Screen.MovieDetail.createRoute(it.slug)) }
            )
        }

        item { Spacer(Modifier.height(120.dp)) }
    }
}

@Composable
private fun CinematicHeroCarousel(
    movies: List<Movie>,
    onPlayClick: (Movie) -> Unit,
    onInfoClick: (Movie) -> Unit
) {
    if (movies.isEmpty()) return

    val listState = rememberLazyListState()
    var currentIndex by remember { mutableStateOf(0) }

    // Track scroll position to update indicator
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val first = info.visibleItemsInfo.firstOrNull() ?: return@snapshotFlow 0
            val offset = -first.offset.toFloat() / first.size.toFloat()
            (first.index + offset).toInt().coerceIn(0, movies.lastIndex)
        }.collect { idx ->
            if (idx != currentIndex) currentIndex = idx
        }
    }

    // Auto-scroll every 6s
    LaunchedEffect(movies.size) {
        if (movies.size <= 1) return@LaunchedEffect
        while (true) {
            delay(6000)
            val next = (currentIndex + 1) % movies.size
            listState.animateScrollToItem(next)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(540.dp)  // Tall cinematic hero
    ) {
        // Background pages (with scale + alpha for depth)
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) {
            itemsIndexed(movies, key = { _, m -> m.id }) { index, movie ->
                HeroPage(
                    movie = movie,
                    isActive = index == currentIndex,
                    onPlayClick = onPlayClick,
                    onInfoClick = onInfoClick
                )
            }
        }

        // Page indicator
        if (movies.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(movies.size) { i ->
                    val active = i == currentIndex
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (active) 22.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) NetflixRed
                                else NetflixWhite.copy(alpha = 0.45f)
                            )
                            .graphicsLayer {
                                alpha = if (active) 1f else 0.7f
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroPage(
    movie: Movie,
    isActive: Boolean,
    onPlayClick: (Movie) -> Unit,
    onInfoClick: (Movie) -> Unit
) {
    Box(
        modifier = Modifier
            .fillParentMaxSize()
            .background(NetflixDarkGray)
    ) {
        // Image
        AsyncImage(
            model = movie.thumbUrl.takeIf { it.isNotBlank() } ?: movie.posterUrl,
            contentDescription = movie.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Subtle parallax-like effect
                    scaleX = if (isActive) 1.04f else 1.0f
                    scaleY = if (isActive) 1.04f else 1.0f
                    alpha = if (isActive) 1f else 0.85f
                }
        )

        // Cinematic gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.78f),
                            NetflixDark
                        ),
                        startY = 200f
                    )
                )
        )

        // Top fade for status bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            NetflixDark.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 56.dp)
        ) {
            // Quality badge row
            Row(verticalAlignment = Alignment.CenterVertically) {
                movie.quality?.takeIf { it.isNotBlank() }?.let { q ->
                    Box(
                        modifier = Modifier
                            .background(NetflixRed, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            q,
                            color = NetflixWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                if (movie.year != null || movie.episodeCurrent != null) {
                    Spacer(Modifier.width(8.dp))
                }
                movie.year?.let {
                    Text(
                        it.toString(),
                        color = NetflixWhite.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                movie.episodeCurrent?.let {
                    Text(
                        "  •  $it",
                        color = NetflixWhite.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Title
            Text(
                text = movie.name,
                color = NetflixWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 36.sp,
                letterSpacing = (-0.8).sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Origin name
            movie.originName?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    it,
                    color = NetflixTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(20.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Play - white dominant
                Button(
                    onClick = { onPlayClick(movie) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetflixWhite,
                        contentColor = NetflixDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1.2f)
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = NetflixDark
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Phát",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                // My list - subtle
                IconButton(
                    onClick = { /* TODO: add to list */ },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NetflixWhite.copy(alpha = 0.18f))
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Thêm vào danh sách",
                        tint = NetflixWhite
                    )
                }

                // Info - subtle
                IconButton(
                    onClick = { onInfoClick(movie) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NetflixWhite.copy(alpha = 0.18f))
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Chi tiết",
                        tint = NetflixWhite
                    )
                }
            }
        }
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                itemsIndexed(movies, key = { _, m -> m.id }) { index, movie ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(movie.id) {
                        delay(index * 45L)
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
private fun LoadingHomeContent() {
    LazyColumn {
        item { ShimmerHeroBanner() }

        item {
            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Phim Mới Cập Nhật")
            Spacer(Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(6) { ShimmerMovieCard() }
            }
        }

        item { Spacer(Modifier.height(120.dp)) }
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
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(NetflixRed.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text("⚠️", fontSize = 44.sp)
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Đã xảy ra lỗi",
            color = NetflixWhite,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            color = NetflixTextSecondary,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(46.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Thử lại", fontWeight = FontWeight.Bold)
        }
    }
}
