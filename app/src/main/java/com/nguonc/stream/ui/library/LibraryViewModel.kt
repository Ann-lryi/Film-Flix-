package com.nguonc.stream.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguonc.stream.data.local.FavoriteEntity
import com.nguonc.stream.data.local.HistoryEntity
import com.nguonc.stream.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MovieRepository,
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteEntity>> = repository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = repository.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun removeHistory(slug: String) {
        viewModelScope.launch { repository.removeHistory(slug) }
    }
}
