package com.prog7313.sandbox.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.prog7313.sandbox.location.LocationResult
import com.prog7313.sandbox.location.LocationService
import kotlinx.coroutines.launch

@Composable
fun LocationDemoScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationService = remember {
        LocationService(context.applicationContext)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var locationResult by remember {
        mutableStateOf<LocationResult?>(null)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    fun retrieveLocation() {
        if (isLoading) {
            return
        }

        scope.launch {
            isLoading = true
            errorMessage = null

            locationService.getCurrentLocation()
                .onSuccess { result ->
                    locationResult = result
                }
                .onFailure { exception ->
                    errorMessage = exception.message
                        ?: "The location could not be retrieved."
                }

            isLoading = false
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val coarseGranted =
                permissions[
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ] == true

            val fineGranted =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true

            if (coarseGranted || fineGranted) {
                retrieveLocation()
            } else {
                errorMessage =
                    "Location permission was denied."
            }
        }

    fun requestOrRetrieveLocation() {
        val coarseGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val fineGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (coarseGranted || fineGranted) {
            retrieveLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Location Access Demo",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "This demo requests foreground location " +
                    "permission, retrieves a fresh location, and " +
                    "reverse-geocodes it into a country.",
            style = MaterialTheme.typography.bodyLarge
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Emulator test location",
                    style = MaterialTheme.typography.titleMedium
                )

                Text("Durban latitude: -29.8587")
                Text("Durban longitude: 31.0218")

                Text(
                    text = "In Extended Controls → Location, " +
                            "enter the coordinates, select Set " +
                            "location, then request the location again.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Button(
            onClick = ::requestOrRetrieveLocation,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.height(22.dp)
                )
            } else {
                Text("Use my location")
            }
        }

        locationResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Location received",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Latitude: %.6f".format(
                            result.latitude
                        )
                    )

                    Text(
                        "Longitude: %.6f".format(
                            result.longitude
                        )
                    )

                    Text(
                        "Country: ${
                            result.countryName ?: "Unavailable"
                        }"
                    )

                    Text(
                        "Country code: ${
                            result.countryCode ?: "Unavailable"
                        }"
                    )
                }
            }
        }

        errorMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color =
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBack
            ) {
                Text("Back")
            }
        }
    }
}