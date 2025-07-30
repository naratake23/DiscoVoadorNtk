package com.liberty.discovoadorntk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liberty.discovoadorntk.data.local.entity.MutedUserEntity

@Dao
interface MutedUserDao {

    //Insere ou atualiza (até 24h) o usuário mutado
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mutedUser: MutedUserEntity)

    //Remove o silenciamento de um usuário específico em um grupo
    @Query("DELETE FROM muted_users WHERE groupId = :groupId AND userId = :userId")
    suspend fun delete(groupId: String, userId: String)

    //Limpa todos os registros cujo tempo de silenciamento já expirou
    @Query("DELETE FROM muted_users WHERE mutedUntil < :now")
    suspend fun deleteExpired(now: Long)

    //Retorna apenas os IDs dos usuários ainda silenciados em um grupo
    @Query("SELECT userId FROM muted_users WHERE groupId = :groupId")
    suspend fun getMutedMembersIds(groupId: String): List<String>
}
