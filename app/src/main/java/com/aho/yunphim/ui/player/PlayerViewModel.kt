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
    ) : PlayerUiState
}

class PlayerViewModel(
    private val repository: MovieRepository,
    private val slug: String,
    private val serverIndex: Int,
    private val episodeIndex: Int,
) : ViewModel() {

    private val _state = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var detailServers: List<ServerGroup> = emptyList()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            _state.value = PlayerUiState.Loading
            when (val result = repository.fetchDetail(slug)) {
                is UiState.Success -> {
                    detailServers = result.data.servers
                    val episode = detailServers.getOrNull(serverIndex)?.serverData?.getOrNull(episodeIndex)
                    if (episode == null) {
                        _state.value = PlayerUiState.Error(
                            "Không tìm thấy tập phim tại server=$serverIndex, tập=$episodeIndex trong " +
                                "response - danh sách server/tập có thể đã đổi thứ tự.",
                        )
                        return@launch
                    }
                    val url = episode.linkM3u8?.takeIf { it.isNotBlank() } ?: episode.linkEmbed
                    if (url.isNullOrBlank()) {
                        _state.value = PlayerUiState.Error(
                            "Tập này không có link phát (cả link_m3u8 và link_embed đều trống trong JSON).",
                        )
                        return@launch
                    }
                    _state.value = PlayerUiState.Ready(
                        title = result.data.detail.name.orEmpty(),
                        episodeLabel = episode.displayName,
                        streamUrl = url,
                        servers = detailServers,
                        serverIndex = serverIndex,
                        episodeIndex = episodeIndex,
                    )
                }

                is UiState.Error -> _state.value = PlayerUiState.Error(result.message, result.isSchemaMismatch)
                UiState.Loading -> Unit
            }
        }
    }

    /**
     * Gọi khi ExoPlayer báo lỗi phát trực tiếp streamUrl hiện tại. Bản thân ViewModel không giữ
     * WebView (cần Context/lifecycle của UI) - chỉ bật cờ [PlayerUiState.Ready.isResolvingFallback],
     * Composable quan sát cờ này để tự chạy [WebViewStreamResolver] rồi gọi lại
     * [onFallbackResolved]/[onFallbackError].
     */
    fun onDirectPlaybackFailed() {
        val current = _state.value
        if (current !is PlayerUiState.Ready || current.isResolvingFallback || current.fallbackFailed) return
        val episode = detailServers.getOrNull(current.serverIndex)?.serverData?.getOrNull(current.episodeIndex)
        val embedUrl = episode?.linkEmbed
        if (embedUrl.isNullOrBlank() || embedUrl == current.streamUrl) {
            _state.value = current.copy(fallbackFailed = true)
            return
        }
        _state.value = current.copy(isResolvingFallback = true)
    }

    fun embedUrlForFallback(): String? {
        val current = _state.value as? PlayerUiState.Ready ?: return null
        return detailServers.getOrNull(current.serverIndex)?.serverData?.getOrNull(current.episodeIndex)?.linkEmbed
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
