package com.nguonc.stream.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nguonc.stream.ui.components.ErrorBox
import com.nguonc.stream.ui.theme.FilmFlixIcons
import com.nguonc.stream.ui.components.HeroBannerCarousel
import com.nguonc.stream.ui.components.MoviePosterCard
import com.nguonc.stream.ui.components.SectionHeader
import com.nguonc.stream.ui.components.ShimmerHero
import com.nguonc.stream.ui.components.ShimmerPosterRow
import com.nguonc.stream.ui.theme.AppGradients
import com.nguonc.stream.ui.theme.AppShapes
import com.nguonc.stream.ui.theme.Elevation
import com.nguonc.stream.ui.theme.Motion
import com.nguonc.stream.ui.theme.PremiumTextStyles
import com.nguonc.stream.ui.theme.Primary
import com.nguonc.stream.ui.theme.glowShadow
import com.nguonc.stream.ui.theme.topHighlight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (String) -> Unit,
    onSeeMore: (listType: String, title: String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient decorative glow — top-right corner
        Box(
            modifier = Modifier
                .size(440.dp)
                .align(Alignment.TopEnd)
                .drawBehind {
                    drawCircle(
                        brush = AppGradients.AmbientRedGlow,
                        radius = 440.dp.toPx()
                    )
                }
        )
        // Subtle violet glow — bottom-left
        Box(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.BottomStart)
                .drawBehind {
                    drawCircle(
                        brush = AppGradients.AmbientVioletGlow,
                        radius = 380.dp.toPx()
                    )
                }
        )

        Column(modifier = Modifier.fillMaxSize()) {
            PremiumHomeTopBar(onSearchClick = onSearchClick)

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 130.dp),
                    verticalArrangement = Arrangement.spacedBy(30.dp),
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
                    } else {
                        // Hero skeleton
                        item { ShimmerHero() }
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
                                            modifier = Modifier.width(152.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(AppShapes.Medium)
                                        .background(AppGradients.PrimaryGradient)
                                        .glowShadow(
                                            color = Primary,
                                            shape = AppShapes.Medium,
                                            glowRadius = 14.dp,
                                            elevation = Elevation.S
                                        )
                                        .topHighlight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        FilmFlixIcons.BoltFilled,
                                        "FilmFlix",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "FILM FLIX • CINEMATIC UNIVERSE",
                                    style = PremiumTextStyles.Eyebrow,
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
            // Logo — squircle-ish box with gradient + glow
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(AppShapes.Medium)
                    .background(AppGradients.PrimaryGradient)
                    .glowShadow(
                        color = Primary,
                        shape = AppShapes.Medium,
                        glowRadius = 14.dp,
                        elevation = Elevation.S
                    )
                    .topHighlight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "F",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "FILM FLIX",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(color = Primary, shape = CircleShape, modifier = Modifier.size(6.dp)) {}
                }
                Text(
                    text = "Khám phá vũ trụ điện ảnh",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Search pill — premium pressable, full-width version on the right
        val searchInteraction = remember { MutableInteractionSource() }
        val isPressed by searchInteraction.collectIsPressedAsState()
        val searchScale by androidx.compose.animation.core.animateFloatAsState(
            targetValue = if (isPressed) Motion.PressScale else 1f,
            animationSpec = Motion.PressSpring,
            label = "searchPillScale"
        )
        Surface(
            shape = AppShapes.Pill,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            modifier = Modifier
                .height(44.dp)
                .scale(searchScale)
                .clip(AppShapes.Pill)
                .clickable(interactionSource = searchInteraction, indication = null, onClick = onSearchClick)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    FilmFlixIcons.SearchOutline, null,
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
    }
}

// Avoids extra import in the function above
private typealias ImageVector = androidx.compose.ui.graphics.vector.ImageVector

private fun mapTitle(original: String): String = original

private fun mapSubtitle(type: String): String {
    return when (type) {
        "phim-moi-cap-nhat" -> "Cập nhật liên tục mỗi giờ"
        "phim-bo" -> "Cày phim không ngừng nghỉ"
        "phim-le" -> "Điện ảnh đỉnh cao"
        "hoat-hinh" -> "Thế giới anime & cartoon"
        else -> "Tuyển chọn cho bạn"
    }
}
