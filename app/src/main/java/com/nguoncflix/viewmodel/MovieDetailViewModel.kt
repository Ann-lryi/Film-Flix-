package com.nguoncflix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguoncflix.api.RetrofitClient
import com.nguoncflix.data.models.EpisodeData
import com.nguoncflix.data.models.MovieDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MovieDetailUiState(
    val movie: MovieDetail? = null,
    val episodes: List<EpisodeData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MovieDetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    fun fetchMovieDetail(slug: String) {
        viewModelScope.launch {
            _uiState.value = MovieDetailUiState(isLoading = true)
            try {
                val response = RetrofitClient.apiService.getMovieDetail(slug)
                val movie = response.movie
                val episodes = response.episodes?.firstOrNull()?.serverData ?: emptyList()

                _uiState.value = MovieDetailUiState(
                    movie = movie,
                    episodes = episodes,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = MovieDetailUiState(
                    isLoading = false,
                    error = e.message ?: "Failed to load movie"
                )
            }
        }
    }
}
