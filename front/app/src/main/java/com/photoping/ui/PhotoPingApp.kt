package com.photoping.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.photoping.ui.navigation.PhotoPingNavGraph
import com.photoping.ui.theme.PhotoPingTheme
import com.photoping.ui.viewmodel.AppViewModel

@Composable
fun PhotoPingApp(appViewModel: AppViewModel = viewModel()) {
    val themeState by appViewModel.themeState.collectAsState()
    PhotoPingTheme(darkTheme = themeState.isDark) {
        Surface(color = MaterialTheme.colorScheme.background) {
            PhotoPingNavGraph(appViewModel = appViewModel)
        }
    }
}
