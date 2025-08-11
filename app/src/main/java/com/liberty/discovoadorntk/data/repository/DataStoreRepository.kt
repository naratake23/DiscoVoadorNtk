package com.liberty.discovoadorntk.data.repository

import kotlinx.coroutines.flow.Flow

//Abstraction over AndroidX DataStore to persist simple key-value pairs
interface DataStoreRepository {

    suspend fun saveStringDS(key: String, value: String)
    fun getStringDS(key: String): Flow<String?>
}