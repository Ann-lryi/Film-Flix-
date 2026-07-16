package com.nguonc.streamapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nguonc.streamapp.data.model.MovieListResponse
import com.nguonc.streamapp.data.model.NetworkResult
import com.nguonc.streamapp.data.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchState = MutableStateFlow<NetworkResult<MovieListResponse>>(NetworkResult.Success(MovieListResponse()))
    val searchState: StateFlow<NetworkResult<MovieListResponse>> = _searchState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.trim().isEmpty()) {
            _searchState.value = NetworkResult.Success(MovieListResponse())
            return
        }
        searchJob = viewModelScope.launch {
            delay(500) // Debounce 500ms
            performSearch(query)
        }
    }

    fun performSearch(query: String = _searchQuery.value) {
        if (query.trim().isEmpty()) return
        viewModelScope.launch {
            repository.searchMovies(query.trim()).collect { result ->
                _searchState.value = result
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchState.value = NetworkResult.Success(MovieListResponse())
        searchJob?.cancel()
    }
}

class SearchViewModelFactory(private val repository: MovieRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
