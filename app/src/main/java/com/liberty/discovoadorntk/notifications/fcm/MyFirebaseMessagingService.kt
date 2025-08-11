package com.liberty.discovoadorntk.notifications.fcm


import android.app.NotificationManager
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.liberty.discovoadorntk.core.services.AlarmService
import com.liberty.discovoadorntk.core.utils.AppConstants
import com.liberty.discovoadorntk.data.repository.MutedUserLocalRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

//Recebimento e tratamento de notificações FCM e manipulação do token FCM.

//Receber o token FCM e salvá-lo no Firestore.
//Tratar as mensagens recebidas do Firebase Cloud Messaging (FCM).
//Decidir se deve acionar o alarme ou apenas mostrar uma notificação.
@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var firebaseManager: FirebaseManager
    @Inject
    lateinit var mutedRepo: MutedUserLocalRepository

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d("AlarmDebug", "senderId: ${remoteMessage.senderId} *** notificação RECEBIDA ***")

        // 1) Extrai dados
        // extract everything synchronously
        val data = remoteMessage.data
        val senderId = data[AppConstants.SENDER_ID].orEmpty()
        val groupId = data[AppConstants.GROUP_ID].orEmpty()
        val senderName = data[AppConstants.SENDER_NAME].orEmpty()
        val groupName = data[AppConstants.GROUP_NAME].orEmpty()
        val messageBody = data[AppConstants.MESSAGE_BODY].orEmpty()
        val timestamp = data[AppConstants.TIMESTAMP]?.toLongOrNull() ?: System.currentTimeMillis()

        // now switch to IO for DB lookup & service launch
        CoroutineScope(Dispatchers.IO).launch {
            val isMuted = mutedRepo
                .getMutedMembersIdsByGroupId(groupId)
                .contains(senderId)

//        val isMuted = runBlocking {
//            mutedRepo.getMutedMembersIdsByGroupId(groupId).contains(senderId)
//        }
            // 2) Monta Intent pro AlarmService
            val svcIntent =
                Intent(this@MyFirebaseMessagingService, AlarmService::class.java).apply {
                    action = AppConstants.ACTION_START_ALARM
                    putExtra(AppConstants.SENDER_NAME, senderName)
                    putExtra(AppConstants.MESSAGE_BODY, messageBody)
                    putExtra(AppConstants.GROUP_NAME, groupName)
                    putExtra(AppConstants.TIMESTAMP, timestamp)
                    putExtra(AppConstants.IS_MUTED, isMuted)
                }

            // 3) Inicia o serviço
            startForegroundService(svcIntent)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // salva no Firestore sempre que o token for renovado
        // aqui não dá pra usar coroutines diretas, mas podemos disparar um worker ou um GlobalScope
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firebaseManager.Token().saveTokenToFirestore(token)
            } catch (e: Exception) {
                Log.d("AlarmDebug", "Não foi possível salvar o novo token", e)
            }
        }
    }

}