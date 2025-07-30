package com.liberty.discovoadorntk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.liberty.discovoadorntk.data.local.entity.AlarmMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmMessageDao {
    //Insere ou atualiza uma mensagem de alarme
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: AlarmMessageEntity)

    //Observa todas as mensagens de um grupo, ordenadas por data de envio (ascendente). E Retorna um Flow
    // que emite atualizações em tempo real
    @Query("""
    SELECT * 
      FROM alarm_messages 
     WHERE groupId = :groupId 
  ORDER BY timestamp ASC
  """)
    fun observeByGroup(groupId: String): Flow<List<AlarmMessageEntity>>

    //Exclui todas as mensagens associadas ao grupo informado
    @Query("DELETE FROM alarm_messages WHERE groupId = :groupId")
    suspend fun deleteAllForGroup(groupId: String)
}