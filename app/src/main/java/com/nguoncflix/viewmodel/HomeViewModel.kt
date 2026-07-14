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
                // Parallel fetch with robust handling
                val newDef = async {
                    try {
                        val response = RetrofitClient.apiService.getNewMovies(1)
                        // /danh-sach/phim-moi-cap-nhat uses direct items
                        response.items ?: emptyList()
                    } catch (e: Exception) { emptyList() }
                }

                val seriesDef = async {
                    try {
                        val response = RetrofitClient.apiService.getMoviesByType("phim-bo", 1, 16)
                        // /v1/api/... uses data.items
                        response.data?.items ?: response.items ?: emptyList()
                    } catch (e: Exception) { emptyList() }
                }

                val singleDef = async {
                    try {
                        val response = RetrofitClient.apiService.getMoviesByType("phim-le", 1, 16)
                        response.data?.items ?: response.items ?: emptyList()
                    } catch (e: Exception) { emptyList() }
                }

                val tvDef = async {
                    try {
                        val response = RetrofitClient.apiService.getMoviesByType("tv-shows", 1, 14)
                        response.data?.items ?: response.items ?: emptyList()
                    } catch (e: Exception) { emptyList() }
                }

                val (newsRaw, seriesRaw, singlesRaw, tvsRaw) = awaitAll(newDef, seriesDef, singleDef, tvDef)

                // === INDUSTRIAL-GRADE DEFENSIVE FALLBACKS ===
                val finalNew = newsRaw.take(14).filter { it.name.isNotBlank() }

                // Series (Phim Bộ)
                val finalSeries = when {
                    seriesRaw.isNotEmpty() -> seriesRaw.take(14).filter { it.name.isNotBlank() }
                    else -> finalNew.filter { 
                        it.episodeCurrent?.contains("Tập", ignoreCase = true) == true || 
                        it.type == "series" || 
                        (it.episodeCurrent?.contains("/") == true && !it.episodeCurrent.contains("Full"))
                    }.take(14).ifEmpty { finalNew.take(8) }
                }

                // Singles (Phim Lẻ)
                val finalSingles = when {
                    singlesRaw.isNotEmpty() -> singlesRaw.take(14).filter { it.name.isNotBlank() }
                    else -> finalNew.filter { 
                        it.episodeCurrent?.contains("Full", ignoreCase = true) == true || 
                        it.type == "single" || 
                        it.episodeCurrent?.contains("phút") == true
                    }.take(14).ifEmpty { finalNew.take(8) }
                }

                // TV Shows / Anime (more aggressive fallback)
                val finalTv = when {
                    tvsRaw.isNotEmpty() -> tvsRaw.take(14).filter { it.name.isNotBlank() }
                    else -> finalNew.filter { 
                        it.episodeCurrent?.contains("Tập", ignoreCase = true) == true ||
                        it.type?.contains("tv", ignoreCase = true) == true ||
                        it.episodeCurrent?.contains("phút") == false
                    }.take(14).ifEmpty { finalNew.take(10) }
                }

                // Always guarantee at least some content
                val guaranteedSeries = if (finalSeries.isEmpty()) finalNew.take(8) else finalSeries
                val guaranteedSingles = if (finalSingles.isEmpty()) finalNew.take(8) else finalSingles
                val guaranteedTv = if (finalTv.isEmpty()) finalNew.take(8) else finalTv

                val featured = finalNew.firstOrNull()
                    ?: guaranteedSeries.firstOrNull()
                    ?: guaranteedSingles.firstOrNull()
                    ?: guaranteedTv.firstOrNull()

                _uiState.value = HomeUiState(
                    featuredMovie = featured,
                    newMovies = finalNew,
                    seriesMovies = guaranteedSeries,
                    singleMovies = guaranteedSingles,
                    tvShows = guaranteedTv,
                    isLoading = false,
                    isRefreshing = false
                )
            } catch (e: Exception) {
                // Ultimate fallback: use newMovies for everything if all else fails
                val currentNew = _uiState.value.newMovies.ifEmpty { 
                    try { 
                        RetrofitClient.apiService.getNewMovies(1).items?.take(12) ?: emptyList() 
                    } catch (_: Exception) { emptyList() } 
                }

                _uiState.value = HomeUiState(
                    featuredMovie = currentNew.firstOrNull(),
                    newMovies = currentNew,
                    seriesMovies = currentNew.take(8),
                    singleMovies = currentNew.take(8),
                    tvShows = currentNew.take(8),
                    isLoading = false,
                    isRefreshing = false,
                    error = if (currentNew.isEmpty()) "Không thể tải dữ liệu. Kéo xuống để thử lại." else null
                )
            }
        }
    }
}
