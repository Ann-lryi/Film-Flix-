package com.aho.yunphim.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aho.yunphim.data.UiState
import com.aho.yunphim.data.model.MovieSummary
import com.aho.yunphim.data.remote.NguonCEndpoints
import com.aho.yunphim.data.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val selectedType: String = NguonCEndpoints.ListType.NEW_UPDATE,
    val items: List<MovieSummary> = emptyList(),
    val page: Int = 1,
    val lastPage: Int = 1,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val isSchemaMismatch: Boolean = false,
)

class HomeViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadFirstPage()
    }

    fun selectType(type: String) {
        if (_state.value.selectedType == type) return
        loadJob?.cancel()
        _state.value = HomeUiState(selectedType = type)
        loadFirstPage()
    }

    fun retry() = loadFirstPage()

    private fun loadFirstPage() {
        loadJob?.cancel()
        val type = _state.value.selectedType
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.fetchList(type, 1)) {
                is UiState.Success -> _state.update {
                    it.copy(
                        items = result.data.items,
                        page = result.data.currentPage,
                        lastPage = result.data.lastPage,
                        isLoading = false,
                    )
                }

                is UiState.Error -> _state.update {
                    it.copy(
                        isLoading = false,
                        error = result.message,
                        isSchemaMismatch = result.isSchemaMismatch,
                    )
                }

                UiState.Loading -> Unit
            }
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current.isLoadingMore || current.isLoading || current.page >= current.lastPage) return
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            when (val result = repository.fetchList(current.selectedType, current.page + 1)) {
                is UiState.Success -> _state.update {
                    it.copy(
                        items = it.items + result.data.items,
                        page = result.data.currentPage,
                        lastPage = result.data.lastPage,
                        isLoadingMore = false,
                    )
                }

                is UiState.Error -> _state.update { it.copy(isLoadingMore = false) }
                UiState.Loading -> Unit
            }
        }
    }
}
