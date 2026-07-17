package com.aho.yunphim.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aho.yunphim.data.remote.NguonCEndpoints
import com.aho.yunphim.ui.components.FullScreenError
import com.aho.yunphim.ui.components.FullScreenLoading
import com.aho.yunphim.ui.components.MovieCard
import com.aho.yunphim.ui.theme.YunPhimColors

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMovieClick: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo }.collect { layoutInfo ->
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = layoutInfo.totalItemsCount
            if (total > 0 && lastVisible >= total - 6) {
                viewModel.loadMore()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(YunPhimColors.Background)
            .systemBarsPadding(),
    ) {
        TopBar(onSearchClick = onSearchClick)
        CategoryTabs(selected = state.selectedPath, onSelect = viewModel::selectPath)
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> FullScreenLoading()
                state.error != null && state.items.isEmpty() -> FullScreenError(
                    message = state.error.orEmpty(),
                    isSchemaMismatch = state.isSchemaMismatch,
                    onRetry = viewModel::retry,
                )

                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    state = gridState,
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(
                        items = state.items,
                        key = { it.slug ?: it.id ?: it.hashCode().toString() },
                    ) { movie ->
                        MovieCard(movie = movie, onClick = { movie.slug?.let(onMovieClick) })
                    }
                    if (state.isLoadingMore) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    color = YunPhimColors.Accent,
                                    modifier = Modifier.size(24.dp),
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
private fun TopBar(onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "YunPhim",
            style = MaterialTheme.typography.headlineSmall,
            color = YunPhimColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onSearchClick) {
            Icon(Icons.Filled.Search, contentDescription = "Tìm kiếm", tint = YunPhimColors.TextPrimary)
        }
    }
}

private data class CategoryTab(val type: String, val label: String)

private val categoryTabs = listOf(
    CategoryTab(NguonCEndpoints.ListPath.NEW_UPDATE, "Mới cập nhật"),
    CategoryTab(NguonCEndpoints.ListPath.SINGLE, "Phim lẻ"),
    CategoryTab(NguonCEndpoints.ListPath.SERIES, "Phim bộ"),
    CategoryTab(NguonCEndpoints.ListPath.ANIME, "Hoạt hình"),
    CategoryTab(NguonCEndpoints.ListPath.ADULT_18, "18+"),
)

@Composable
private fun CategoryTabs(selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(categoryTabs) { tab ->
            val isSelected = tab.type == selected
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) YunPhimColors.Accent else YunPhimColors.Surface,
                modifier = Modifier.clickable { onSelect(tab.type) },
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Color.White else YunPhimColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}
