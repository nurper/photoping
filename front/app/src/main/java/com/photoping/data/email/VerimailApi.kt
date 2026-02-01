package com.photoping.data.email

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface VerimailApi {
    @GET("v3/verify")
    suspend fun verify(
        @Query("email") email: String,
        @Query("key") key: String
    ): VerimailResponse
}

data class VerimailResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("result") val result: String? = null,
    @SerializedName("deliverable") val deliverable: Boolean? = null,
    @SerializedName("did_you_mean") val didYouMean: String? = null,
    @SerializedName("user") val user: String? = null,
    @SerializedName("domain") val domain: String? = null,
    @SerializedName("code") val code: Int? = null
) {
    fun isDeliverableOk(): Boolean = status == "success" && deliverable == true
}
