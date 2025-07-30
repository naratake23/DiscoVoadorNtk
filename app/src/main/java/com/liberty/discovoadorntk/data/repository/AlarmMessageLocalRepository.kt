package com.liberty.discovoadorntk.data.repository

import com.liberty.discovoadorntk.data.local.entity.AlarmMessageEntity
import kotlinx.coroutines.flow.Flow

interface AlarmMessageLocalRepository {
    suspend fun insert(message: AlarmMessageEntity)
    fun observeByGroupId(groupId: String): Flow<List<AlarmMessageEntity>>
    suspend fun deleteAllByGroupId(groupId: String)
}