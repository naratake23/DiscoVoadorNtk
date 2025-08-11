package com.liberty.discovoadorntk.core.utils

import android.content.Context
import android.content.Intent
import com.liberty.discovoadorntk.core.services.AlarmService

//Envia Intent para parar o serviço de alarme.
fun stopAlarmService(context: Context) {
    val stopIntent = Intent(context, AlarmService::class.java).apply {
        action = AppConstants.ACTION_STOP_ALARM
    }
    context.startService(stopIntent)
}