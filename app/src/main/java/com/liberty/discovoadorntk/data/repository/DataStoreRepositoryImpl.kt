package com.liberty.discovoadorntk.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : DataStoreRepository {

    //Converts the String key to a Preferences.Key<String> and writes [value] atomically
    override suspend fun saveStringDS(key: String, value: String) {
        // 1. Cria a chave de preferência tipada
        val dataStoreKey = stringPreferencesKey(key)
        // 2. Edita o DataStore numa transação de Preferences
        dataStore.edit { preferences ->
            preferences[dataStoreKey] = value
        }
    }

    //Returns a Flow that emits the current stored value for [key].
    //Every time the preference changes, the Flow emits a new value
    override fun getStringDS(key: String): Flow<String?> {
        // 1. Cria a chave tipada igual à gravação.
        val dataStoreKey = stringPreferencesKey(key)
        // 2. Map transforma o Flow<Preferences> num Flow<String?>
        return dataStore.data.map { preferences ->
            preferences[dataStoreKey]
        }
    }
}

// 1. stringPreferencesKey: cria uma chave tipada para String.
// 2. dataStore.edit: bloco transacional que persiste as mudanças.
// 3. dataStore.data: Flow de Preferences que emite sempre que algo muda.