package com.nguonc.stream.ui.anime

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

data class AnimeTabUiState(
    val newAnime: List<AnimeVietsubApi.AVSAnime> = emptyList(),
    val movieAnime: List<AnimeVietsubApi.AVSAnime> = emptyList(),
    val seriesAnime: List<AnimeVietsubApi.AVSAnime> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class AnimeViewModel @Inject constructor(
    private val avsApi: AnimeVietsubApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnimeTabUiState())
    val uiState: StateFlow<AnimeTabUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val newPage = withContext(Dispatchers.IO) { avsApi.getList("anime-moi", 1) }
                val moviePage = withContext(Dispatchers.IO) { avsApi.getList("anime-le", 1) }
                val seriesPage = withContext(Dispatchers.IO) { avsApi.getList("anime-bo", 1) }
                AppLogger.i(LogTags.API, "Anime: ${newPage.items.size} new, ${moviePage.items.size} movie, ${seriesPage.items.size} series")
                _uiState.update {
                    it.copy(
                        newAnime = newPage.items,
                        movieAnime = moviePage.items,
                        seriesAnime = seriesPage.items,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                AppLogger.e(LogTags.API, "Anime load failed: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Lỗi tải anime") }
            }
        }
    }
}
