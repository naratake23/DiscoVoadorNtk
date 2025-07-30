package com.liberty.discovoadorntk.data.repository

import com.liberty.discovoadorntk.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

interface GroupLocalRepository {
    suspend fun insert(group: GroupEntity)
    suspend fun getById(groupId: String): GroupEntity?
    suspend fun deleteById(groupId: String)
    fun observeAll(): Flow<List<GroupEntity>>
}