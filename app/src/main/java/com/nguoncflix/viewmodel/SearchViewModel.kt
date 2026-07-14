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
                _uiState.value = SearchUiState(movies = items, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = SearchUiState(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
