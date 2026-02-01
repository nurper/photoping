package com.photoping.data.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationRepository(context: Context) {
    private val appContext = context.applicationContext
    private val fused = LocationServices.getFusedLocationProviderClient(appContext)

    @Volatile
    private var cachedPlace: Place? = null

    fun getCachedPlace(): Place? = cachedPlace

    @SuppressLint("MissingPermission")
    suspend fun getOrFetchPlace(): Place {
        cachedPlace?.let { return it }

        val location = suspendCancellableCoroutine { cont ->
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) cont.resume(loc)
                    else {
                        fused.lastLocation
                            .addOnSuccessListener { last ->
                                cont.resume(last)
                            }
                            .addOnFailureListener {
                                cont.resume(null)
                            }
                    }
                }
                .addOnFailureListener {
                    fused.lastLocation
                        .addOnSuccessListener { last -> cont.resume(last) }
                        .addOnFailureListener { cont.resume(null) }
                }
        }

        requireNotNull(location) { "Location unavailable" }

        val place = LocationUtils.placeKey(location.latitude, location.longitude, decimals = 4)
        cachedPlace = place
        return place
    }
}
