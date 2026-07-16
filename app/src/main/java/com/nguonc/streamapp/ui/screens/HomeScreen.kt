package com.nguonc.streamapp.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nguonc.streamapp.data.model.NetworkResult
import com.nguonc.streamapp.ui.components.CategoryChips
import com.nguonc.streamapp.ui.components.ErrorRetryScreen
import com.nguonc.streamapp.ui.components.HeroBanner
import com.nguonc.streamapp.ui.components.MovieCard
import com.nguonc.streamapp.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMovieClick: (String) -> Unit,
    onPlayMovieClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filterMoviesState by viewModel.filterMoviesState.collectAsState()
    val selectedFilterSlug by viewModel.selectedFilterSlug.collectAsState()
    val selectedFilterName by viewModel.selectedFilterName.collectAsState()
    val heroMovie by viewModel.heroMovie.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = filterMoviesState) {
            is NetworkResult.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Đang kết nối đến máy chủ phim.nguonc.com...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is NetworkResult.Error -> {
                ErrorRetryScreen(
                    message = state.message ?: "Lỗi kết nối máy chủ",
                    onRetry = { viewModel.refresh() }
                )
            }

            is NetworkResult.Success -> {
                val movies = state.data?.items ?: emptyList()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Hero Banner
                    if (heroMovie != null && selectedFilterSlug == "phim-moi-cap-nhat" && currentPage == 1) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HeroBanner(
                                movie = heroMovie!!,
                                onPlayClick = {
                                    heroMovie?.slug?.let { onPlayMovieClick(it) }
                                },
                                onDetailClick = {
                                    heroMovie?.slug?.let { onMovieClick(it) }
                                }
                            )
                        }
                    }

                    // Category Filter Bar
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            if (heroMovie == null || selectedFilterSlug != "phim-moi-cap-nhat" || currentPage > 1) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            CategoryChips(
                                selectedSlug = selectedFilterSlug,
                                onSelectFilter = { slug, name, isCountry ->
                                    viewModel.selectFilter(slug, name, isCountry)
                                }
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Danh sách: $selectedFilterName",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Trang $currentPage",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Grid Items
                    items(movies) { item ->
                        MovieCard(
                            movie = item,
                            onClick = {
                                item.slug?.let { onMovieClick(it) }
                            },
                            modifier = Modifier
                                .height(210.dp)
                                .padding(horizontal = if (movies.indexOf(item) % 3 == 0) 4.dp else 0.dp)
                        )
                    }

                    // Pagination Controls
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentPage > 1) {
                                OutlinedButton(
                                    onClick = {
                                        if (selectedFilterSlug == "phim-moi-cap-nhat") {
                                            viewModel.loadRecentMovies(currentPage - 1)
                                        } else {
                                            // Category pagination can be added similarly
                                            viewModel.loadRecentMovies(currentPage - 1)
                                        }
                                    }
                                ) {
                                    Text(text = "Trang Trước")
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                            }

                            Button(
                                onClick = {
                                    if (selectedFilterSlug == "phim-moi-cap-nhat") {
                                        viewModel.loadRecentMovies(currentPage + 1)
                                    } else {
                                        viewModel.loadRecentMovies(currentPage + 1)
                                    }
                                }
                            ) {
                                Text(text = "Trang Tiếp ($currentPage -> ${currentPage + 1})")
                            }
                        }
                    }
                }
            }
        }
    }
}
