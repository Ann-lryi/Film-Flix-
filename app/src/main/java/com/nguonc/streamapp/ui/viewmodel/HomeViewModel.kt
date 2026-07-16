package com.nguonc.streamapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nguonc.streamapp.data.model.Categories
import com.nguonc.streamapp.data.model.MovieItem
import com.nguonc.streamapp.data.model.MovieListResponse
import com.nguonc.streamapp.data.model.NetworkResult
import com.nguonc.streamapp.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _recentMoviesState = MutableStateFlow<NetworkResult<MovieListResponse>>(NetworkResult.Loading())
    val recentMoviesState: StateFlow<NetworkResult<MovieListResponse>> = _recentMoviesState.asStateFlow()

    private val _filterMoviesState = MutableStateFlow<NetworkResult<MovieListResponse>>(NetworkResult.Loading())
    val filterMoviesState: StateFlow<NetworkResult<MovieListResponse>> = _filterMoviesState.asStateFlow()

    private val _selectedFilterSlug = MutableStateFlow("phim-moi-cap-nhat")
    val selectedFilterSlug: StateFlow<String> = _selectedFilterSlug.asStateFlow()

    private val _selectedFilterName = MutableStateFlow("Mới nhất")
    val selectedFilterName: StateFlow<String> = _selectedFilterName.asStateFlow()

    private val _heroMovie = MutableStateFlow<MovieItem?>(null)
    val heroMovie: StateFlow<MovieItem?> = _heroMovie.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    init {
        loadRecentMovies()
    }

    fun loadRecentMovies(page: Int = 1) {
        viewModelScope.launch {
            _currentPage.value = page
            repository.getRecentlyUpdated(page).collect { result ->
                _recentMoviesState.value = result
                if (result is NetworkResult.Success) {
                    val items = result.data?.items ?: emptyList()
                    if (items.isNotEmpty() && _heroMovie.value == null) {
                        _heroMovie.value = items.first()
                    }
                    if (_selectedFilterSlug.value == "phim-moi-cap-nhat") {
                        _filterMoviesState.value = result
                    }
                }
            }
        }
    }

    fun selectFilter(slug: String, name: String, isCountry: Boolean = false) {
        _selectedFilterSlug.value = slug
        _selectedFilterName.value = name
        _currentPage.value = 1

        if (slug == "phim-moi-cap-nhat") {
            _filterMoviesState.value = _recentMoviesState.value
            return
        }

        viewModelScope.launch {
            val flow = if (isCountry) {
                repository.getMoviesByCountry(slug, 1)
            } else {
                repository.getMoviesByCategory(slug, 1)
            }
            flow.collect { result ->
                _filterMoviesState.value = result
                if (result is NetworkResult.Success && _heroMovie.value == null) {
                    val items = result.data?.items ?: emptyList()
                    if (items.isNotEmpty()) {
                        _heroMovie.value = items.first()
                    }
                }
            }
        }
    }

    fun refresh() {
        if (_selectedFilterSlug.value == "phim-moi-cap-nhat") {
            loadRecentMovies(_currentPage.value)
        } else {
            selectFilter(_selectedFilterSlug.value, _selectedFilterName.value)
        }
    }

    fun setHeroMovie(movie: MovieItem) {
        _heroMovie.value = movie
    }
}

class HomeViewModelFactory(private val repository: MovieRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
