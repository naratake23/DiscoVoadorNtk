package com.liberty.discovoadorntk.core.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.liberty.discovoadorntk.R


// Gerencia a reprodução de um “beep” de alarme com intervalo entre cada som.
class AlarmPlayBeep(private val context: Context) {
    // Cria um Handler atrelado ao Looper da Main Thread (UI thread).
    // Serve para agendar execução de Runnables no futuro.
    private val handler = Handler(Looper.getMainLooper())

    // Flag que indica se estamos no modo “tocando beeps”.
    // Usada para interromper o loop e evitar reagendamentos.
    private var isPlaying = false

    // Referência ao MediaPlayer atualmente executando o som.
    // Permite pará‑lo e liberá‑lo ao chamar stop().
    private var currentPlayer: MediaPlayer? = null

    // Runnable que encapsula toda a lógica de tocar um beep e
    // reagendar a si mesmo após 2s, enquanto isPlaying == true.
    private val playRunnable = object : Runnable {
        override fun run() {
            // Se já não estivermos mais tocando (stop() foi chamado), aborta aqui.
            if (!isPlaying) return

            // Cria e prepara automaticamente um MediaPlayer para o recurso raw.
            val mp = MediaPlayer.create(context, R.raw.alienxxx_beep)

            // Armazena a instância atual para podermos pará‑la depois.
            currentPlayer = mp

            // Registra callback para quando o som terminar de tocar.
            mp.setOnCompletionListener { player ->
                // Ao completar, libera os recursos internos do MediaPlayer.
                player.release()
                // Zera a referência para evitar vazamentos.
                currentPlayer = null

                // Se ainda estivermos em modo “playing”, agenda de novo daqui 2s.
                if (isPlaying) {
                    handler.postDelayed(this, 1935)
                }
            }

            // Inicia imediatamente a reprodução do beep.
            mp.start()
        }
    }


    //Inicia o loop de beeps:
    //- Marca isPlaying = true;
    //- Cancela qualquer chamada pendente de playRunnable;
    //- Executa playRunnable imediatamente.

    fun start() {
        // Se já estiver tocando, não faz nada (evita chamadas duplicadas).
        if (isPlaying) return

        // Sinaliza que entramos no modo “tocando”.
        isPlaying = true

        // Remove qualquer execução futura que estivesse agendada.
        handler.removeCallbacks(playRunnable)

        // Dispara o primeiro beep agora, via Runnable.
        playRunnable.run()
    }


    //Para o loop de beeps:
    //- Marca isPlaying = false;
    //- Cancela callbacks futuros;
    //- Para e libera o player ativo (se existir).

    fun stop() {
        // Desliga o modo “tocando” — faz run() abortar em próximas execuções.
        isPlaying = false

        // Cancela qualquer agendamento remanescente do Runnable.
        handler.removeCallbacks(playRunnable)

        // Se existir um MediaPlayer tocando, pare-o e libere seus recursos:
        currentPlayer?.let { player ->
            // Se ainda estiver reproduzindo, para imediatamente.
            if (player.isPlaying) player.stop()
            // Libera memória nativa e componentes internos.
            player.release()
            // Zera referência para evitar leaks.
            currentPlayer = null
        }
    }
}