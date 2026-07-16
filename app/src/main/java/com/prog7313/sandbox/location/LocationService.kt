package com.prog7313.sandbox.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val countryCode: String?,
    val countryName: String?
)

class LocationService(
    private val context: Context
) {
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<LocationResult> {
        return try {
            val location = requestFreshLocation()
                ?: return Result.failure(
                    IllegalStateException(
                        "The device location could not be determined."
                    )
                )

            val address = reverseGeocode(
                latitude = location.latitude,
                longitude = location.longitude
            )

            Result.success(
                LocationResult(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    countryCode = address?.countryCode?.lowercase(),
                    countryName = address?.countryName
                )
            )
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFreshLocation(): android.location.Location? {
        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()

            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(0)
                .setDurationMillis(15_000)
                .build()

            fusedLocationClient.getCurrentLocation(
                request,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (continuation.isActive) {
                    continuation.resume(location)
                }
            }.addOnFailureListener { exception ->
                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }

    private suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): android.location.Address? {
        if (!Geocoder.isPresent()) {
            return null
        }

        val geocoder = Geocoder(
            context,
            Locale.getDefault()
        )

        return if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1
                ) { addresses ->
                    if (continuation.isActive) {
                        continuation.resume(
                            addresses.firstOrNull()
                        )
                    }
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(
                latitude,
                longitude,
                1
            )?.firstOrNull()
        }
    }
}
