package com.nguoncflix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguoncflix.api.RetrofitClient
import com.nguoncflix.data.models.Movie
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SearchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Latest query — used to discard stale responses
    private var currentQuery: String = ""
    private var searchJob: Job? = null

    /**
     * Run a debounced search. Cancels any in-flight request to avoid race conditions
     * where an older response overwrites a newer one.
     */
    fun search(query: String) {
        val trimmed = query.trim()
        currentQuery = trimmed
        searchJob?.cancel()

        if (trimmed.length < 2) {
            _uiState.value = SearchUiState()
            return
        }

        searchJob = viewModelScope.launch {
            // Debounce: 300ms in addition to whatever the UI does
            delay(300)
            // Re-check: query might have changed while we were waiting
            if (currentQuery != trimmed) return@launch

            _uiState.value = SearchUiState(isLoading = true)

            try {
                val response = RetrofitClient.apiService.searchMovies(
                    keyword = trimmed, limit = 30
                )
                val items = response.items ?: response.data?.items ?: emptyList()
                if (currentQuery != trimmed) return@launch  // discard stale response

                val filtered = items
                    .filter { it.name.isNotBlank() }
                    .take(30)

                _uiState.value = SearchUiState(
                    movies = filtered,
                    isLoading = false
                )
            } catch (e: Exception) {
                if (currentQuery != trimmed) return@launch

                // Fallback: filter new-movies list locally
                val fallback = try {
                    val newResp = RetrofitClient.apiService.getNewMovies(1)
                    (newResp.items ?: emptyList())
                        .filter { it.name.contains(trimmed, ignoreCase = true) }
                        .take(12)
                } catch (_: Exception) { emptyList() }

                _uiState.value = SearchUiState(
                    movies = fallback,
                    isLoading = false,
                    error = if (fallback.isEmpty()) "Không thể tìm kiếm lúc này" else null
                )
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        currentQuery = ""
        _uiState.value = SearchUiState()
    }
}
