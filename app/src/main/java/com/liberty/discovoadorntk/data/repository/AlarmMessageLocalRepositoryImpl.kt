package com.liberty.discovoadorntk.data.repository

import com.liberty.discovoadorntk.data.local.dao.AlarmMessageDao
import com.liberty.discovoadorntk.data.local.entity.AlarmMessageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmMessageLocalRepositoryImpl @Inject constructor(
    private val dao: AlarmMessageDao
) : AlarmMessageLocalRepository {

    override suspend fun insert(message: AlarmMessageEntity) {
        dao.insert(message)
    }

    override fun observeByGroupId(groupId: String): Flow<List<AlarmMessageEntity>> =
        dao.observeByGroup(groupId)

    override suspend fun deleteAllByGroupId(groupId: String) {
        dao.deleteAllForGroup(groupId)
    }
}