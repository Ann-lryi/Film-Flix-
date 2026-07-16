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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DetailViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _detailState = MutableStateFlow<NetworkResult<MovieDetailResponse>>(NetworkResult.Loading())
    val detailState: StateFlow<NetworkResult<MovieDetailResponse>> = _detailState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _selectedServerIndex = MutableStateFlow(0)
    val selectedServerIndex: StateFlow<Int> = _selectedServerIndex.asStateFlow()

    private val _selectedEpisode = MutableStateFlow<EpisodeItem?>(null)
    val selectedEpisode: StateFlow<EpisodeItem?> = _selectedEpisode.asStateFlow()

    private var currentSlug: String = ""

    fun loadDetail(slug: String) {
        currentSlug = slug
        viewModelScope.launch {
            repository.getMovieDetail(slug).collect { result ->
                _detailState.value = result
                if (result is NetworkResult.Success) {
                    val servers = result.data?.episodes ?: emptyList()
                    if (servers.isNotEmpty()) {
                        val firstServer = servers.first()
                        val episodes = firstServer.getEpisodeList()
                        if (episodes.isNotEmpty() && _selectedEpisode.value == null) {
                            _selectedEpisode.value = episodes.first()
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            repository.isFavorite(slug).collectLatest { isFav ->
                _isFavorite.value = isFav
            }
        }
    }

    fun selectServer(index: Int) {
        _selectedServerIndex.value = index
        val servers = (_detailState.value as? NetworkResult.Success)?.data?.episodes ?: emptyList()
        if (index in servers.indices) {
            val episodes = servers[index].getEpisodeList()
            if (episodes.isNotEmpty()) {
                _selectedEpisode.value = episodes.first()
            }
        }
    }

    fun selectEpisode(episode: EpisodeItem) {
        _selectedEpisode.value = episode
    }

    fun toggleFavorite() {
        val movieDetail = (_detailState.value as? NetworkResult.Success)?.data?.movie ?: return
        viewModelScope.launch {
            repository.toggleFavoriteDetail(movieDetail, _isFavorite.value)
        }
    }

    fun retry() {
        if (currentSlug.isNotEmpty()) {
            loadDetail(currentSlug)
        }
    }
}

class DetailViewModelFactory(private val repository: MovieRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
