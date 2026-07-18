package com.aho.yunphim.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aho.yunphim.data.UiState
import com.aho.yunphim.data.model.ServerGroup
import com.aho.yunphim.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PlayerUiState {
    data object Loading : PlayerUiState

    data class Error(val message: String, val isSchemaMismatch: Boolean = false) : PlayerUiState

    data class Ready(
        val title: String,
        val episodeLabel: String,
        val streamUrl: String,
        val servers: List<ServerGroup>,
        val serverIndex: Int,
        val episodeIndex: Int,
        val isResolvingFallback: Boolean = false,
        val fallbackFailed: Boolean = false,
        val streamcFailureDetail: String? = null,
    ) : PlayerUiState
}

/**
 * Chiến lược phát:
 *  1. Lấy URL từ episode.m3u8 (ưu tiên) hoặc episode.embed - đúng thứ tự ưu tiên plugin gốc dùng.
 *  2. Nếu URL thuộc họ streamc.xyz (StreamcResolver.isStreamcFamily) -> KHÔNG đưa thẳng vào
 *     ExoPlayer (chắc chắn lỗi, site yêu cầu bắt tay token trước) - chạy StreamcResolver ngay
 *     trong lúc load(), trả về URL proxy cục bộ nếu thành công.
 *  3. Nếu không thuộc họ streamc.xyz - thử phát thẳng, WebViewStreamResolver (dò tổng quát qua
 *     WebView) là lưới an toàn cuối nếu ExoPlayer báo lỗi.
 */
class PlayerViewModel(
    private val repository: MovieRepository,
    val streamcResolver: StreamcResolver,
    private val slug: String,
    private val serverIndex: Int,
    private val episodeIndex: Int,
) : ViewModel() {

    private val _state = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var detailServers: List<ServerGroup> = emptyList()
    private var movieTitle: String = ""

    init {
        load()
    }

    fun retry() = load()

    override fun onCleared() {
        super.onCleared()
        streamcResolver.stopActiveProxies()
    }

    private fun load() {
        viewModelScope.launch {
            _state.value = PlayerUiState.Loading
            when (val result = repository.fetchDetail(slug)) {
                is UiState.Success -> {
                    detailServers = result.data.servers
                    movieTitle = result.data.name.orEmpty()
                    resolveEpisode(serverIndex, episodeIndex)
                }

                is UiState.Error -> _state.value = PlayerUiState.Error(result.message, result.isSchemaMismatch)
                UiState.Loading -> Unit
            }
        }
    }

    private suspend fun resolveEpisode(sIdx: Int, eIdx: Int) {
        val episode = detailServers.getOrNull(sIdx)?.episodes?.getOrNull(eIdx)
        if (episode == null) {
            _state.value = PlayerUiState.Error(
                "Không tìm thấy tập phim tại server=$sIdx, tập=$eIdx trong response - danh sách " +
                    "server/tập có thể đã đổi thứ tự.",
            )
            return
        }
        val candidateUrl = episode.m3u8?.takeIf { it.isNotBlank() } ?: episode.embed
        if (candidateUrl.isNullOrBlank()) {
            _state.value = PlayerUiState.Error("Tập này không có link phát (cả m3u8 và embed đều trống trong JSON).")
            return
        }

        if (StreamcResolver.isStreamcFamily(candidateUrl)) {
            when (val result = streamcResolver.resolve(candidateUrl)) {
                is StreamcResolver.Result.Success -> _state.value = PlayerUiState.Ready(
                    title = movieTitle,
                    episodeLabel = episode.displayName,
                    streamUrl = result.proxyUrl,
                    servers = detailServers,
                    serverIndex = sIdx,
                    episodeIndex = eIdx,
                )

                is StreamcResolver.Result.Failure -> {
                    // Luồng token đã biết fail - không báo lỗi ngay, rơi tiếp qua
                    // WebViewStreamResolver (dò tổng quát) làm lưới an toàn cuối, giữ lý do fail
                    // cụ thể để hiện lên UI nếu WebView cũng fail nốt.
                    _state.value = PlayerUiState.Ready(
                        title = movieTitle,
                        episodeLabel = episode.displayName,
                        streamUrl = candidateUrl,
                        servers = detailServers,
                        serverIndex = sIdx,
                        episodeIndex = eIdx,
                        isResolvingFallback = true,
                        streamcFailureDetail = "[${result.step}] ${result.detail}",
                    )
                }
            }
        } else {
            _state.value = PlayerUiState.Ready(
                title = movieTitle,
                episodeLabel = episode.displayName,
                streamUrl = candidateUrl,
                servers = detailServers,
                serverIndex = sIdx,
                episodeIndex = eIdx,
            )
        }
    }

    /**
     * Gọi khi ExoPlayer báo lỗi phát trực tiếp streamUrl hiện tại (chỉ xảy ra với nhánh KHÔNG
     * thuộc họ streamc.xyz - nhánh streamc.xyz đã tự xử lý ở [resolveEpisode]). Bản thân
     * ViewModel không giữ WebView (cần Context/lifecycle của UI) - chỉ bật cờ
     * [PlayerUiState.Ready.isResolvingFallback], Composable quan sát cờ này để tự chạy
     * [WebViewStreamResolver] rồi gọi lại [onFallbackResolved]/[onFallbackError].
     */
    fun onDirectPlaybackFailed() {
        val current = _state.value
        if (current !is PlayerUiState.Ready || current.isResolvingFallback || current.fallbackFailed) return
        val episode = detailServers.getOrNull(current.serverIndex)?.episodes?.getOrNull(current.episodeIndex)
        val embedUrl = episode?.embed
        if (embedUrl.isNullOrBlank() || embedUrl == current.streamUrl) {
            _state.value = current.copy(fallbackFailed = true)
            return
        }
        _state.value = current.copy(isResolvingFallback = true)
    }

    fun embedUrlForFallback(): String? {
        val current = _state.value as? PlayerUiState.Ready ?: return null
        return detailServers.getOrNull(current.serverIndex)?.episodes?.getOrNull(current.episodeIndex)?.embed
    }

    fun onFallbackResolved(resolvedUrl: String) {
        val current = _state.value
        if (current is PlayerUiState.Ready) {
            _state.value = current.copy(streamUrl = resolvedUrl, isResolvingFallback = false)
        }
    }

    fun onFallbackError() {
        val current = _state.value
        if (current is PlayerUiState.Ready) {
            _state.value = current.copy(isResolvingFallback = false, fallbackFailed = true)
        }
    }
}
