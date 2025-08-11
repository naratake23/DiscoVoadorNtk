package com.liberty.discovoadorntk.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

//Observa mudanças na conectividade de internet (Wi‑Fi ou dados móveis).
//context Contexto para obter ConnectivityManager.
class ConnectivityObserver(context: Context) {
    // Obtém o serviço de conectividade do sistema
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    //Retorna um Flow que emite `true` quando há conexão com internet, e `false` quando a conexão é perdida.
    fun observe(): Flow<Boolean> = callbackFlow {
        // Cria um callback para listen em eventos de rede
        val callback = object: ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true) // Internet disponível
            }
            override fun onLost(network: Network) {
                trySend(false) // Internet perdida
            }
        }

        // 1) Define o tipo de rede que interessa: precisa ter capacidade de Internet
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        // 2) Registra o callback no ConnectivityManager
        cm.registerNetworkCallback(request, callback)
        // 3) Emite o estado inicial de conectividade
        val initial = cm.activeNetwork
            ?.let { cm.getNetworkCapabilities(it) }
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        trySend(initial)
        // 4) Quando o Flow é cancelado, limpa/desregistra o callback para evitar vazamentos
        awaitClose { cm.unregisterNetworkCallback(callback) }
    }
}