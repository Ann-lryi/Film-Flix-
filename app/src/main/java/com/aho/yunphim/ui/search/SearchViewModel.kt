package com.aho.yunphim.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aho.yunphim.data.UiState
import com.aho.yunphim.data.model.MovieSummary
import com.aho.yunphim.data.repository.MovieRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val items: List<MovieSummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSchemaMismatch: Boolean = false,
    val hasSearched: Boolean = false,
)

@OptIn(FlowPreview::class)
class SearchViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { query -> performSearch(query) }
        }
    }

    fun onQueryChange(newQuery: String) {
        _state.update { it.copy(query = newQuery) }
        queryFlow.value = newQuery
    }

    /** Bỏ qua debounce, tìm lại ngay với query hiện tại - dùng cho nút "Thử lại". */
    fun retry() {
        viewModelScope.launch { performSearch(_state.value.query) }
    }

    private suspend fun performSearch(query: String) {
        if (query.isBlank()) {
            _state.update {
                it.copy(items = emptyList(), isLoading = false, hasSearched = false, error = null)
            }
            return
        }
        _state.update { it.copy(isLoading = true, error = null, hasSearched = true) }
        when (val result = repository.search(query)) {
            is UiState.Success -> _state.update { it.copy(items = result.data, isLoading = false) }
            is UiState.Error -> _state.update {
                it.copy(isLoading = false, error = result.message, isSchemaMismatch = result.isSchemaMismatch)
            }

            UiState.Loading -> Unit
        }
    }
}
