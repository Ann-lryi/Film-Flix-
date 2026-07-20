package com.nguonc.stream.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguonc.stream.data.repository.MovieDetailBundle
import com.nguonc.stream.data.repository.MovieRepository
import com.nguonc.stream.debug.AppLogger
import com.nguonc.stream.debug.LogTags
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
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val slug: String = savedStateHandle.get<String>("slug").orEmpty()

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        // Restore selected server across configuration changes / process death
        val savedServer = savedStateHandle.get<Int>("selectedServer") ?: 0
        AppLogger.i(LogTags.DETAIL_VM, "=== DetailViewModel init === slug=\"$slug\", savedServer=$savedServer")
        _uiState.update { it.copy(selectedServer = savedServer) }
        load()
        viewModelScope.launch {
            repository.observeIsFavorite(slug).collect { isFav ->
                AppLogger.d(LogTags.DETAIL_VM, "isFavorite changed: $isFav")
                _uiState.update { it.copy(isFavorite = isFav) }
            }
        }
        viewModelScope.launch {
            repository.getHistory(slug)?.let { history ->
                AppLogger.i(
                    LogTags.DETAIL_VM,
                    "History found: epSlug=${history.episodeSlug}, positionMs=${history.positionMs}"
                )
                _uiState.update { it.copy(lastWatchedEpisode = history.episodeSlug) }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            AppLogger.i(LogTags.DETAIL_VM, "load() — fetching detail for slug=\"$slug\"")
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { repository.getMovieDetail(slug) }
                .onSuccess { bundle ->
                    // Clamp selectedServer to valid range
                    val clamped = _uiState.value.selectedServer
                        .coerceIn(0, (bundle.episodes.size - 1).coerceAtLeast(0))
                    AppLogger.success(
                        LogTags.DETAIL_VM,
                        "load() ✓ — \"${bundle.movie.name}\", ${bundle.episodes.size} servers, selectedServer=$clamped"
                    )
                    bundle.episodes.forEachIndexed { idx, srv ->
                        AppLogger.d(
                            LogTags.DETAIL_VM,
                            "  server[${idx}] = \"${srv.serverName}\" (${srv.serverData.size} eps)"
                        )
                    }
                    _uiState.update {
                        it.copy(isLoading = false, bundle = bundle, selectedServer = clamped)
                    }
                }
                .onFailure { e ->
                    AppLogger.e(LogTags.DETAIL_VM, "load() FAILED: ${e.message}", e)
                    _uiState.update { it.copy(isLoading = false, error = e.readableMessage()) }
                }
        }
    }

    fun toggleFavorite() {
        val movie = _uiState.value.bundle?.movie ?: return
        AppLogger.d(LogTags.DETAIL_VM, "toggleFavorite() — slug=\"${movie.slug}\"")
        viewModelScope.launch { repository.toggleFavorite(movie) }
    }

    fun selectServer(index: Int) {
        val max = (_uiState.value.bundle?.episodes?.size ?: 1) - 1
        val safe = index.coerceIn(0, max.coerceAtLeast(0))
        AppLogger.i(LogTags.DETAIL_VM, "selectServer($index) → clamped=$safe")
        savedStateHandle["selectedServer"] = safe
        _uiState.update { it.copy(selectedServer = safe) }
    }
}
