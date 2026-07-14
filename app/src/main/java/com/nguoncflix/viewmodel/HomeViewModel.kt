package com.nguoncflix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguoncflix.api.RetrofitClient
import com.nguoncflix.data.models.Movie
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
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchHomeData()
    }

    private fun fetchHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // New movies (featured)
                val newResponse = RetrofitClient.apiService.getNewMovies(page = 1)
                val newMovies = newResponse.items ?: emptyList()

                // Series
                val seriesResponse = RetrofitClient.apiService.getMoviesByType("phim-bo", page = 1, limit = 12)
                val series = seriesResponse.items ?: seriesResponse.data?.items ?: emptyList()

                // Single movies
                val singleResponse = RetrofitClient.apiService.getMoviesByType("phim-le", page = 1, limit = 12)
                val singles = singleResponse.items ?: singleResponse.data?.items ?: emptyList()

                // TV Shows
                val tvResponse = RetrofitClient.apiService.getMoviesByType("tv-shows", page = 1, limit = 12)
                val tvs = tvResponse.items ?: tvResponse.data?.items ?: emptyList()

                _uiState.value = HomeUiState(
                    featuredMovie = newMovies.firstOrNull(),
                    newMovies = newMovies.take(12),
                    seriesMovies = series,
                    singleMovies = singles,
                    tvShows = tvs,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load data"
                )
            }
        }
    }
}
