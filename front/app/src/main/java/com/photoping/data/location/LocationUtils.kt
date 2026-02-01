package com.photoping.data.location

import java.util.Locale
import kotlin.math.pow

object LocationUtils {
    fun round(value: Double, decimals: Int = 4): Double {
        val factor = 10.0.pow(decimals)
        return kotlin.math.round(value * factor) / factor
    }

    fun placeKey(lat: Double, lon: Double, decimals: Int = 4): Place {
        val latR = round(lat, decimals)
        val lonR = round(lon, decimals)
        val key = String.format(Locale.US, "%.${decimals}f,%.${decimals}f", latR, lonR)
        return Place(latRounded = latR, lonRounded = lonR, placeKey = key)
    }
}
