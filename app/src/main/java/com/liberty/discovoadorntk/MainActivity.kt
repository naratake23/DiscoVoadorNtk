package com.liberty.discovoadorntk

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.liberty.discovoadorntk.ui.screen_main.ScreenMain
import com.liberty.discovoadorntk.ui.theme.MyTheme
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.activity.result.contract.ActivityResultContracts
import com.liberty.discovoadorntk.notifications.AlarmChannel


//Activity principal do app:
// Solicita permissão de notificações (Android 13+).
// Garante que o canal de alarmes esteja registrado.
// Inicia a UI Compose via ScreenMain.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    //Launcher pra pedir a permissão POST_NOTIFICATIONS.
    //Se o usuário aceitar, registra o canal de alarmes.
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                // Usuário concedeu permissão → registra o canal centralizado
                AlarmChannel.createChannel(this)
            } else {
                // Sem permissão, avisa o usuário (ele pode habilitar nas configurações depois)
                Toast.makeText(
                    this,
                    "To use the app properly, notifications need to be enabled. Please grant permission in your system settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Em Android 13+ pedimos permissão; senão, já registramos o canal direto
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            AlarmChannel.createChannel(this)
        }

        //Sobe a árvore do Compose
        setContent {
            MyTheme {
                ScreenMain()
            }
        }
    }

}