package com.liberty.discovoadorntk

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.liberty.discovoadorntk.core.BaseButton
import com.liberty.discovoadorntk.core.BaseText
import com.liberty.discovoadorntk.core.BracketedText
import com.liberty.discovoadorntk.core.VerticalSpacer
import com.liberty.discovoadorntk.core.services.AlarmService
import com.liberty.discovoadorntk.core.utils.AppConstants
import com.liberty.discovoadorntk.core.utils.stopAlarmService
import com.liberty.discovoadorntk.core.utils.toFormatTimestamp
import com.liberty.discovoadorntk.ui.theme.MyTheme
import dagger.hilt.android.AndroidEntryPoint

//Activity exibida quando um alarme de mensagem é disparado.
//Mostra remetente, grupo, corpo, timestamp e botão para parar o alarme.
@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    // Propriedades lazy para extrair dados da Intent
    private val senderName: String by lazy { intent.getStringExtra(AppConstants.SENDER_NAME).orEmpty() }
    private val messageBody: String by lazy { intent.getStringExtra(AppConstants.MESSAGE_BODY).orEmpty() }
    private val groupName:   String by lazy { intent.getStringExtra(AppConstants.GROUP_NAME).orEmpty() }
    private val timestamp:   Long   by lazy { intent.getLongExtra(AppConstants.TIMESTAMP, 0L) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configura a Activity para ligar a tela e exibir sobre o keyguard
        setupScreenOn(this)

        // Definição do conteúdo Compose
        setContent {
            MyTheme {
                AlarmScreen(
                    senderName = senderName,
                    groupName  = groupName,
                    message    = messageBody,
                    timestamp  = timestamp,
                    onStopAlarm = { stopAlarmService(this) }
                )
            }
        }
    }

    //Ajusta flags para exibir Activity acima da lockscreen e ligar a tela.
    private fun setupScreenOn(context: ComponentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            context.setTurnScreenOn(true)
            context.setShowWhenLocked(true)
            (context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager)
                .requestDismissKeyguard(context, null)
        } else {
            @Suppress("DEPRECATION")
            context.window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }



    //Composable que renderiza t0do o layout do alarme.
    @Composable
    private fun AlarmScreen(
        senderName: String,
        groupName: String,
        message: String,
        timestamp: Long,
        onStopAlarm: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(9.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            //Exibe o remetente
            BaseText(
                text = "From: $senderName",
                fontSize = 20,
            )
            VerticalSpacer(15)
            //Exibe o nome do grupo
            BaseText(
                text = "Group: $groupName",
                fontSize = 20,
            )
            VerticalSpacer(30)

            BracketedText(message = message, fontSize = 30, lineHeight = 33)
            VerticalSpacer()

            //Exibe data e hora
            BaseText(
                text = timestamp.toFormatTimestamp(),
                fontSize = 15,
                textAlign = TextAlign.End
            )
            VerticalSpacer(75)
            BaseButton(text = "Stop the alarm") {
                //A Activity permanece aberta até o usuário sair manualmente clicando no botão de voltar do sistema
                //Não chamamos finish(); a Activity só fechará se o usuário apertar Voltar.
                onStopAlarm()
            }
        }

    }
}
