package com.liberty.discovoadorntk.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.liberty.discovoadorntk.data.local.dao.AlarmMessageDao
import com.liberty.discovoadorntk.data.local.dao.GroupDao
import com.liberty.discovoadorntk.data.local.dao.MutedUserDao
import com.liberty.discovoadorntk.data.local.entity.AlarmMessageEntity
import com.liberty.discovoadorntk.data.local.entity.GroupEntity
import com.liberty.discovoadorntk.data.local.entity.MutedUserEntity

@Database(
    entities = [GroupEntity::class, AlarmMessageEntity::class, MutedUserEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmMessageDao(): AlarmMessageDao
    abstract fun groupDao(): GroupDao
    abstract fun mutedUserDao(): MutedUserDao
}