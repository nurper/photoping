package com.photoping.data.auth

import com.photoping.config.AppConfig
import com.photoping.data.email.VerimailApi
import com.photoping.data.storage.TokenStorage

class AuthRepository(
    private val authApi: AuthApi,
    private val verimailApi: VerimailApi,
    private val tokenStore: TokenStorage
) {
    suspend fun verifyEmail(email: String): Boolean {
        if (AppConfig.verimailApiKey.isBlank()) {
            error("Missing VERIMAIL_API_KEY. Set it in front/local.properties and sync Gradle.")
        }
        val response = verimailApi.verify(email = email, key = AppConfig.verimailApiKey)
        return response.isDeliverableOk()
    }

    suspend fun register(email: String, password: String) {
        authApi.register(RegisterRequest(email = email, password = password))
    }

    suspend fun registerAndLogin(email: String, password: String) {
        register(email = email, password = password)
        login(email = email, password = password)
    }

    suspend fun login(email: String, password: String) {
        val response = authApi.login(LoginRequest(email = email, password = password))
        tokenStore.saveToken(response.requireToken())
    }
}
