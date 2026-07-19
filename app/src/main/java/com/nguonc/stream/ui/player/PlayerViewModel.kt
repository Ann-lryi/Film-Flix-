package com.nguonc.stream.ui.player

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.nguonc.stream.data.remote.PhimApi
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

data class PlayerUiState(
    val isLoading: Boolean = true,
    /** Lỗi khi tải metadata phim (API) — chưa có gì để phát, hiện full-screen error. */
    val error: String? = null,
    /** Lỗi phát sinh trong lúc phát video (mạng rớt giữa chừng, CDN từ chối...) — khác với `error`. */
    val playerError: String? = null,
    val isBuffering: Boolean = false,
    val movieName: String = "",
    val posterUrl: String = "",
    /** Toàn bộ server trả về từ API, KHÔNG lọc theo linkM3u8 — để danh sách tập khớp 100% với DetailScreen. */
    val servers: List<EpisodeServerDto> = emptyList(),
    val currentServerIndex: Int = 0,
    val currentEpisodeSlug: String = "",
) {
    val currentServer: EpisodeServerDto? get() = servers.getOrNull(currentServerIndex)
    val currentEpisodes: List<EpisodeDto> get() = currentServer?.serverData.orEmpty()
    val currentEpisode: EpisodeDto? get() = currentEpisodes.firstOrNull { it.slug == currentEpisodeSlug }
    val currentEpisodeName: String get() = currentEpisode?.name.orEmpty()
    /** Tên hiển thị của server hiện tại (vd "Vietsub", "Lồng Tiếng") — dùng cho chip chuyển server trong player. */
    val currentServerName: String get() = currentServer?.serverName.orEmpty()
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val slug: String = checkNotNull(savedStateHandle["slug"])
    private val requestedEpisode: String? = savedStateHandle["ep"]
    private val requestedServerIndex: Int = savedStateHandle["server"] ?: 0

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    /**
     * Player sống trong ViewModel để không mất tiến trình khi xoay màn hình.
     * Gắn User-Agent giống hệt OkHttp (PhimApi.USER_AGENT) — CDN segment .m3u8/.ts
     * của một số nguồn từ chối request không có User-Agent hợp lệ.
     */
    val player: ExoPlayer by lazy {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(PhimApi.USER_AGENT)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(15_000)
        val mediaSourceFactory = DefaultMediaSourceFactory(appContext)
            .setDataSourceFactory(dataSourceFactory)
        ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply { playWhenReady = true }
    }

    private var progressJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) startProgressSaver() else stopProgressSaver()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _uiState.update {
                it.copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                    // Phát được rồi thì bỏ lỗi cũ (vd sau khi buffer lại thành công)
                    playerError = if (playbackState == Player.STATE_READY) null else it.playerError,
                )
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _uiState.update { it.copy(isBuffering = false, playerError = error.toFriendlyMessage()) }
        }
    }

    init {
        player.addListener(playerListener)
        load()
    }

    /** Tải lại toàn bộ metadata phim từ API — dùng khi mở màn hình hoặc lỗi tải ban đầu. */
    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, playerError = null) }
            runCatching {
                val detail = repository.getMovieDetail(slug)
                val history = repository.getHistory(slug)
                detail to history
            }.onSuccess { (detail, history) ->
                val servers = detail.episodes
                if (servers.isEmpty() || servers.all { it.serverData.isEmpty() }) {
                    _uiState.update { it.copy(isLoading = false, error = "Phim chưa có nguồn phát") }
                    return@onSuccess
                }

                val resolvedServerIndex = requestedServerIndex.coerceIn(0, servers.lastIndex)
                val targetServer = servers[resolvedServerIndex]
                val episodesInServer = targetServer.serverData
                val targetEpisode = episodesInServer.firstOrNull { it.slug == requestedEpisode }
                    ?: episodesInServer.firstOrNull()

                if (targetEpisode == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Server \"${targetServer.serverName}\" chưa có tập nào") }
                    return@onSuccess
                }

                val resumePositionMs = if (
                    history != null &&
                    history.episodeSlug == targetEpisode.slug &&
                    history.serverIndex == resolvedServerIndex
                ) history.positionMs else 0L

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        movieName = detail.movie.name,
                        posterUrl = detail.movie.posterUrl,
                        servers = servers,
                        currentServerIndex = resolvedServerIndex,
                        currentEpisodeSlug = targetEpisode.slug,
                    )
                }
                playEpisode(resolvedServerIndex, targetEpisode, resumePositionMs)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.readableMessage()) }
            }
        }
    }

    /** Đổi tập trong CÙNG server đang xem. */
    fun switchEpisode(episode: EpisodeDto) {
        val state = _uiState.value
        if (episode.slug == state.currentEpisodeSlug) return
        saveCurrentProgress()
        playEpisode(state.currentServerIndex, episode, startPositionMs = 0L)
    }

    /**
     * Đổi server (vd Vietsub ↔ Lồng Tiếng) — cố gắng giữ nguyên tập đang xem (khớp theo slug)
     * và giữ nguyên vị trí phát hiện tại để chuyển track mượt, không phải xem lại từ đầu.
     */
    fun switchServer(newServerIndex: Int) {
        val state = _uiState.value
        if (newServerIndex == state.currentServerIndex) return
        val newServer = state.servers.getOrNull(newServerIndex) ?: return
        val currentPositionMs = runCatching { player.currentPosition }.getOrDefault(0L).coerceAtLeast(0L)
        saveCurrentProgress()
        val matchedEpisode = newServer.serverData.firstOrNull { it.slug == state.currentEpisodeSlug }
            ?: newServer.serverData.firstOrNull()
            ?: return
        _uiState.update { it.copy(currentServerIndex = newServerIndex) }
        playEpisode(newServerIndex, matchedEpisode, startPositionMs = currentPositionMs)
    }

    /** Thử phát lại tập/server hiện tại từ vị trí đang dừng — dùng cho nút Thử lại khi lỗi phát sinh giữa chừng. */
    fun retryPlayback() {
        val state = _uiState.value
        val episode = state.currentEpisode ?: return
        val position = runCatching { player.currentPosition }.getOrDefault(0L).coerceAtLeast(0L)
        playEpisode(state.currentServerIndex, episode, startPositionMs = position)
    }

    private fun playEpisode(serverIndex: Int, episode: EpisodeDto, startPositionMs: Long) {
        if (episode.linkM3u8.isBlank()) {
            _uiState.update {
                it.copy(
                    currentServerIndex = serverIndex,
                    currentEpisodeSlug = episode.slug,
                    playerError = "Tập này chưa có liên kết phát trực tiếp trong ứng dụng.",
                )
            }
            return
        }
        _uiState.update {
            it.copy(currentServerIndex = serverIndex, currentEpisodeSlug = episode.slug, playerError = null)
        }
        player.setMediaItem(MediaItem.fromUri(episode.linkM3u8), startPositionMs)
        player.prepare()
        player.playWhenReady = true
    }

    fun pause() {
        runCatching { player.pause() }
    }

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
        val duration = runCatching { player.duration }.getOrDefault(C.TIME_UNSET)
            .takeIf { it != C.TIME_UNSET && it > 0L } ?: 0L
        viewModelScope.launch {
            repository.saveProgress(
                slug = slug,
                name = state.movieName,
                posterUrl = state.posterUrl,
                episodeSlug = state.currentEpisodeSlug,
                episodeName = state.currentEpisodeName,
                positionMs = position,
                durationMs = duration,
                serverIndex = state.currentServerIndex,
            )
        }
    }

    override fun onCleared() {
        stopProgressSaver()
        runCatching { player.removeListener(playerListener) }
        runCatching { player.release() }
        super.onCleared()
    }
}

/** Dịch lỗi kỹ thuật ExoPlayer (tiếng Anh) sang thông báo tiếng Việt dễ hiểu cho người xem. */
private fun PlaybackException.toFriendlyMessage(): String = when (errorCode) {
    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "Mất kết nối mạng khi đang phát"
    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
    PlaybackException.ERROR_CODE_IO_NO_PERMISSION -> "Nguồn phát bị từ chối hoặc không còn khả dụng"
    PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
    PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED,
    PlaybackException.ERROR_CODE_PARSING_MANIFEST_UNSUPPORTED -> "Định dạng video không hợp lệ hoặc chưa được hỗ trợ"
    PlaybackException.ERROR_CODE_DECODING_FAILED,
    PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> "Thiết bị không giải mã được video này"
    else -> "Không thể phát video (mã lỗi $errorCode)"
}
