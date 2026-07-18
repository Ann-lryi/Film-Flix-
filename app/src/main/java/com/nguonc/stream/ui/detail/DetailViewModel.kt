package com.nguonc.stream.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguonc.stream.data.repository.MovieDetailBundle
import com.nguonc.stream.data.repository.MovieRepository
import com.nguonc.stream.ui.home.readableMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val bundle: MovieDetailBundle? = null,
    val isFavorite: Boolean = false,
    val selectedServer: Int = 0,
    val lastWatchedEpisode: String? = null,
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val slug: String = checkNotNull(savedStateHandle["slug"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            repository.observeIsFavorite(slug).collect { isFav ->
                _uiState.update { it.copy(isFavorite = isFav) }
            }
        }
        viewModelScope.launch {
            repository.getHistory(slug)?.let { history ->
                _uiState.update { it.copy(lastWatchedEpisode = history.episodeSlug) }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.getMovieDetail(slug) }
                .onSuccess { bundle ->
                    _uiState.update { it.copy(isLoading = false, bundle = bundle) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.readableMessage()) }
                }
        }
    }

    fun toggleFavorite() {
        val movie = _uiState.value.bundle?.movie ?: return
        viewModelScope.launch { repository.toggleFavorite(movie) }
    }

    fun selectServer(index: Int) {
        _uiState.update { it.copy(selectedServer = index) }
    }
}
