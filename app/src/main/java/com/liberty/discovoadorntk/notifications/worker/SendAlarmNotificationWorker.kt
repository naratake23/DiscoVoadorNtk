package com.liberty.discovoadorntk.notifications.worker

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.liberty.discovoadorntk.core.data.AlarmMessageInfo
import com.liberty.discovoadorntk.core.data.GroupInfo
import com.liberty.discovoadorntk.notifications.fcm.FcmNotificationSender
import com.liberty.discovoadorntk.notifications.fcm.FirebaseManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


//Worker encarregado de processar e enviar notificações de alarme de mensagens de grupo.
//Recebe dados de entrada (ID do grupo, nome do grupo, ID da mensagem, informações do remetente e texto)
//e realiza as seguintes etapas:
//1. Validação dos dados de entrada.
//2. Persistência da mensagem no Firebase.
//3. Recuperação dos tokens dos membros do grupo.
//4. Envio de notificações via FCM.
@HiltWorker
class SendAlarmNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,                // Contexto do aplicativo para WorkManager
    @Assisted params: WorkerParameters,        // Parâmetros do Worker (inclui inputData)
    private val firebaseManager: FirebaseManager,      // Gerenciador customizado do Firebase
    private val fcmSender: FcmNotificationSender      // Sender customizado para FCM
) : CoroutineWorker(context, params) {

    companion object {
        // Chaves esperadas em inputData para recuperar valores
        const val KEY_GROUP_ID = "groupId"
        const val KEY_GROUP_NAME = "groupName"
        const val KEY_MESSAGE_ID = "messageId"
        const val KEY_SENDER_ID = "senderId"
        const val KEY_SENDER_NAME = "senderName"
        const val KEY_MESSAGE_TEXT = "messageText"
    }

    //Executa o trabalho de envio de notificações. Em caso de falha recuperável,
    //retorna Result.retry(); em sucesso ou cancelamento, retorna Result.success().
    override suspend fun doWork(): Result {
        return try {
            // Extrai e valida cada campo obrigatório do inputData
            val groupId = inputData.getString(KEY_GROUP_ID)
                ?: return Result.failure()   // Falha imediata se não existir
            val groupName = inputData.getString(KEY_GROUP_NAME)
                ?: return Result.failure()
            val messageId = inputData.getString(KEY_MESSAGE_ID)
                ?: return Result.failure()
            val senderId = inputData.getString(KEY_SENDER_ID)
                ?: return Result.failure()
            val senderName = inputData.getString(KEY_SENDER_NAME)
                ?: return Result.failure()
            val messageText = inputData.getString(KEY_MESSAGE_TEXT)
                ?: return Result.failure()

            // Monta objeto de informação da mensagem de alarme
            val alarmMsg = AlarmMessageInfo(
                id = messageId,
                senderName = senderName,
                senderId = senderId,
                messageBody = messageText
            )

            // Monta objeto de informação do grupo
            val groupInfo = GroupInfo(
                groupId = groupId,
                groupName = groupName
            )

            // Persiste a mensagem de alarme no banco de dados Firebase
            firebaseManager.Message()
                .saveAlarmMessage(groupId, alarmMsg)

            // Busca tokens dos dispositivos dos membros do grupo para envio de notificações
            val tokens = firebaseManager.Token()
                .fetchGroupMemberTokens(groupId)
            Log.d("AlarmDebug", "Fetched tokens: $tokens")

            // Envia notificações FCM para todos os tokens recuperados
            fcmSender.sendNotifications(tokens, alarmMsg, groupInfo)
            Log.d("AlarmDebug", "Notifications sent successfully")

            // Retorna sucesso após completar todas as etapas
            Result.success()

        } catch (e: Exception) {
            // Em caso de erro inesperado, registra e solicita nova tentativa
            Log.e("AlarmDebug", "Worker failed: ${e.message}", e)
            Result.retry()
        }
    }
}