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
import com.nguonc.stream.debug.AppLogger
import com.nguonc.stream.debug.LogTags
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

    /**
     * URL phát hiện tại của tập đang xem.
     * - Nếu linkM3u8 không rỗng → ExoPlayer play trực tiếp m3u8.
     * - Nếu chỉ có linkEmbed → dùng WebView để load iframe player.
     *   (Trường hợp NguoncApi chỉ trả embed URL.)
     */
    val currentPlayUrl: String
        get() = currentEpisode?.linkM3u8?.takeIf { it.isNotBlank() }
            ?: currentEpisode?.linkEmbed.orEmpty()

    /** true nếu phải dùng WebView (chỉ có embed URL, không có m3u8). */
    val useWebView: Boolean
        get() = currentEpisode?.linkM3u8.isNullOrBlank() &&
            !currentEpisode?.linkEmbed.isNullOrBlank()
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val slug: String = savedStateHandle.get<String>("slug").orEmpty()
    private val requestedEpisode: String? = savedStateHandle["ep"]
    private val requestedServer: Int = savedStateHandle.get<Int>("server") ?: 0

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    /**
     * Host của m3u8 URL hiện tại (vd: "embed11.streamc.xyz").
     * Updated mỗi khi play() được gọi.
     * Interceptor dùng giá trị này để set Referer động cho mọi request
     * (m3u8 playlist + segments trên thais.hihihoho3.top đều cần cùng Referer).
     */
    @Volatile
    private var currentStreamHost: String = ""

    /** Player sống trong ViewModel để không mất tiến trình khi xoay màn hình. */
    val player: ExoPlayer by lazy {
        // Custom DataSourceFactory chèn Referer + User-Agent headers cho m3u8 streams.
        // Streamc.xyz CDN yêu cầu Referer = embedXX.streamc.xyz (iframe origin).
        // Referer phải ĐỘNG theo host của m3u8 URL (mỗi tập có thể khác subdomain).
        val dataSourceFactory = androidx.media3.datasource.okhttp.OkHttpDataSource.Factory(
            okhttp3.OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val request = chain.request()
                    val url = request.url.toString()
                    val referer = if (currentStreamHost.isNotBlank()) {
                        "https://$currentStreamHost/"
                    } else {
                        "https://${request.url.host}/"
                    }
                    // Phân biệt request types:
                    // - m3u8 playlist (URL chứa sUb token, host = embedXX.streamc.xyz):
                    //   CHỈ set Referer, KHÔNG set Origin → CDN trả standard HLS m3u8
                    //   (nếu có Origin → CDN trả AES-GCM encrypted m3u8 → ExoPlayer crash)
                    // - segment (URL kết thúc .png/.ts, host = philiXX.amassXX.top):
                    //   CẦN CẢ Referer VÀ Origin = https://embedXX.streamc.xyz
                    //   (nếu thiếu Origin → 403 Forbidden)
                    val isSegment = url.contains(".png") || url.contains(".ts") ||
                        url.contains(".mp4") || url.contains("amass") ||
                        url.contains("hihihoho")
                    val newRequest = if (isSegment && currentStreamHost.isNotBlank()) {
                        request.newBuilder()
                            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36")
                            .header("Referer", referer)
                            .header("Origin", "https://$currentStreamHost")
                            .build()
                    } else {
                        // m3u8 playlist — NO Origin header
                        request.newBuilder()
                            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36")
                            .header("Referer", referer)
                            .build()
                    }
                    chain.proceed(newRequest)
                }
                .build()
        )
        ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(appContext)
                    .setDataSourceFactory(dataSourceFactory)
            )
            // ⚡ Load control: buffer tối ưu cho seek mượt
            .setLoadControl(
                androidx.media3.exoplayer.DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        15000,  // minBufferMs — 15s buffer tối thiểu (để tua xa không bị trống)
                        50000,  // maxBufferMs — 50s buffer tối đa (tua xa vẫn có data)
                        1500,   // playbackBufferMs — play khi có 1.5s buffer
                        3000,   // rebufferMs — rebuffer khi buffer < 3s
                    )
                    .setTargetBufferBytes(androidx.media3.exoplayer.DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS)
                    .setPrioritizeTimeOverSizeThresholds(true)
                    .build()
            )
            .setSeekParameters(androidx.media3.exoplayer.SeekParameters.DEFAULT)
            .build().apply {
                playWhenReady = true
                // ⚡ Pause khi seek để không phát đoạn cũ trong khi buffer đoạn mới
                addListener(object : Player.Listener {
                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                            // Seek xong → set buffering state
                            _uiState.update { it.copy(isBuffering = true) }
                        }
                    }
                })
            }
    }

    private var progressJob: Job? = null
    private var resumePositionMs: Long = 0L

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            AppLogger.i(LogTags.PLAYER, "onIsPlayingChanged(isPlaying=$isPlaying)")
            _uiState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) startProgressSaver() else stopProgressSaver()
        }

        override fun onPlaybackStateChanged(state: Int) {
            val stateName = when (state) {
                Player.STATE_BUFFERING -> "BUFFERING"
                Player.STATE_READY -> "READY"
                Player.STATE_ENDED -> "ENDED"
                Player.STATE_IDLE -> "IDLE"
                else -> "UNKNOWN($state)"
            }
            AppLogger.i(LogTags.PLAYER, "onPlaybackStateChanged(state=$stateName)")
            when (state) {
                Player.STATE_BUFFERING -> {
                    _uiState.update { it.copy(isBuffering = true) }
                }
                Player.STATE_READY -> {
                    _uiState.update { it.copy(isBuffering = false, error = null) }
                    // ⚡ Auto-play khi READY (nếu chưa playing)
                    if (!player.isPlaying && player.playWhenReady == false) {
                        player.playWhenReady = true
                        AppLogger.i(LogTags.PLAYER, "Auto-play triggered on STATE_READY")
                    }
                    AppLogger.success(LogTags.PLAYER, "Player READY — video should be playing")
                }
                Player.STATE_ENDED -> {
                    AppLogger.i(LogTags.PLAYER, "Episode ENDED — auto-advancing to next episode")
                    // Tự chuyển tập kế tiếp
                    _uiState.value.nextEpisode?.let { nextEp ->
                        switchEpisode(nextEp, autoPlay = true)
                    }
                }
                Player.STATE_IDLE -> AppLogger.w(LogTags.PLAYER, "Player IDLE — no media loaded")
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            AppLogger.e(LogTags.PLAYER, "onPlayerError: errorCode=${error.errorCodeName}, msg=${error.message}", error)
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
            AppLogger.i(LogTags.PLAYER_VM, "=== PlayerViewModel.load() ===")
            AppLogger.i(LogTags.PLAYER_VM, "slug=\"$slug\", requestedEpisode=\"$requestedEpisode\", requestedServer=$requestedServer")
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val detail = repository.getMovieDetail(slug)
                val history = repository.getHistory(slug)
                if (history != null) {
                    AppLogger.i(
                        LogTags.PLAYER_VM,
                        "History found: epSlug=${history.episodeSlug}, epName=${history.episodeName}, positionMs=${history.positionMs}"
                    )
                } else {
                    AppLogger.d(LogTags.PLAYER_VM, "No history for this movie")
                }
                detail to history
            }.onSuccess { (detail, history) ->
                AppLogger.success(LogTags.PLAYER_VM, "Detail loaded: \"${detail.movie.name}\"")
                // Repository đã lọc các server không có link m3u8 hợp lệ.
                // Detail và Player dùng cùng một danh sách server, index khớp 1:1.
                val servers: List<ServerGroup> = detail.episodes
                    .mapIndexed { idx, srv ->
                        ServerGroup(
                            index = idx,
                            name = srv.serverName.ifBlank { "Server ${idx + 1}" },
                            episodes = srv.serverData
                        )
                    }

                if (servers.isEmpty()) {
                    AppLogger.e(LogTags.PLAYER_VM, "No valid servers — error: \"Phim chưa có nguồn phát\"")
                    _uiState.update {
                        it.copy(isLoading = false, error = "Phim chưa có nguồn phát")
                    }
                    return@onSuccess
                }

                AppLogger.i(
                    LogTags.PLAYER_VM,
                    "${servers.size} servers available: ${servers.joinToString { "\"${it.name}\"(${it.episodes.size} eps)" }}"
                )

                // Chọn server theo requestedServer, clamp vào khoảng hợp lệ
                val targetServerIdx = requestedServer.coerceIn(0, servers.lastIndex)
                val server = servers[targetServerIdx]
                AppLogger.i(
                    LogTags.PLAYER_VM,
                    "Selected server #${targetServerIdx + 1}=\"${server.name}\" (requestedServer=$requestedServer, clamped to $targetServerIdx)"
                )

                // Chọn tập: theo requestedEpisode → history → đầu
                val targetEpisode = server.episodes.firstOrNull { it.slug == requestedEpisode }
                    ?: server.episodes.firstOrNull { it.slug == history?.episodeSlug }
                    ?: server.episodes.first()
                AppLogger.i(
                    LogTags.PLAYER_VM,
                    "Selected episode: name=\"${targetEpisode.name}\", slug=\"${targetEpisode.slug}\""
                )
                AppLogger.d(
                    LogTags.PLAYER_VM,
                    "Episode embed URL: ${targetEpisode.linkEmbed}"
                )
                AppLogger.d(
                    LogTags.PLAYER_VM,
                    "Episode m3u8 URL: ${targetEpisode.linkM3u8.ifBlank { "(empty — will use WebView)" }}"
                )

                resumePositionMs =
                    if (history?.episodeSlug == targetEpisode.slug) history.positionMs else 0L
                if (resumePositionMs > 0) {
                    AppLogger.i(LogTags.PLAYER_VM, "Resume position: ${resumePositionMs}ms")
                }

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
                AppLogger.success(LogTags.PLAYER_VM, "State updated, calling play()...")
                play(targetEpisode, resumePositionMs)
            }.onFailure { e ->
                AppLogger.e(LogTags.PLAYER_VM, "load() FAILED: ${e.message}", e)
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
        AppLogger.i(
            LogTags.PLAYER_VM,
            "▶ play() — episode=\"${episode.name}\", slug=\"${episode.slug}\", startPos=${startPositionMs}ms"
        )
        // ⚡ LAZY EXTRACT: Nếu linkM3u8 rỗng (chưa extract), extract ngay tại đây.
        // Chỉ extract 1 tập đang play — nhanh hơn nhiều so với extract tất cả.
        if (episode.linkM3u8.isBlank() && episode.linkEmbed.isNotBlank()) {
            AppLogger.i(LogTags.PLAYER_VM, "⚡ Lazy extracting m3u8 for this episode...")
            _uiState.update { it.copy(isBuffering = true) }
            viewModelScope.launch {
                val m3u8Url = repository.extractM3u8ForEpisode(episode.linkEmbed)
                if (m3u8Url.isNotBlank()) {
                    AppLogger.success(LogTags.PLAYER_VM, "  ✓ m3u8 extracted: ${m3u8Url.take(80)}...")
                    // ⚡ Update servers list trong state để useWebView = false
                    // (nếu không update, useWebView vẫn thấy linkM3u8="" → render WebView → che ExoPlayer)
                    updateEpisodeM3u8InState(episode.slug, m3u8Url)
                    val updatedEpisode = episode.copy(linkM3u8 = m3u8Url)
                    playWithExoPlayer(updatedEpisode, startPositionMs)
                } else {
                    AppLogger.e(LogTags.PLAYER_VM, "  ⚠ m3u8 extraction failed — falling back to WebView")
                    _uiState.update {
                        it.copy(
                            isBuffering = false,
                            isPlaying = true,
                            durationMs = 0L,
                            positionMs = 0L,
                            bufferedMs = 0L,
                        )
                    }
                }
            }
            return
        }
        // Nếu đã có m3u8 (extract trước đó), play trực tiếp
        if (episode.linkM3u8.isNotBlank()) {
            playWithExoPlayer(episode, startPositionMs)
            return
        }
        // Fallback: WebView mode
        AppLogger.w(LogTags.PLAYER_VM, "⚠ No m3u8 + no embed → WebView mode")
        _uiState.update {
            it.copy(
                isBuffering = false,
                isPlaying = true,
                durationMs = 0L,
                positionMs = 0L,
                bufferedMs = 0L,
            )
        }
    }

    private fun playWithExoPlayer(episode: EpisodeDto, startPositionMs: Long) {
        AppLogger.i(LogTags.PLAYER_VM, "Using ExoPlayer mode with m3u8: ${episode.linkM3u8}")
        // ⚡ Set buffering ngay lập tức → UI hiện spinner, không hiện màn hình đen
        _uiState.update {
            it.copy(isBuffering = true, isPlaying = false)
        }
        val m3u8Host = try {
            val uri = android.net.Uri.parse(episode.linkM3u8)
            uri.host ?: ""
        } catch (e: Exception) { "" }
        currentStreamHost = m3u8Host
        AppLogger.i(LogTags.PLAYER_VM, "  Referer host set to: $m3u8Host")
        val mediaItem = MediaItem.Builder()
            .setUri(episode.linkM3u8)
            .setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
            .build()
        // ⚡ playWhenReady = false initially → chỉ play khi STATE_READY
        player.playWhenReady = false
        player.setMediaItem(mediaItem, startPositionMs)
        player.prepare()
        AppLogger.success(LogTags.PLAYER_VM, "ExoPlayer prepared (waiting for READY)")
    }

    /**
     * Update linkM3u8 cho 1 episode trong servers list (state).
     * Cần thiết sau lazy extract để useWebView computed property
     * trả về false → PlayerScreen render ExoPlayer (không WebView).
     */
    private fun updateEpisodeM3u8InState(episodeSlug: String, m3u8Url: String) {
        _uiState.update { state ->
            val updatedServers = state.servers.map { server ->
                val updatedEps = server.episodes.map { ep ->
                    if (ep.slug == episodeSlug) {
                        ep.copy(linkM3u8 = m3u8Url)
                    } else {
                        ep
                    }
                }
                server.copy(episodes = updatedEps)
            }
            state.copy(servers = updatedServers)
        }
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
        val target = positionMs.coerceIn(0, player.duration.coerceAtLeast(0))
        val currentPos = player.currentPosition
        val delta = kotlin.math.abs(target - currentPos)
        // ⚡ Nếu tua xa (>10s), pause trước khi seek để tránh phát đoạn cũ
        if (delta > 10_000) {
            AppLogger.i(LogTags.PLAYER, "seekTo: long seek (${delta}ms) — pausing first")
            player.pause()
            _uiState.update { it.copy(isBuffering = true) }
            player.seekTo(target)
            // Resume play sau khi seek (ExoPlayer tự resume khi buffer xong)
            player.playWhenReady = true
        } else {
            player.seekTo(target)
        }
    }

    fun seekRelative(deltaMs: Long) {
        val target = (player.currentPosition + deltaMs).coerceIn(0, player.duration.coerceAtLeast(0))
        seekTo(target)
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
