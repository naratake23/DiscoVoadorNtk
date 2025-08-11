package com.liberty.discovoadorntk.di

import android.content.Context
import android.provider.Settings
import com.liberty.discovoadorntk.core.utils.ConnectivityObserver
import com.liberty.discovoadorntk.notifications.fcm.FcmHttpClient
import com.liberty.discovoadorntk.notifications.fcm.FcmNotificationSender
import com.liberty.discovoadorntk.notifications.fcm.FirebaseManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeneralModule {

    @Provides
    @Singleton
    fun provideFirebaseHelper(@DeviceIdQualifier deviceIdQualifier: String): FirebaseManager =
        FirebaseManager(deviceIdQualifier)

    @Provides
    @Singleton
    @DeviceIdQualifier
    fun provideDeviceId(@ApplicationContext context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    @Provides
    @Singleton
    fun provideConnectivityObserver(@ApplicationContext context: Context) =
        ConnectivityObserver(context)

    @Provides
    @Singleton
    fun provideFcmHttpClient(@ApplicationContext context: Context): FcmHttpClient =
        FcmHttpClient(context)

    @Provides
    @Singleton
    fun provideFcmNotificationSender(httpClient: FcmHttpClient): FcmNotificationSender =
        FcmNotificationSender(httpClient)

}