package com.photoping

import android.app.Application
import com.google.firebase.FirebaseApp
import com.photoping.data.AppContainer

class PhotoPingApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        appContainer = AppContainer(this)
    }
}
