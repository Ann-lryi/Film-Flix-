package com.nguoncflix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguoncflix.api.RetrofitClient
import com.nguoncflix.data.models.Movie
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val featuredMovie: Movie? = null,
    val newMovies: List<Movie> = emptyList(),
    val seriesMovies: List<Movie> = emptyList(),
    val singleMovies: List<Movie> = emptyList(),
    val tvShows: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchHomeData()
    }

    fun refresh() {
        fetchHomeData(forceRefresh = true)
    }

    private fun fetchHomeData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh && _uiState.value.newMovies.isNotEmpty() && !_uiState.value.isLoading) {
                // Skip if already loaded
                return@launch
            }

            val isRefreshing = _uiState.value.newMovies.isNotEmpty()
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefreshing,
                isRefreshing = isRefreshing,
                error = null
            )

            try {
                // Parallel API calls - this is the BIG performance win
                val newMoviesDeferred = async { 
                    RetrofitClient.apiService.getNewMovies(page = 1).items ?: emptyList() 
                }
                val seriesDeferred = async { 
                    RetrofitClient.apiService.getMoviesByType("phim-bo", page = 1, limit = 12)
                        .items ?: emptyList() 
                }
                val singlesDeferred = async { 
                    RetrofitClient.apiService.getMoviesByType("phim-le", page = 1, limit = 12)
                        .items ?: emptyList() 
                }
                val tvDeferred = async { 
                    RetrofitClient.apiService.getMoviesByType("tv-shows", page = 1, limit = 12)
                        .items ?: emptyList() 
                }

                // Await all in parallel
                val (newMovies, series, singles, tvs) = awaitAll(
                    newMoviesDeferred,
                    seriesDeferred,
                    singlesDeferred,
                    tvDeferred
                )

                val featured = newMovies.firstOrNull()

                _uiState.value = HomeUiState(
                    featuredMovie = featured,
                    newMovies = newMovies.take(12),
                    seriesMovies = series,
                    singleMovies = singles,
                    tvShows = tvs,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Không thể tải dữ liệu. Vui lòng thử lại."
                )
            }
        }
    }
}
