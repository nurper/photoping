package com.photoping.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.photoping.PhotoPingApplication
import com.photoping.data.location.LocationRepository
import com.photoping.data.photo.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class SubmitUiState(
    val loading: Boolean = false,
    val error: String? = null
)

class SubmitViewModel(app: Application) : AndroidViewModel(app) {
    private val container = (app as PhotoPingApplication).appContainer
    private val locationRepo: LocationRepository = container.locationRepository
    private val photoRepo: PhotoRepository = container.photoRepository

    private val _state = MutableStateFlow(SubmitUiState())
    val state: StateFlow<SubmitUiState> = _state.asStateFlow()

    fun submit(imagePath: String, message: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = SubmitUiState(loading = true)
            runCatching {
                val place = locationRepo.getOrFetchPlace()
                photoRepo.uploadPhotoToFirebase(
                    imageFile = File(imagePath),
                    message = message,
                    place = place
                )
            }.onSuccess {
                _state.value = SubmitUiState(loading = false)
                onSuccess()
            }.onFailure { e ->
                _state.value = SubmitUiState(loading = false, error = e.message ?: "Upload failed")
            }
        }
    }
}
