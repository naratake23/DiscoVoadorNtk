package com.liberty.discovoadorntk.notifications.fcm

import android.util.Log
import com.liberty.discovoadorntk.core.data.AlarmMessageInfo
import com.liberty.discovoadorntk.core.data.GroupInfo
import com.liberty.discovoadorntk.core.utils.AppConstants
import com.liberty.discovoadorntk.notifications.AlarmChannel
import org.json.JSONObject

//Camada de serviço responsável por montar o payload FCM e enviar
//notificações usando o FcmHttpClient.
class FcmNotificationSender(
    private val fcmHttpClient: FcmHttpClient
) {

//    Envia uma notificação FCM para cada token informado.
    suspend fun sendNotifications(
        tokens: List<String>,
        messageInfo: AlarmMessageInfo,
        groupInfo: GroupInfo
    ) {
        // 1) Gera token Bearer
        val bearer = fcmHttpClient.getBearerToken()

        // 2) Pré-monta configurações Android e payload data
        val androidJson = JSONObject().apply {
            put("priority", "HIGH")
            put("direct_boot_ok", true)
        }

        val dataJson = JSONObject().apply {
            put(AppConstants.SENDER_ID, messageInfo.senderId)
            put(AppConstants.GROUP_ID, groupInfo.groupId)
            put(AppConstants.MESSAGE_ID, messageInfo.id)
            put(AppConstants.SENDER_NAME, messageInfo.senderName)
            put(AppConstants.GROUP_NAME, groupInfo.groupName)
            put(AppConstants.MESSAGE_BODY, messageInfo.messageBody)
            put(AppConstants.TIMESTAMP, messageInfo.timestamp.toString())
        }

        // 3) Para cada token, monta e envia
        tokens.forEach { token ->
            val messageJson = JSONObject().apply {
                put("token", token)
                put("android", androidJson)
                put("data", dataJson)
            }
            val envelope = JSONObject().put("message", messageJson).toString()

            // 4) Envia via HTTP client (em contexto I/O)
            Log.d("AlarmDebug", "senderId: ${messageInfo.senderId} *** notificação ENVIADA ***")
                fcmHttpClient.postJson(FcmConfig.ENDPOINT, bearer, envelope)
        }
    }
}