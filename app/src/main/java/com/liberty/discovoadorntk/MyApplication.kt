package com.liberty.discovoadorntk


import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import java.security.Security
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

//       override fun onCreate() {
//              super.onCreate()
//              // Coloca o BC em primeiro lugar no JCA
//              Security.insertProviderAt(BouncyCastleProvider(), 1)
//          }
}
