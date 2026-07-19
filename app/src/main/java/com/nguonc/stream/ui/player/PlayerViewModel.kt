package com.nguonc.stream.ui.player

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import com.nguonc.stream.data.remote.dto.EpisodeDto
import com.nguonc.stream.data.remote.dto.EpisodeServerDto
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

/**
 * Một server (nguồn âm thanh) cùng danh sách tập của nó.
 * Ví dụ: server "Vietsub", "Thuyết Minh", "Lồng tiếng".
 */
data class ServerGroup(
    val index: Int,
    val name: String,
    val episodes: List<EpisodeDto>,
)

data class PlayerUiState(
    val isLoading: Boolean = true,
    val isBuffering: Boolean = false,
    val error: String? = null,
    val movieName: String = "",
    val movieOriginName: String = "",
    val posterUrl: String = "",
    val trailerUrl: String = "",
    val movieYear: Int = 0,
    val movieLang: String = "",
    val servers: List<ServerGroup> = emptyList(),
    val currentServerIndex: Int = 0,
    val currentEpisodeSlug: String = "",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val controlsVisible: Boolean = true,
    val isLocked: Boolean = false,
) {
    val currentEpisodeName: String
        get() = currentEpisode?.name.orEmpty()

    val currentEpisode: EpisodeDto?
        get() = currentServer?.episodes?.firstOrNull { it.slug == currentEpisodeSlug }

    val currentServer: ServerGroup?
        get() = servers.getOrNull(currentServerIndex)

    val hasMultipleServers: Boolean get() = servers.size > 1
    val hasMultipleEpisodes: Boolean get() = (currentServer?.episodes?.size ?: 0) > 1

    /** Tập kế tiếp, null nếu đang ở tập cuối. */
    val nextEpisode: EpisodeDto?
        get() {
            val srv = currentServer ?: return null
            val idx = srv.episodes.indexOfFirst { it.slug == currentEpisodeSlug }
            if (idx < 0 || idx >= srv.episodes.size - 1) return null
            return srv.episodes[idx + 1]
        }

    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    val bufferedProgress: Float
        get() = if (durationMs > 0) (bufferedMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val slug: String = checkNotNull(savedStateHandle["slug"])
    private val requestedEpisode: String? = savedStateHandle["ep"]
    private val requestedServer: Int = savedStateHandle.get<Int>("server") ?: 0

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
            _uiState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) startProgressSaver() else stopProgressSaver()
        }

        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_BUFFERING -> _uiState.update { it.copy(isBuffering = true) }
                Player.STATE_READY -> _uiState.update { it.copy(isBuffering = false, error = null) }
                Player.STATE_ENDED -> {
                    // Tự chuyển tập kế tiếp
                    _uiState.value.nextEpisode?.let { nextEp ->
                        switchEpisode(nextEp, autoPlay = true)
                    }
                }
                Player.STATE_IDLE -> Unit
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _uiState.update {
                it.copy(
                    isBuffering = false,
                    error = error.readablePlayerMessage(),
                )
            }
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
                Triple(detail, history, detail.episodes)
            }.onSuccess { (detail, history, rawServers) ->
                // Lọc các server có dữ liệu + có link m3u8 hợp lệ
                val servers: List<ServerGroup> = rawServers
                    .filter { it.serverData.any { ep -> ep.linkM3u8.isNotBlank() } }
                    .mapIndexed { idx, srv ->
                        ServerGroup(
                            index = idx,
                            name = srv.serverName.ifBlank { "Server ${idx + 1}" },
                            episodes = srv.serverData.filter { it.linkM3u8.isNotBlank() }
                        )
                    }

                if (servers.isEmpty()) {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Phim chưa có nguồn phát")
                    }
                    return@onSuccess
                }

                // Chọn server theo requestedServer hoặc theo history hoặc mặc định server đầu
                val historyServerName: String? = null // API không lưu server trong history
                val targetServerIdx = servers.indexOfFirst { it.index == requestedServer }
                    .takeIf { it >= 0 }
                    ?: servers.indexOfFirst { it.name.equals(historyServerName, true) }
                        .takeIf { it >= 0 }
                    ?: 0
                val server = servers[targetServerIdx]

                // Chọn tập: theo requestedEpisode → history → đầu
                val targetEpisode = server.episodes.firstOrNull { it.slug == requestedEpisode }
                    ?: server.episodes.firstOrNull { it.slug == history?.episodeSlug }
                    ?: server.episodes.first()

                resumePositionMs =
                    if (history?.episodeSlug == targetEpisode.slug) history.positionMs else 0L

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        movieName = detail.movie.name,
                        movieOriginName = detail.movie.originName,
                        posterUrl = detail.movie.posterUrl,
                        trailerUrl = detail.movie.trailerUrl,
                        movieYear = detail.movie.year,
                        movieLang = detail.movie.lang,
                        servers = servers,
                        currentServerIndex = targetServerIdx,
                        currentEpisodeSlug = targetEpisode.slug,
                    )
                }
                play(targetEpisode, resumePositionMs)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.readableMessage()) }
            }
        }
    }

    /**
     * Chuyển server (Vietsub ↔ Thuyết Minh ↔ Lồng tiếng).
     * Cố gắng giữ tập hiện tại theo slug; nếu không có thì lấy đầu.
     */
    fun switchServer(serverIndex: Int) {
        val state = _uiState.value
        val targetServer = state.servers.getOrNull(serverIndex) ?: return
        if (serverIndex == state.currentServerIndex) return
        saveCurrentProgress()
        val targetEp = targetServer.episodes.firstOrNull { it.slug == state.currentEpisodeSlug }
            ?: targetServer.episodes.firstOrNull()
            ?: return
        _uiState.update {
            it.copy(
                currentServerIndex = serverIndex,
                currentEpisodeSlug = targetEp.slug,
            )
        }
        play(targetEp, startPositionMs = 0L)
    }

    fun switchEpisode(episode: EpisodeDto, autoPlay: Boolean = false) {
        if (episode.slug == _uiState.value.currentEpisodeSlug && !autoPlay) return
        saveCurrentProgress()
        _uiState.update { it.copy(currentEpisodeSlug = episode.slug) }
        play(episode, startPositionMs = 0L)
    }

    private fun play(episode: EpisodeDto, startPositionMs: Long) {
        val mediaItem = MediaItem.fromUri(episode.linkM3u8)
        player.setMediaItem(mediaItem, startPositionMs)
        player.prepare()
        player.playWhenReady = true
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
            }
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceIn(0, player.duration.coerceAtLeast(0)))
    }

    fun seekRelative(deltaMs: Long) {
        val target = (player.currentPosition + deltaMs).coerceIn(0, player.duration.coerceAtLeast(0))
        player.seekTo(target)
    }

    fun setPlaybackSpeed(speed: Float) {
        player.playbackParameters = PlaybackParameters(speed, 1.0f)
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    fun toggleControls() {
        _uiState.update { it.copy(controlsVisible = !it.controlsVisible) }
    }

    fun setControlsVisible(visible: Boolean) {
        _uiState.update { it.copy(controlsVisible = visible) }
    }

    fun toggleLock() {
        _uiState.update { it.copy(isLocked = !it.isLocked, controlsVisible = !it.isLocked) }
    }

    fun pause() = player.pause()

    /** Lưu tiến độ xem định kỳ 5s một lần trong khi đang phát. */
    private fun startProgressSaver() {
        if (progressJob?.isActive == true) return
        progressJob = viewModelScope.launch {
            while (isActive) {
                delay(5_000)
                saveCurrentProgress()
                val pos = runCatching { player.currentPosition }.getOrDefault(0L)
                val dur = runCatching { player.duration }.getOrDefault(0L)
                val buf = runCatching { player.bufferedPosition }.getOrDefault(0L)
                _uiState.update {
                    it.copy(positionMs = pos, durationMs = dur, bufferedMs = buf)
                }
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

    private fun PlaybackException.readablePlayerMessage(): String = when (errorCodeName) {
        "ERROR_CODE_IO_NETWORK_CONNECTION_FAILED",
        "ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT" -> "Lỗi kết nối mạng, kiểm tra internet và thử lại"
        "ERROR_CODE_PARSING_CONTAINER_MALFORMED",
        "ERROR_CODE_PARSING_MANIFEST_MALFORMED" -> "Nguồn phát bị lỗi, thử đổi server hoặc tập khác"
        "ERROR_CODE_DECODER_INIT_FAILED",
        "ERROR_CODE_DECODER_QUERY_FAILED" -> "Thiết bị không hỗ trợ định dạng này"
        else -> localizedMessage ?: "Không thể phát tập phim này, thử đổi server"
    }
}
