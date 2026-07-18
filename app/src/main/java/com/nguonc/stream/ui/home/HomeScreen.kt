package com.nguonc.stream.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nguonc.stream.data.remote.dto.MovieItemDto
import com.nguonc.stream.ui.components.ErrorBox
import com.nguonc.stream.ui.components.HeroBannerCarousel
import com.nguonc.stream.ui.components.MoviePosterCard
import com.nguonc.stream.ui.components.SectionHeader
import com.nguonc.stream.ui.components.ShimmerPosterRow
import com.nguonc.stream.ui.theme.Aurora
import com.nguonc.stream.ui.theme.BrandCherry
import com.nguonc.stream.ui.theme.BrandCherrySoft
import com.nguonc.stream.ui.theme.OLED1
import com.nguonc.stream.ui.theme.OLED2
import com.nguonc.stream.ui.theme.OnDarkSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (String) -> Unit,
    onSeeMore: (listType: String, title: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient blobs — depth
        Box(
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-40).dp)
                .drawBehind {
                    drawCircle(
                        brush = Aurora.AmbientCherry,
                        radius = 400.dp.toPx()
                    )
                }
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-100).dp, y = 200.dp)
                .drawBehind {
                    drawCircle(
                        brush = Aurora.AmbientViolet,
                        radius = 320.dp.toPx()
                    )
                }
        )

        Column(modifier = Modifier.fillMaxSize()) {
            AuroraTopBar(
                onSearchClick = { onSeeMore("phim-moi-cap-nhat", "Phim mới") }
            )

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    val firstSectionMovies = state.sections.firstOrNull()?.items.orEmpty()
                    if (firstSectionMovies.isNotEmpty()) {
                        item {
                            HeroBannerCarousel(
                                movies = firstSectionMovies,
                                onMovieClick = onMovieClick,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    item { QuickFilterRow() }

                    // Top 10 — numbered strip từ section đầu tiên
                    if (firstSectionMovies.size >= 5) {
                        item {
                            Top10Strip(
                                movies = firstSectionMovies.take(10),
                                onMovieClick = onMovieClick,
                            )
                        }
                    }

                    items(state.sections, key = { it.listType }) { section ->
                        Column {
                            SectionHeader(
                                title = mapTitle(section.title),
                                subtitle = mapSubtitle(section.listType),
                                onSeeMore = { onSeeMore(section.listType, section.title) },
                                modifier = Modifier.padding(horizontal = 20.dp),
                            )
                            Spacer(Modifier.height(14.dp))
                            when {
                                section.isLoading -> ShimmerPosterRow()
                                section.error != null -> ErrorBox(
                                    message = section.error,
                                    onRetry = { viewModel.retrySection(section.listType) },
                                    modifier = Modifier.height(180.dp),
                                )
                                else -> LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    items(section.items, key = { it.id }) { movie ->
                                        MoviePosterCard(
                                            movie = movie,
                                            onClick = { onMovieClick(movie.slug) },
                                            modifier = Modifier.width(146.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { BrandFooter() }
                }
            }
        }
    }
}

// ======================================================
// TOP BAR — Aurora
// ======================================================
@Composable
private fun AuroraTopBar(onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Logo brand — vẽ tay chữ F
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Aurora.BrandLinear)
                    .shadow(10.dp, RoundedCornerShape(14.dp), ambientColor = BrandCherry.copy(alpha = 0.6f))
                    .border(
                        BorderStroke(0.5.dp, Color.White.copy(alpha = 0.18f)),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "F",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = Color.White
                )
            }
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "FILM FLIX",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.4).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(BrandCherry)
                    )
                }
                Text(
                    text = "Khám phá vũ trụ điện ảnh",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = OnDarkSurfaceVariant
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .height(42.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .clickable(onClick = onSearchClick)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Tìm kiếm",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(BrandCherry)
                            .border(1.5.dp, OLED1, CircleShape)
                    )
                }
            }
        }
    }
}

// ======================================================
// QUICK FILTER ROW
// ======================================================
@Composable
private fun QuickFilterRow() {
    val filters = listOf(
        "🔥 Thịnh hành" to true,
        "Phim mới" to false,
        "Top IMDb" to false,
        "4K HDR" to false,
        "Marvel" to false,
        "Anime" to false,
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(filters.size) { i ->
            val (label, selected) = filters[i]
            val isFire = label.contains("Thịnh")
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = if (selected) BrandCherry
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                border = BorderStroke(
                    1.dp,
                    if (selected) Color.Transparent
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .clickable { }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (isFire) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (selected) Color.White else Color(0xFFFF6A00),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// ======================================================
// TOP 10 STRIP — Số khổng lồ phía sau poster
// ======================================================
@Composable
private fun Top10Strip(
    movies: List<MovieItemDto>,
    onMovieClick: (String) -> Unit,
) {
    Column {
        SectionHeader(
            title = "Top 10 hôm nay",
            subtitle = "Được xem nhiều nhất 24h qua",
            onSeeMore = null,
            icon = Icons.Filled.LocalFireDepartment,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(movies.take(10).withIndex().toList(), key = { it.value.id }) { (idx, movie) ->
                Top10Item(
                    rank = idx + 1,
                    movie = movie,
                    onClick = { onMovieClick(movie.slug) },
                )
            }
        }
    }
}

@Composable
private fun Top10Item(
    rank: Int,
    movie: MovieItemDto,
    onClick: () -> Unit,
) {
    val interaction = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "top10Scale"
    )

    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp)
    ) {
        // Số khổng lồ gradient — dùng Box drawBehind + Canvas
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 110.dp)
                .offset(x = (-6).dp, y = 8.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    textSize = this.size.height * 1.15f
                    typeface = android.graphics.Typeface.create(
                        android.graphics.Typeface.DEFAULT,
                        android.graphics.Typeface.BOLD
                    )
                }
                paint.shader = android.graphics.LinearGradient(
                    0f, 0f, 0f, this.size.height,
                    intArrayOf(
                        Color(0xFFFF1F4A).copy(alpha = 0.20f).toArgb(),
                        Color(0xFFFF1F4A).copy(alpha = 0.02f).toArgb()
                    ),
                    null,
                    android.graphics.Shader.TileMode.CLAMP
                )
                drawContext.canvas.nativeCanvas.drawText(
                    rank.toString(),
                    0f,
                    this.size.height - paint.fontMetrics.descent,
                    paint
                )
            }
        }
        // Poster nhỏ
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(160.dp)
                .offset(x = (-30).dp)
                .shadow(12.dp, RoundedCornerShape(14.dp), ambientColor = Color.Black.copy(alpha = 0.6f))
                .clip(RoundedCornerShape(14.dp))
                .background(OLED2)
        ) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun BrandFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth(0.6f)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "FILM FLIX 3.0",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Cinema re-imagined",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

private fun mapTitle(raw: String): String = when {
    raw.contains("Phim mới", ignoreCase = true) -> "Mới ra rạp"
    raw.contains("Phim lẻ", ignoreCase = true) -> "Phim lẻ đặc sắc"
    raw.contains("Phim bộ", ignoreCase = true) -> "Phim bộ hot"
    raw.contains("hoạt hình", ignoreCase = true) || raw.contains("anime", ignoreCase = true) -> "Hoạt hình"
    raw.contains("thuyết minh", ignoreCase = true) -> "Lồng tiếng Việt"
    raw.contains("vietsub", ignoreCase = true) -> "Phụ đề Việt"
    else -> raw
}

private fun mapSubtitle(listType: String): String = when {
    listType.contains("phim-le", true) -> "Chọn lọc kỹ lưỡng"
    listType.contains("phim-bo", true) -> "Dài tập hấp dẫn"
    listType.contains("hoat-hinh", true) -> "Cho mọi lứa tuổi"
    listType.contains("viet-sub", true) -> "Sub tiếng Việt chuẩn"
    listType.contains("thuyet-minh", true) -> "Lồng tiếng chất lượng cao"
    else -> "Cập nhật liên tục"
}
