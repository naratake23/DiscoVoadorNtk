package com.liberty.discovoadorntk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liberty.discovoadorntk.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    //Insere ou atualiza um grupo no banco.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: GroupEntity)

    //Busca um único grupo pelo seu ID; retorna null se não existir
    @Query("SELECT * FROM groups WHERE groupId = :groupId")
    suspend fun findById(groupId: String): GroupEntity?

    //Observa a lista de todos os grupos, emitindo atualizações em tempo real
    @Query("SELECT * FROM groups")
    fun observeAll(): Flow<List<GroupEntity>>

    //Remove o grupo com o ID especificado
    @Query("DELETE FROM groups WHERE groupId = :groupId")
    suspend fun deleteById(groupId: String)
}