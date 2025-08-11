package com.liberty.discovoadorntk.data.repository

import com.liberty.discovoadorntk.data.local.dao.MutedUserDao
import com.liberty.discovoadorntk.data.local.entity.MutedUserEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MutedUserLocalRepositoryImpl @Inject constructor(
    private val dao: MutedUserDao
) : MutedUserLocalRepository {

    override suspend fun mute(groupId: String, userId: String, until: Long) =
        dao.insert(MutedUserEntity(groupId, userId, mutedUntil = until))

    override suspend fun unmute(groupId: String, userId: String) =
        dao.delete(groupId, userId)

    override suspend fun deleteExpired(now: Long) =
        dao.deleteExpired(now)

    override suspend fun getMutedMembersIdsByGroupId(groupId: String) =
        dao.getMutedMembersIds(groupId)

}