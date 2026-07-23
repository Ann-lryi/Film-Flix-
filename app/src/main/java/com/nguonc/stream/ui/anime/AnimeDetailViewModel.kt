package com.nguonc.stream.ui.anime

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguonc.stream.data.remote.AnimeVietsubApi
import com.nguonc.stream.debug.AppLogger
import com.nguonc.stream.debug.LogTags
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AnimeDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val detail: AnimeVietsubApi.AVSDetail? = null,
    val selectedEpisodeUrl: String? = null,
    val playerIframeUrl: String? = null,
    val isPlayerLoading: Boolean = false,
)

@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    private val avsApi: AnimeVietsubApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnimeDetailUiState())
    val uiState: StateFlow<AnimeDetailUiState> = _uiState.asStateFlow()

    fun load(slug: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val detail = withContext(Dispatchers.IO) { avsApi.getDetail(slug) }
                AppLogger.i(LogTags.API, "AnimeDetail: ${detail.title}, ${detail.episodes.size} eps")
                _uiState.update {
                    it.copy(isLoading = false, detail = detail)
                }
                // Auto-select first episode
                if (detail.episodes.isNotEmpty()) {
                    selectEpisode(detail.episodes.first().url)
                }
            } catch (e: Exception) {
                AppLogger.e(LogTags.API, "AnimeDetail load failed: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Lỗi tải chi tiết") }
            }
        }
    }

    fun selectEpisode(episodeUrl: String) {
        _uiState.update {
            it.copy(selectedEpisodeUrl = episodeUrl, isPlayerLoading = true, playerIframeUrl = null)
        }
        viewModelScope.launch {
            try {
                val player = withContext(Dispatchers.IO) { avsApi.getPlayer(episodeUrl) }
                AppLogger.i(LogTags.API, "AnimePlayer: iframe=${player.iframeUrl.take(80)}")
                _uiState.update {
                    it.copy(playerIframeUrl = player.iframeUrl, isPlayerLoading = false)
                }
            } catch (e: Exception) {
                AppLogger.e(LogTags.API, "AnimePlayer load failed: ${e.message}", e)
                _uiState.update { it.copy(isPlayerLoading = false) }
            }
        }
    }
}
