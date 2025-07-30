package com.liberty.discovoadorntk.core.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.liberty.discovoadorntk.AlarmActivity
import com.liberty.discovoadorntk.R
import com.liberty.discovoadorntk.core.utils.AlarmPlayBeep
import com.liberty.discovoadorntk.core.utils.AlarmPlayVibration
import com.liberty.discovoadorntk.core.utils.AppConstants
import com.liberty.discovoadorntk.core.utils.AppConstants.ACTION_START_ALARM
import com.liberty.discovoadorntk.core.utils.AppConstants.ACTION_STOP_ALARM
import com.liberty.discovoadorntk.notifications.AlarmChannel


//Serviço que gerencia o alarme crítico em foreground:
//- toca som e vibra;
//- exibe notificação full‑screen;
//- oferece ação de parar alarme.
class AlarmService : Service() {

    private val alarmPlayBeep by lazy { AlarmPlayBeep(this) }
    private val alarmPlayVibration by lazy { AlarmPlayVibration(this) }

    //flag interna para saber se já chamamos startForeground (evita registrar múltiplas vezes)
    private var isForegroundService = false

    //inteiro usado para gerar IDs únicos para cada PendingIntent e cada notificação
    private var uniqueCode = 0



    //método chamado quando alguém faz startService(intent)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        AlarmChannel.createChannel(this)

        when (intent?.action) {
            ACTION_START_ALARM -> startAlarm(intent)
            ACTION_STOP_ALARM  -> stopAlarm()
        }
        //indica ao sistema que, se o serviço for morto, não deve ser reiniciado automaticamente
        return START_NOT_STICKY
    }


    //método que dispara o alarme
    private fun startAlarm(intent: Intent) {

        //Lê do Intent se o alarme deve ficar mudo
        val isMuted = intent.getBooleanExtra(AppConstants.IS_MUTED, false)

        if(!isMuted) {
            // toca o som
            alarmPlayBeep.start()
            }
        // vibra
        alarmPlayVibration.start()

        // 2) Prepara PendingIntent para abrir AlarmActivity full-screen
        //Cria um Intent para sua AlarmActivity, com flags que limpam qualquer instância anterior e abrem em nova tarefa.
        val fullIntent = Intent(this, AlarmActivity::class.java).apply {
            // força que, ao abrir AlarmActivity, limpe qualquer instância anterior:
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtras(intent.extras!!)
        }
        // 3) Gera um uniqueCode (timestamp como inteiro) para diferenciar vários PendingIntent
        uniqueCode = System.currentTimeMillis().toInt()
        val fullPI = PendingIntent.getActivity(
            this, uniqueCode, fullIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4) Constrói notificação de alta prioridade
        val builder = NotificationCompat.Builder(this, AppConstants.ALARM_CHANNEL_ID)
            //toca som/vibração apenas ao criar, não em updates
            .setOnlyAlertOnce(true)
            //ícone que aparece na barra
            .setSmallIcon(R.drawable.modified_saturn)
            .setContentTitle("Alert from ${intent.getStringExtra(AppConstants.GROUP_NAME)}")
            .setContentText("${intent.getStringExtra(AppConstants.SENDER_NAME)}: ${intent.getStringExtra(AppConstants.MESSAGE_BODY)}")
            //fazem a notificação se comportar como uma chamada de emergência
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)

            //força abertura de AlarmActivity em tela cheia
            .setFullScreenIntent(fullPI, true)
            //ação padrão ao tocar na notificação
            .setContentIntent(fullPI) // faz o click na notificação levar também ao Alarm
            //não remove a notificação ao tocar nela
            .setAutoCancel(false)

        // 5) Condicionalmente, adiciona botão/ação “Stop” se não for mudo
        if (!isMuted) {
            val stopPI = PendingIntent.getService(
                this,
                uniqueCode + 1,
                Intent(this, AlarmService::class.java).setAction(ACTION_STOP_ALARM),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(R.drawable.ufo_outline, "Stop", stopPI)
        }

        // Constrói o objeto Notification final
        val notif = builder.build()

        // 6) Inicia ou atualiza o foreground service com esse ID único.
        // Caso já esteja (ou seja segunda notificação), “remove” a antiga e promove a nova.
        if (!isForegroundService) {
            //Isso eleva o serviço a “foreground” e expõe a notificação permanentemente na barra
            startForeground(uniqueCode, notif)
            isForegroundService = true
        } else {
            // remove a notificação anterior para a canalização normal (fica empilhada lá)
            stopForeground(Service.STOP_FOREGROUND_DETACH)
            // agora promove esta nova notificação em foreground, atualizando-a
            startForeground(uniqueCode, notif)
        }

    }

    private fun stopAlarm() {

        alarmPlayBeep.stop()
        alarmPlayVibration.stop()

        //Remove o serviço do foreground
        stopForeground(Service.STOP_FOREGROUND_DETACH)
//        val nm = getSystemService(NotificationManager::class.java)!!
//        nm.cancel(uniqueCode)
        //encerrar o próprio serviço
        stopSelf()
        //Reseta a flag isForegroundService
        isForegroundService = false
    }

    override fun onBind(intent: Intent?) = null
}

