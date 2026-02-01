package com.photoping.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.photoping.data.sensor.LightSensorRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ThemeState(
    val isDark: Boolean,
    val lux: Float? = null
)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val lightRepo = LightSensorRepository(app)

    val themeState: StateFlow<ThemeState> = lightRepo.lux
        .map { lux ->
            val dark = lux != null && lux < 40f
            ThemeState(isDark = dark, lux = lux)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeState(isDark = false, lux = null))
}
