package com.nguonc.stream.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguonc.stream.data.remote.dto.MovieItemDto
import com.nguonc.stream.data.repository.MovieRepository
import com.nguonc.stream.ui.home.readableMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    val items: List<MovieItemDto> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val searched: Boolean = false,
) {
    val canLoadMore: Boolean get() = currentPage < totalPages && !isLoadingMore
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(400)
                .distinctUntilChanged()
                .collectLatest { query -> performSearch(query, page = 1) }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        queryFlow.value = query.trim()
    }

    /** Thử lại trang 1 với từ khoá hiện tại (distinctUntilChanged chặn query trùng). */
    fun retry() {
        val query = _uiState.value.query
        if (query.isBlank()) return
        viewModelScope.launch { performSearch(query, page = 1) }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (!state.canLoadMore || state.query.isBlank()) return
        viewModelScope.launch { performSearch(state.query, state.currentPage + 1) }
    }

    private suspend fun performSearch(query: String, page: Int) {
        if (query.isBlank()) {
            _uiState.update { SearchUiState(query = it.query) }
            return
        }
        _uiState.update {
            if (page == 1) it.copy(isLoading = true, error = null)
            else it.copy(isLoadingMore = true)
        }
        runCatching { repository.search(query, page) }
            .onSuccess { result ->
                _uiState.update { state ->
                    state.copy(
                        items = if (page == 1) result.items else state.items + result.items,
                        currentPage = result.pagination.currentPage,
                        totalPages = result.pagination.totalPages,
                        isLoading = false,
                        isLoadingMore = false,
                        searched = true,
                    )
                }
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = if (page == 1) e.readableMessage() else it.error,
                        searched = true,
                    )
                }
            }
    }
}
