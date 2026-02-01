package com.photoping.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.photoping.PhotoPingApplication
import com.photoping.data.location.Place
import com.photoping.data.location.LocationRepository
import com.photoping.data.photo.PhotoPingDto
import com.photoping.data.photo.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = false,
    val place: Place? = null,
    val items: List<PhotoPingDto> = emptyList(),
    val error: String? = null
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val container = (app as PhotoPingApplication).appContainer
    private val locationRepo: LocationRepository = container.locationRepository
    private val photoRepo: PhotoRepository = container.photoRepository

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                val place = locationRepo.getOrFetchPlace()
                val items = photoRepo.getByPlace(place)
                place to items
            }.onSuccess { (place, items) ->
                _state.value = HomeUiState(loading = false, place = place, items = items)
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, error = e.message ?: "Failed to load")
            }
        }
    }
}
