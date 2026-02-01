package com.photoping.config

import com.photoping.BuildConfig

object AppConfig {
    const val backendBaseUrl: String = BuildConfig.BACKEND_BASE_URL
    const val verimailBaseUrl: String = BuildConfig.VERIMAIL_BASE_URL
    const val verimailApiKey: String = BuildConfig.VERIMAIL_API_KEY
}
