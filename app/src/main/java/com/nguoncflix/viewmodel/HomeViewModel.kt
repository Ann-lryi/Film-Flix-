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
            if (!forceRefresh && _uiState.value.newMovies.isNotEmpty() && !_uiState.value.isLoading) return@launch

            val isRefresh = _uiState.value.newMovies.isNotEmpty()
            _uiState.value = _uiState.value.copy(
                isLoading = !isRefresh,
                isRefreshing = isRefresh,
                error = null
            )

            try {
                val newDef = async {
                    val r = RetrofitClient.apiService.getNewMovies(1)
                    r.items ?: r.data?.items ?: emptyList()
                }
                val seriesDef = async {
                    val r = RetrofitClient.apiService.getMoviesByType("phim-bo", 1, 12)
                    r.items ?: r.data?.items ?: emptyList()
                }
                val singleDef = async {
                    val r = RetrofitClient.apiService.getMoviesByType("phim-le", 1, 12)
                    r.items ?: r.data?.items ?: emptyList()
                }
                val tvDef = async {
                    val r = RetrofitClient.apiService.getMoviesByType("tv-shows", 1, 12)
                    r.items ?: r.data?.items ?: emptyList()
                }

                val (news, series, singles, tvs) = awaitAll(newDef, seriesDef, singleDef, tvDef)

                // Industrial-grade data guarantee
                val finalNew = news.take(12)
                val finalSeries = if (series.isNotEmpty()) series.take(12) else finalNew.filter { it.episodeCurrent?.contains("Tập") == true }.take(12)
                val finalSingles = if (singles.isNotEmpty()) singles.take(12) else finalNew.filter { it.episodeCurrent?.contains("Full") == true || it.type == "single" }.take(12)
                val finalTv = if (tvs.isNotEmpty()) tvs.take(12) else finalNew.take(12)

                val featured = finalNew.firstOrNull() 
                    ?: finalSeries.firstOrNull() 
                    ?: finalSingles.firstOrNull() 
                    ?: finalTv.firstOrNull()

                _uiState.value = HomeUiState(
                    featuredMovie = featured,
                    newMovies = finalNew,
                    seriesMovies = finalSeries,
                    singleMovies = finalSingles,
                    tvShows = finalTv,
                    isLoading = false,
                    isRefreshing = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "Không thể tải dữ liệu. Kéo xuống để thử lại."
                )
            }
        }
    }
}
