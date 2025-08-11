package com.liberty.discovoadorntk.ui.screen_history

import com.liberty.discovoadorntk.core.data.AlarmMessageInfo

//Estado da UI para ScreenHistory.
// messages - Lista de AlarmMessageInfo.
// isLoading - Indicador de loading.
data class HistoryS1States(
    val messages: List<AlarmMessageInfo> = emptyList(),
    val isLoading: Boolean = true
)