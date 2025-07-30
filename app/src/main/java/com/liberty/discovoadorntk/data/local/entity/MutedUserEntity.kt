package com.liberty.discovoadorntk.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "muted_users",
    primaryKeys = ["groupId","userId"],
    foreignKeys = [
        ForeignKey(entity = GroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MutedUserEntity(
    val groupId:    String,   // FK para groups.groupId
    val userId:     String,
    val mutedUntil: Long      // System.currentTimeMillis() + 24h

)
