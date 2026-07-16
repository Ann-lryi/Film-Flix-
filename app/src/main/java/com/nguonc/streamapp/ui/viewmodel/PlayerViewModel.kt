package com.nguonc.streamapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nguonc.streamapp.data.model.EpisodeItem
import com.nguonc.streamapp.data.model.EpisodeServer
import com.nguonc.streamapp.data.model.MovieDetail
import com.nguonc.streamapp.data.model.MovieDetailResponse
import com.nguonc.streamapp.data.model.NetworkResult
import com.nguonc.streamapp.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _detailState = MutableStateFlow<NetworkResult<MovieDetailResponse>>(NetworkResult.Loading())
    val detailState: StateFlow<NetworkResult<MovieDetailResponse>> = _detailState.asStateFlow()

    private val _currentServerIndex = MutableStateFlow(0)
    val currentServerIndex: StateFlow<Int> = _currentServerIndex.asStateFlow()

    private val _currentEpisode = MutableStateFlow<EpisodeItem?>(null)
    val currentEpisode: StateFlow<EpisodeItem?> = _currentEpisode.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _aspectRatioMode = MutableStateFlow(0) // 0: Fit, 1: Fill, 2: Zoom
    val aspectRatioMode: StateFlow<Int> = _aspectRatioMode.asStateFlow()

    fun loadMovieAndEpisode(slug: String, episodeSlug: String) {
        viewModelScope.launch {
            repository.getMovieDetail(slug).collect { result ->
                _detailState.value = result
                if (result is NetworkResult.Success) {
                    val servers = result.data?.episodes ?: emptyList()
                    var foundEpisode: EpisodeItem? = null
                    var foundServerIndex = 0

                    for ((sIdx, server) in servers.withIndex()) {
                        val eps = server.getEpisodeList()
                        val match = eps.find { it.slug == episodeSlug }
                        if (match != null) {
                            foundEpisode = match
                            foundServerIndex = sIdx
                            break
                        }
                    }

                    if (foundEpisode == null && servers.isNotEmpty()) {
                        foundServerIndex = 0
                        foundEpisode = servers[0].getEpisodeList().firstOrNull()
                    }

                    _currentServerIndex.value = foundServerIndex
                    _currentEpisode.value = foundEpisode
                }
            }
        }
    }

    fun switchServer(serverIndex: Int) {
        val servers = (_detailState.value as? NetworkResult.Success)?.data?.episodes ?: emptyList()
        if (serverIndex in servers.indices) {
            _currentServerIndex.value = serverIndex
            val eps = servers[serverIndex].getEpisodeList()
            val currSlug = _currentEpisode.value?.slug
            val match = eps.find { it.slug == currSlug } ?: eps.firstOrNull()
            _currentEpisode.value = match
        }
    }

    fun switchEpisode(episode: EpisodeItem) {
        _currentEpisode.value = episode
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
    }

    fun toggleLock() {
        _isLocked.value = !_isLocked.value
    }

    fun cycleAspectRatio() {
        _aspectRatioMode.value = (_aspectRatioMode.value + 1) % 3
    }
}

class PlayerViewModelFactory(private val repository: MovieRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
