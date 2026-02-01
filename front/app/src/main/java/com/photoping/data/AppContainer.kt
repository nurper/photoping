package com.photoping.data

import android.content.Context
import com.photoping.BuildConfig
import com.photoping.config.AppConfig
import com.photoping.data.auth.AuthApi
import com.photoping.data.auth.AuthRepository
import com.photoping.data.email.VerimailApi
import com.photoping.data.location.LocationRepository
import com.photoping.data.network.AuthInterceptor
import com.photoping.data.photo.PhotoApi
import com.photoping.data.photo.PhotoRepository
import com.photoping.data.photo.upload.FirebaseStorageImageUploader
import com.photoping.data.photo.upload.ImageUploader
import com.photoping.data.storage.TokenStorage
import com.photoping.data.storage.TokenStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(appContext: Context) {
    private val tokenStore: TokenStorage = TokenStore(appContext)

    private val imageUploader: ImageUploader = FirebaseStorageImageUploader()

    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
    }

    private val backendOkHttp: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(tokenStore))
        .addInterceptor(logging)
        .build()

    private val verimailOkHttp: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val backendRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.backendBaseUrl)
        .client(backendOkHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val verimailRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.verimailBaseUrl)
        .client(verimailOkHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val authApi: AuthApi = backendRetrofit.create(AuthApi::class.java)
    private val photoApi: PhotoApi = backendRetrofit.create(PhotoApi::class.java)
    private val verimailApi: VerimailApi = verimailRetrofit.create(VerimailApi::class.java)

    val authRepository: AuthRepository = AuthRepository(
        authApi = authApi,
        verimailApi = verimailApi,
        tokenStore = tokenStore
    )

    val locationRepository: LocationRepository = LocationRepository(appContext)
    val photoRepository: PhotoRepository = PhotoRepository(photoApi, imageUploader)
}
