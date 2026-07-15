package com.nguoncflix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguoncflix.api.RetrofitClient
import com.nguoncflix.data.models.Movie
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

    fun search(query: String) {
        if (query.length < 2) {
            _uiState.value = SearchUiState()
            return
        }

        viewModelScope.launch {
            _uiState.value = SearchUiState(isLoading = true)

            try {
                val response = RetrofitClient.apiService.searchMovies(keyword = query, limit = 30)
                val items = response.items ?: response.data?.items ?: emptyList()

                // Defensive fallback + filtering
                val filtered = items
                    .filter { it.name.isNotBlank() }
                    .take(30)

                _uiState.value = SearchUiState(
                    movies = filtered,
                    isLoading = false
                )
            } catch (e: Exception) {
                // Fallback: try to use new movies as backup
                val fallback = try {
                    val newResp = RetrofitClient.apiService.getNewMovies(1)
                    (newResp.items ?: emptyList())
                        .filter { it.name.contains(query, ignoreCase = true) }
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
        _uiState.value = SearchUiState()
    }
}
