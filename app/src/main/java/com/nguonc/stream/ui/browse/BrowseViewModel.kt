package com.nguonc.stream.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nguonc.stream.data.remote.dto.CategoryDto
import com.nguonc.stream.data.repository.MovieRepository
import com.nguonc.stream.ui.home.readableMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BrowseUiState(
    val categories: List<CategoryDto> = emptyList(),
    val countries: List<CategoryDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowseUiState())
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                coroutineScope {
                    val categories = async { repository.getCategories() }
                    val countries = async { repository.getCountries() }
                    categories.await() to countries.await()
                }
            }.onSuccess { (categories, countries) ->
                _uiState.update {
                    it.copy(
                        categories = categories.sortedBy { c -> c.name },
                        countries = countries.sortedBy { c -> c.name },
                        isLoading = false,
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.readableMessage()) }
            }
        }
    }
}
