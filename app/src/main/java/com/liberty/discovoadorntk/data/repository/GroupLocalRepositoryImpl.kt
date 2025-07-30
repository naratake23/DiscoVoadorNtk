package com.liberty.discovoadorntk.data.repository

import com.liberty.discovoadorntk.data.local.dao.GroupDao
import com.liberty.discovoadorntk.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupLocalRepositoryImpl @Inject constructor(
    private val dao: GroupDao
) : GroupLocalRepository {

    override suspend fun insert(group: GroupEntity) {
        dao.insert(group)
    }

    override suspend fun getById(groupId: String): GroupEntity? =
        dao.findById(groupId)


    override suspend fun deleteById(groupId: String) {
        dao.deleteById(groupId)
    }

    override fun observeAll(): Flow<List<GroupEntity>> =
        dao.observeAll()
}