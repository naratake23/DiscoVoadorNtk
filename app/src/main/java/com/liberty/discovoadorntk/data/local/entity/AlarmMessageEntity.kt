package com.liberty.discovoadorntk.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alarm_messages",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["groupId"])]
)
data class AlarmMessageEntity(
    @PrimaryKey val id: String,
    val groupId: String,  // pra relacionar à qual grupo pertence
    val senderId: String,
    val senderName: String,
    val messageBody: String,
    val timestamp: Long
)
