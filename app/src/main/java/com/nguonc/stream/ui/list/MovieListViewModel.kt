package com.nguonc.stream.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguonc.stream.data.remote.dto.MovieItemDto
import com.nguonc.stream.data.repository.MovieRepository
import com.nguonc.stream.debug.AppLogger
import com.nguonc.stream.ui.home.readableMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Nguồn dữ liệu cho màn lưới phim */
enum class MovieListSource { LIST, CATEGORY, COUNTRY }

data class MovieListUiState(
    val items: List<MovieItemDto> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val isInitialLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val loadMoreError: Boolean = false,
) {
    val canLoadMore: Boolean get() = currentPage < totalPages && !isLoadingMore
}

@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieListUiState())
    val uiState: StateFlow<MovieListUiState> = _uiState.asStateFlow()

    private var source: MovieListSource = MovieListSource.LIST
    private var key: String = ""
    private var loadJob: Job? = null
    private var initialized = false

    fun init(source: MovieListSource, key: String) {
        if (initialized) return
        initialized = true
        this.source = source
        this.key = key
        loadPage(1)
    }

    fun refresh() {
        _uiState.value = MovieListUiState()
        loadPage(1)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (!state.canLoadMore || loadJob?.isActive == true) return
        loadPage(state.currentPage + 1)
    }

    fun retryInitial() = refresh()

    private fun loadPage(page: Int) {
        // Chỉ cancel job cũ khi load page 1 (refresh/initial).
        // Khi load more (page > 1), KHÔNG cancel — nếu không sẽ cancel chính nó.
        if (page == 1) loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                if (page == 1) it.copy(isInitialLoading = true, error = null)
                else it.copy(isLoadingMore = true, loadMoreError = false)
            }
            runCatching { fetch(page) }
                .onSuccess { result ->
                    _uiState.update { state ->
                        // ⚡ Deduplicate items by slug to prevent crash
                        val newItems = if (page == 1) {
                            result.items
                        } else {
                            val existingSlugs = state.items.map { it.slug }.toSet()
                            result.items.filter { it.slug !in existingSlugs }
                        }
                        state.copy(
                            items = if (page == 1) newItems else state.items + newItems,
                            currentPage = result.pagination.currentPage,
                            totalPages = result.pagination.totalPages,
                            isInitialLoading = false,
                            isLoadingMore = false,
                            error = null,
                        )
                    }
                }
                .onFailure { e ->
                    AppLogger.e("MOVIE_LIST_VM", "loadPage($page) FAILED: ${e.message}", e)
                    _uiState.update {
                        if (page == 1) it.copy(isInitialLoading = false, error = e.readableMessage())
                        else it.copy(isLoadingMore = false, loadMoreError = true)
                    }
                }
        }
    }

    private suspend fun fetch(page: Int) = when (source) {
        MovieListSource.LIST -> repository.getMovieList(key, page)
        MovieListSource.CATEGORY -> repository.getByCategory(key, page)
        MovieListSource.COUNTRY -> repository.getByCountry(key, page)
    }
}
