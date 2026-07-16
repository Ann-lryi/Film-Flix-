package com.aho.yunphim.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aho.yunphim.data.UiState
import com.aho.yunphim.data.model.MovieDetail
import com.aho.yunphim.data.model.ServerGroup
import com.aho.yunphim.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val detail: MovieDetail? = null,
    val servers: List<ServerGroup> = emptyList(),
    val error: String? = null,
    val isSchemaMismatch: Boolean = false,
)

class DetailViewModel(
    private val repository: MovieRepository,
    private val slug: String,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.fetchDetail(slug)) {
                is UiState.Success -> _state.update {
                    it.copy(isLoading = false, detail = result.data.detail, servers = result.data.servers)
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
}
