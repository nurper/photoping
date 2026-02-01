package com.photoping.data.photo.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

object ImageCompressor {
    suspend fun compressToJpegBytes(
        file: File,
        maxDimension: Int,
        quality: Int
    ): ByteArray = withContext(Dispatchers.Default) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)

        val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)

        val opts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val decoded = BitmapFactory.decodeFile(file.absolutePath, opts)
            ?: error("Failed to decode image")

        val scaled = scaleDownIfNeeded(decoded, maxDimension)

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)

        if (scaled !== decoded) scaled.recycle()
        decoded.recycle()

        out.toByteArray()
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        if (width <= 0 || height <= 0) return 1
        val largest = max(width, height)
        var sample = 1
        while (largest / sample > maxDim * 2) {
            sample *= 2
        }
        return sample
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val largest = max(w, h)
        if (largest <= maxDim) return bitmap

        val scale = maxDim.toFloat() / largest.toFloat()
        val newW = (w * scale).toInt().coerceAtLeast(1)
        val newH = (h * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }
}
