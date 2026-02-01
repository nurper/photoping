package com.photoping.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.photoping.data.photo.PhotoPingDto
import com.photoping.ui.viewmodel.AppViewModel
import com.photoping.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    appViewModel: AppViewModel,
    onOpenCamera: () -> Unit
) {
    val themeState by appViewModel.themeState.collectAsState()
    val homeViewModel: HomeViewModel = viewModel()
    val homeState by homeViewModel.state.collectAsState()

    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            hasLocationPermission(context)
        )
    }

    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { result ->
            val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            hasLocationPermission = granted
        }
    )

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            homeViewModel.refresh()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Drop a photo. See who else was here.", style = MaterialTheme.typography.titleMedium)

        if (!hasLocationPermission) {
            LocationCardGraphic(
                placeLabel = "Location permission needed",
                sensorLux = themeState.lux
            )

            Button(
                onClick = {
                    requestPermissionsLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant location permission")
            }

            Text(
                text = "We only read GPS once and round it to 4 decimals to define a place.",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            LocationCardGraphic(
                placeLabel = "Place: ${homeState.place?.placeKey ?: "…"}",
                sensorLux = themeState.lux
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onOpenCamera,
                    modifier = Modifier.weight(1f),
                    enabled = !homeState.loading
                ) {
                    Text("Take a photo")
                }
                Button(
                    onClick = { homeViewModel.refresh() },
                    modifier = Modifier.weight(1f),
                    enabled = !homeState.loading
                ) {
                    if (homeState.loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    Text("Refresh")
                }
            }

            if (homeState.error != null) {
                Text(text = homeState.error ?: "", style = MaterialTheme.typography.bodyMedium)
            }

            PhotoPingList(items = homeState.items)
        }
    }
}

private fun hasLocationPermission(context: android.content.Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

@Composable
private fun PhotoPingList(items: List<PhotoPingDto>) {
    if (items.isEmpty()) {
        Text(
            text = "No PhotoPings here yet.",
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(items) { item ->
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (!item.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(text = item.message ?: "(no message)", style = MaterialTheme.typography.bodyLarge)
                    if (!item.createdAt.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = item.createdAt, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationCardGraphic(
    placeLabel: String,
    sensorLux: Float?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(64.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRoundRect(
                            color = Color(0xFFB3E5FC),
                            topLeft = Offset(0f, 8f),
                            size = Size(size.width, size.height - 8f),
                            cornerRadius = CornerRadius(28f, 28f),
                            style = Fill
                        )

                        val center = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(Color(0xFF0288D1), radius = size.minDimension * 0.17f, center = center)

                        val pinPath = Path().apply {
                            moveTo(center.x, center.y + size.height * 0.28f)
                            lineTo(center.x - size.width * 0.14f, center.y)
                            lineTo(center.x + size.width * 0.14f, center.y)
                            close()
                        }
                        drawPath(pinPath, color = Color(0xFF01579B))
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text(text = placeLabel, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "Light sensor: ${sensorLux?.let { "%.1f".format(it) } ?: "N/A"} lux",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
