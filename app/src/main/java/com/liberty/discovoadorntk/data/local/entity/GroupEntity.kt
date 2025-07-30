package com.liberty.discovoadorntk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val groupId: String,
    val groupName: String
)
