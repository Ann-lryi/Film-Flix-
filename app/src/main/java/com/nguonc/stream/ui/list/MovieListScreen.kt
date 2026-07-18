package com.nguonc.stream.ui.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nguonc.stream.ui.components.EmptyBox
import com.nguonc.stream.ui.components.ErrorBox
import com.nguonc.stream.ui.theme.FilmFlixIcons
import com.nguonc.stream.ui.components.LoadingBox
import com.nguonc.stream.ui.components.MoviePosterCard
import com.nguonc.stream.ui.theme.AppShapes
import com.nguonc.stream.ui.theme.Motion
import com.nguonc.stream.ui.theme.Primary
import com.nguonc.stream.ui.theme.premiumShadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    source: MovieListSource,
    key: String,
    title: String,
    onBack: () -> Unit,
    onMovieClick: (String) -> Unit,
    viewModel: MovieListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    LaunchedEffect(source, key) { viewModel.init(source, key) }

    val shouldLoadMore by remember {
        derivedStateOf {
            val info = gridState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - 6
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            title,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                        )
                        Surface(
                            shape = AppShapes.Pill,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        ) {
                            Text(
                                "${state.items.size}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    val backInteraction = remember { MutableInteractionSource() }
                    val backPressed by backInteraction.collectIsPressedAsState()
                    val backScale by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (backPressed) Motion.PressScale else 1f,
                        animationSpec = Motion.PressSpring,
                        label = "backScale"
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(42.dp)
                            .scale(backScale)
                            .premiumShadow(2.dp, CircleShape)
                    ) {
                        IconButton(onClick = onBack, interactionSource = backInteraction) {
                            Icon(
                                FilmFlixIcons.ChevronLeft,
                                contentDescription = "Quay lại"
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.padding(end = 8.dp).size(42.dp)
                    ) {
                        IconButton(onClick = {}) {
                            Icon(
                                FilmFlixIcons.FilterOutline,
                                contentDescription = "Lọc",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier.statusBarsPadding()
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            when {
                state.isInitialLoading -> LoadingBox()
                state.error != null -> ErrorBox(state.error!!, onRetry = viewModel::retryInitial)
                state.items.isEmpty() -> EmptyBox("Không có phim nào")
                else -> LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = 126.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    items(state.items, key = { it.id }) { movie ->
                        MoviePosterCard(movie = movie, onClick = { onMovieClick(movie.slug) })
                    }
                    if (state.isLoadingMore) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = Primary,
                                    strokeWidth = 2.5.dp
                                )
                            }
                        }
                    } else if (state.loadMoreError) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = viewModel::loadNextPage) {
                                    Text("Tải thêm thất bại — Thử lại", color = Primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
