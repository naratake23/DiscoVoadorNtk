package com.liberty.discovoadorntk.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import com.liberty.discovoadorntk.R
import com.liberty.discovoadorntk.core.utils.AppConstants

object AlarmChannel {


    // Nome amigável que o usuário vê nas configurações de notificação
    private const val CHANNEL_NAME = "Alarme Crítico dvNTK"
    // Descrição exibida nas configurações do canal de notificação
    private const val CHANNEL_DESC = "Notificações de alarme em full-screen"


//    Cria ou atualiza o canal de notificação de alarme
//    context Contexto da aplicação para acessar o NotificationManager
    fun createChannel(context: Context) {
        // Obtém o NotificationManager do sistema, ou retorna se não estiver disponível
        val nm = context.getSystemService(NotificationManager::class.java) ?: return

        // Define o canal de notificação com ID, nome e nível de importância
        val channel = NotificationChannel(
            AppConstants.ALARM_CHANNEL_ID,                         // ID do canal
            CHANNEL_NAME,                             // Nome exibido ao usuário
            NotificationManager.IMPORTANCE_HIGH       // Importância alta: toca som e mostra em destaque
        ).apply {
            description = CHANNEL_DESC               // Define a descrição do canal

            // Desativa som padrão no canal; notificações precisarão chamar setSound() se quiser som
            setSound(null, null)

            // Desativa vibração padrão no canal; use enableVibration(true) + vibrationPattern para personalizar
            enableVibration(false)

            // Controla visibilidade na tela de bloqueio: PUBLIC mostra todo conteúdo
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        // Registra ou atualiza o canal no NotificationManager
        nm.createNotificationChannel(channel)
    }
}

