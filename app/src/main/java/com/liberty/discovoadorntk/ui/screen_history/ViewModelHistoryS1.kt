package com.liberty.discovoadorntk.ui.screen_history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.liberty.discovoadorntk.core.data.AlarmMessageInfo
import com.liberty.discovoadorntk.data.repository.AlarmMessageLocalRepository
import com.liberty.discovoadorntk.ui.navigation.SCNavigationRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//ViewModel da tela de histórico, observa o repositório local para atualizações.
@HiltViewModel
class ViewModelHistoryS1 @Inject constructor(
    application: Application,
    private val alarmMessageLocalRepo: AlarmMessageLocalRepository,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    // Recupera o groupId passado pela rota; falha rápido se não vier
    private val groupId: String =
        requireNotNull(savedStateHandle[SCNavigationRoutes.ARG_GROUP_ID]) {
            throw IllegalArgumentException("groupId não informado na rota")
        }

    private val _uiState = MutableStateFlow(HistoryS1States())
    val uiState = _uiState.asStateFlow()

    init {

        // Observa diretamente o banco local
            alarmMessageLocalRepo
                .observeByGroupId(groupId)   // Flow<List<AlarmMessageEntity>>
                .map { entities ->
                    entities.map { e ->
                        AlarmMessageInfo(
                            id = e.id,
                            senderId = e.senderId,
                            senderName = e.senderName,
                            messageBody = e.messageBody,
                            timestamp = e.timestamp
                        )
                    }
                }
                .map { it.reversed() }
                .onEach { messages ->
                    // Atualiza estado: desativa loading e preenche mensagens
                    _uiState.update { it.copy(messages = messages, isLoading = false) }
                }
                .launchIn(viewModelScope)
    }


}