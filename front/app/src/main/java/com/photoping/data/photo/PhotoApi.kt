package com.photoping.data.photo

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface PhotoApi {
    @GET("photo/by-location")
    suspend fun byLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): List<PhotoPingDto>

    @Multipart
    @POST("photo")
    suspend fun upload(
        @Part photo: MultipartBody.Part,
        @Part("message") message: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("lon") lon: RequestBody
    ): UploadPhotoResponse

    @POST("photo/url")
    suspend fun createByUrl(@Body request: CreatePhotoRequest): UploadPhotoResponse
}

data class PhotoPingDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("lon") val lon: Double? = null
)

data class UploadPhotoResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("message") val message: String? = null
)

data class CreatePhotoRequest(
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("message") val message: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)
