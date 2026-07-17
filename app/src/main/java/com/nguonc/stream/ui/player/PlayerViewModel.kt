package com.nguonc.stream.ui.player

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.nguonc.stream.data.remote.dto.EpisodeDto
import com.nguonc.stream.data.repository.MovieRepository
import com.nguonc.stream.ui.home.readableMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val movieName: String = "",
    val posterUrl: String = "",
    val episodes: List<EpisodeDto> = emptyList(),
    val currentEpisodeSlug: String = "",
) {
    val currentEpisodeName: String
        get() = episodes.firstOrNull { it.slug == currentEpisodeSlug }?.name.orEmpty()
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val slug: String = checkNotNull(savedStateHandle["slug"])
    private val requestedEpisode: String? = savedStateHandle["ep"]

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    /** Player sống trong ViewModel để không mất tiến trình khi xoay màn hình. */
    val player: ExoPlayer by lazy {
        ExoPlayer.Builder(appContext).build().apply {
            playWhenReady = true
        }
    }

    private var progressJob: Job? = null
    private var resumePositionMs: Long = 0L

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) startProgressSaver() else stopProgressSaver()
        }
    }

    init {
        player.addListener(playerListener)
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val detail = repository.getMovieDetail(slug)
                val history = repository.getHistory(slug)
                detail to history
            }.onSuccess { (detail, history) ->
                val episodes = detail.episodes
                    .firstOrNull()?.serverData
                    .orEmpty()
                    .filter { it.linkM3u8.isNotBlank() }
                if (episodes.isEmpty()) {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Phim chưa có nguồn phát")
                    }
                    return@onSuccess
                }
                val targetEpisode = episodes.firstOrNull { it.slug == requestedEpisode }
                    ?: episodes.firstOrNull { it.slug == history?.episodeSlug }
                    ?: episodes.first()
                resumePositionMs =
                    if (history?.episodeSlug == targetEpisode.slug) history.positionMs else 0L

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        movieName = detail.movie.name,
                        posterUrl = detail.movie.posterUrl,
                        episodes = episodes,
                        currentEpisodeSlug = targetEpisode.slug,
                    )
                }
                play(targetEpisode, resumePositionMs)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.readableMessage()) }
            }
        }
    }

    fun switchEpisode(episode: EpisodeDto) {
        if (episode.slug == _uiState.value.currentEpisodeSlug) return
        saveCurrentProgress()
        _uiState.update { it.copy(currentEpisodeSlug = episode.slug) }
        play(episode, startPositionMs = 0L)
    }

    private fun play(episode: EpisodeDto, startPositionMs: Long) {
        player.setMediaItem(MediaItem.fromUri(episode.linkM3u8), startPositionMs)
        player.prepare()
        player.playWhenReady = true
    }

    fun pause() = player.pause()

    /** Lưu tiến độ xem định kỳ 5s một lần trong khi đang phát. */
    private fun startProgressSaver() {
        if (progressJob?.isActive == true) return
        progressJob = viewModelScope.launch {
            while (isActive) {
                delay(5_000)
                saveCurrentProgress()
            }
        }
    }

    private fun stopProgressSaver() {
        progressJob?.cancel()
        progressJob = null
        saveCurrentProgress()
    }

    fun saveCurrentProgress() {
        val state = _uiState.value
        if (state.movieName.isBlank() || state.currentEpisodeSlug.isBlank()) return
        val position = runCatching { player.currentPosition }.getOrDefault(0L)
        if (position <= 0L) return
        viewModelScope.launch {
            repository.saveProgress(
                slug = slug,
                name = state.movieName,
                posterUrl = state.posterUrl,
                episodeSlug = state.currentEpisodeSlug,
                episodeName = state.currentEpisodeName,
                positionMs = position,
            )
        }
    }

    override fun onCleared() {
        stopProgressSaver()
        player.removeListener(playerListener)
        player.release()
        super.onCleared()
    }
}
