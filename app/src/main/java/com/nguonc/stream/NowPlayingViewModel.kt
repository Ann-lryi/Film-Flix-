package com.nguonc.stream

import androidx.lifecycle.ViewModel
import com.nguonc.stream.data.repository.NowPlayingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val repository: NowPlayingRepository,
) : ViewModel() {
    val nowPlaying: StateFlow<com.nguonc.stream.data.repository.NowPlayingState?> =
        repository.state

    fun togglePlay() {
        val current = repository.state.value ?: return
        repository.setPlaying(!current.isPlaying)
    }
}
