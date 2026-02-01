package com.photoping.data.photo

import com.photoping.data.location.Place
import com.photoping.data.photo.image.ImageCompressor
import com.photoping.data.photo.upload.ImageUploader
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class PhotoRepository(
    private val photoApi: PhotoApi,
    private val imageUploader: ImageUploader
) {
    suspend fun getByPlace(place: Place): List<PhotoPingDto> {
        return photoApi.byLocation(lat = place.latRounded, lon = place.lonRounded)
    }

    suspend fun uploadPhoto(
        imageFile: File,
        message: String,
        place: Place
    ): UploadPhotoResponse {
        val jpegBytes = ImageCompressor.compressToJpegBytes(
            file = imageFile,
            maxDimension = 1280,
            quality = 80
        )

        val imageBody = jpegBytes.toRequestBody("image/jpeg".toMediaType())
        val part = MultipartBody.Part.createFormData(
            name = "photo",
            filename = imageFile.name,
            body = imageBody
        )

        val messageBody = message.toRequestBody("text/plain".toMediaType())
        val latBody = place.latRounded.toString().toRequestBody("text/plain".toMediaType())
        val lonBody = place.lonRounded.toString().toRequestBody("text/plain".toMediaType())

        return photoApi.upload(
            photo = part,
            message = messageBody,
            lat = latBody,
            lon = lonBody
        )
    }

    suspend fun uploadPhotoToFirebase(
        imageFile: File,
        message: String,
        place: Place
    ): UploadPhotoResponse {
        val jpegBytes = ImageCompressor.compressToJpegBytes(
            file = imageFile,
            maxDimension = 1280,
            quality = 80
        )

        val imageUrl = imageUploader.uploadJpeg(bytes = jpegBytes, pathHint = place.placeKey)
        return photoApi.createByUrl(
            CreatePhotoRequest(
                imageUrl = imageUrl,
                message = message,
                lat = place.latRounded,
                lon = place.lonRounded
            )
        )
    }
}
