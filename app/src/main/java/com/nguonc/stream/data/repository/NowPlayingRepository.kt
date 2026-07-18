package com.nguonc.stream.data.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Trạng thái phim đang được phát — dùng để hiển thị MiniPlayer
 * ở bottom bar và đồng bộ với màn Player.
 */
data class NowPlayingState(
    val slug: String,
    val title: String,
    val episode: String,
    val episodeSlug: String,
    val progress: Float = 0f,
    val isPlaying: Boolean = true,
)

/**
 * Quản lý state "now playing" chia sẻ giữa màn Player
 * và MiniPlayer ở bottom bar.
 */
@Singleton
class NowPlayingRepository @Inject constructor() {
    private val _state = MutableStateFlow<NowPlayingState?>(null)
    val state: StateFlow<NowPlayingState?> = _state.asStateFlow()

    fun set(state: NowPlayingState) { _state.value = state }
    fun clear() { _state.value = null }
    fun setPlaying(isPlaying: Boolean) {
        _state.value = _state.value?.copy(isPlaying = isPlaying)
    }
    fun setProgress(progress: Float) {
        _state.value = _state.value?.copy(progress = progress)
    }
}
