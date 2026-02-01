package com.photoping.data.photo.upload

interface ImageUploader {
    suspend fun uploadJpeg(bytes: ByteArray, pathHint: String): String
}
