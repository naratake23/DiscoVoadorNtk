package com.liberty.discovoadorntk.core.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService

class AlarmPlayVibration(context: Context) {
    private val vibrator = context.getSystemService<Vibrator>()
        ?: throw IllegalStateException("Vibrator not available")

    @Suppress("DEPRECATION")
    fun start() {
            val pattern = longArrayOf(0, 150, 100, 150, 100, 150, 2000)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    fun stop() {
        vibrator.cancel()
    }
}