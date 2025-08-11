package com.liberty.discovoadorntk.data.repository

interface MutedUserLocalRepository {
    suspend fun mute(groupId: String, userId: String, until: Long)
    suspend fun unmute(groupId: String, userId: String)
    suspend fun deleteExpired(now: Long)
    suspend fun getMutedMembersIdsByGroupId(groupId: String): List<String>
}