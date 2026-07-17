package com.nguonc.stream.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguonc.stream.data.remote.PhimApi
import com.nguonc.stream.data.remote.dto.MovieItemDto
import com.nguonc.stream.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeSection(
    val title: String,
    val listType: String,
    val items: List<MovieItemDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

data class HomeUiState(
    val sections: List<HomeSection> = emptyList(),
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val sectionDefs = listOf(
        "Phim mới cập nhật" to PhimApi.TYPE_NEW,
        "Phim lẻ" to PhimApi.TYPE_MOVIE,
        "Phim bộ" to PhimApi.TYPE_SERIES,
        "Hoạt hình" to PhimApi.TYPE_CARTOON,
    )

    init {
        _uiState.value = HomeUiState(
            sections = sectionDefs.map { (title, type) -> HomeSection(title = title, listType = type) }
        )
        loadAll()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadAll()
    }

    /** Tải song song 4 mục; mục nào lỗi chỉ ảnh hưởng mục đó. */
    private fun loadAll() {
        viewModelScope.launch {
            coroutineScope {
                sectionDefs.mapIndexed { index, (_, type) ->
                    async {
                        runCatching { repository.getMovieList(type, page = 1) }
                            .onSuccess { page ->
                                updateSection(index) {
                                    it.copy(items = page.items, isLoading = false, error = null)
                                }
                            }
                            .onFailure { e ->
                                updateSection(index) {
                                    it.copy(isLoading = false, error = e.readableMessage())
                                }
                            }
                    }
                }.forEach { it.await() }
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun retrySection(listType: String) {
        val index = sectionDefs.indexOfFirst { it.second == listType }
        if (index < 0) return
        updateSection(index) { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching { repository.getMovieList(listType, page = 1) }
                .onSuccess { page ->
                    updateSection(index) { it.copy(items = page.items, isLoading = false) }
                }
                .onFailure { e ->
                    updateSection(index) { it.copy(isLoading = false, error = e.readableMessage()) }
                }
        }
    }

    private fun updateSection(index: Int, transform: (HomeSection) -> HomeSection) {
        _uiState.update { state ->
            state.copy(
                sections = state.sections.mapIndexed { i, section ->
                    if (i == index) transform(section) else section
                }
            )
        }
    }
}

internal fun Throwable.readableMessage(): String = when (this) {
    is java.net.UnknownHostException -> "Không có kết nối mạng"
    is java.net.SocketTimeoutException -> "Kết nối quá chậm, vui lòng thử lại"
    else -> localizedMessage ?: "Đã có lỗi xảy ra"
}
