package com.photoping.data.auth

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterResponse(
    @SerializedName("message") val message: String? = null
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("token_type") val tokenType: String? = null
) {
    fun requireToken(): String = accessToken ?: token
        ?: error("Backend login response did not include `access_token` or `token`.")
}
