package com.photoping.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.photoping.ui.viewmodel.AppViewModel
import com.photoping.ui.viewmodel.SubmitViewModel
import java.io.File

@Composable
fun SubmitScreen(
    appViewModel: AppViewModel,
    imagePath: String?,
    onDone: () -> Unit
) {
    val submitViewModel: SubmitViewModel = viewModel()
    val submitState by submitViewModel.state.collectAsState()

    var message by remember { mutableStateOf("") }
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "New PhotoPing")
        Spacer(modifier = Modifier.height(16.dp))

        if (imagePath == null) {
            Text(text = "Image missing")
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            AsyncImage(
                model = File(imagePath),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (!hasLocationPermission) {
            Text("Location permission is needed to tag this PhotoPing.")
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    requestPermissionsLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            ) {
                Text("Grant location permission")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (submitState.error != null) {
            Text(text = submitState.error ?: "")
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") }
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !submitState.loading && hasLocationPermission && imagePath != null,
            onClick = {
                val path = imagePath ?: return@Button
                submitViewModel.submit(imagePath = path, message = message, onSuccess = onDone)
            }
        ) {
            if (submitState.loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text("Submit")
        }
    }
}
