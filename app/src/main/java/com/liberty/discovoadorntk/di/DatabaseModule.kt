package com.liberty.discovoadorntk.di

import android.content.Context
import androidx.room.Room
import com.liberty.discovoadorntk.data.local.AppDatabase
import com.liberty.discovoadorntk.data.local.dao.AlarmMessageDao
import com.liberty.discovoadorntk.data.local.dao.GroupDao
import com.liberty.discovoadorntk.data.local.dao.MutedUserDao
import com.liberty.discovoadorntk.data.repository.AlarmMessageLocalRepository
import com.liberty.discovoadorntk.data.repository.AlarmMessageLocalRepositoryImpl
import com.liberty.discovoadorntk.data.repository.GroupLocalRepository
import com.liberty.discovoadorntk.data.repository.GroupLocalRepositoryImpl
import com.liberty.discovoadorntk.data.repository.MutedUserLocalRepository
import com.liberty.discovoadorntk.data.repository.MutedUserLocalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            name = "db_dv"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideGroupDao(db: AppDatabase): GroupDao = db.groupDao()

    @Provides
    fun provideAlarmMessageDao(db: AppDatabase): AlarmMessageDao = db.alarmMessageDao()

    @Provides
    fun provideMutedUserDao(db: AppDatabase): MutedUserDao = db.mutedUserDao()
}



@Module
@InstallIn(SingletonComponent::class)
abstract class LocalRepositoryModule {

    @Binds
    abstract fun bindGroupLocalRepository(impl: GroupLocalRepositoryImpl): GroupLocalRepository

    @Binds
    abstract fun bindAlarmMessageLocalRepository(impl: AlarmMessageLocalRepositoryImpl): AlarmMessageLocalRepository

    @Binds
    abstract fun bindMutedUserLocalRepository(impl: MutedUserLocalRepositoryImpl): MutedUserLocalRepository
}