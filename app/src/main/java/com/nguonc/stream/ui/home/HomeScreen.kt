package com.nguonc.stream.ui.home

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nguonc.stream.ui.components.ErrorBox
import com.nguonc.stream.ui.components.HeroBannerCarousel
import com.nguonc.stream.ui.components.MoviePosterCard
import com.nguonc.stream.ui.components.SectionHeader
import com.nguonc.stream.ui.components.ShimmerPosterRow
import com.nguonc.stream.ui.theme.AppGradients
import com.nguonc.stream.ui.theme.Primary

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
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopEnd)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Primary.copy(alpha = 0.18f), Color.Transparent)
                        ),
                        radius = 400.dp.toPx()
                    )
                }
        )

        Column(modifier = Modifier.fillMaxSize()) {
            PremiumHomeTopBar(
                onSearchClick = { onSeeMore("phim-moi-cap-nhat", "Phim mới") }
            )

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
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
                                            modifier = Modifier.width(152.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(AppGradients.PrimaryGradient),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Bolt, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "FILM FLIX • CINEMATIC UNIVERSE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumHomeTopBar(onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(AppGradients.PrimaryGradient).shadow(8.dp, RoundedCornerShape(13.dp), ambientColor = Primary.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "F", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Black)
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "FILM FLIX", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.3).sp), color = MaterialTheme.colorScheme.onBackground)
                    Surface(color = Primary, shape = CircleShape, modifier = Modifier.size(6.dp)) {}
                }
                Text(text = "Khám phá vũ trụ điện ảnh", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.height(42.dp).clip(RoundedCornerShape(100.dp)).clickable(onClick = onSearchClick)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Text("Tìm kiếm", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Filled.Notifications, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(8.dp).clip(CircleShape).background(Primary).border(1.5.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape))
                }
            }
        }
    }
}

@Composable
private fun QuickFilterRow() {
    val filters = listOf("🔥 Thịnh hành" to true, "Phim mới" to false, "Top IMDb" to false, "4K HDR" to false, "Marvel" to false, "Anime" to false)
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(filters.size) { i ->
            val (label, selected) = filters[i]
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = if (selected) Primary else MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.clip(RoundedCornerShape(100.dp)).clickable { }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (label.contains("Thịnh")) {
                        Icon(Icons.Filled.LocalFireDepartment, null, tint = if (selected) Color.White else Color(0xFFFF6A00), modifier = Modifier.size(16.dp))
                    }
                    Text(text = label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

private fun mapTitle(original: String): String {
    return when {
        original.contains("mới", true) -> "Mới cập nhật"
        original.contains("bộ", true) -> "Phim bộ hot"
        original.contains("lẻ", true) -> "Phim lẻ đặc sắc"
        original.contains("hoạt hình", true) -> "Hoạt hình đỉnh cao"
        else -> original
    }
}

private fun mapSubtitle(type: String): String {
    return when (type) {
        "phim-moi-cap-nhat" -> "Cập nhật liên tục mỗi giờ"
        "phim-bo" -> "Cày phim không ngừng nghỉ"
        "phim-le" -> "Điện ảnh đỉnh cao"
        "hoat-hinh" -> "Thế giới anime & cartoon"
        else -> "Tuyển chọn cho bạn"
    }
}
