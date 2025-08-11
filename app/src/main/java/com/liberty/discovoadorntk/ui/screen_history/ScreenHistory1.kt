package com.liberty.discovoadorntk.ui.screen_history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.liberty.discovoadorntk.core.BaseBox
import com.liberty.discovoadorntk.core.BaseLazyColumn
import com.liberty.discovoadorntk.core.BaseText
import com.liberty.discovoadorntk.core.HistoryListItem
import com.liberty.discovoadorntk.core.LoadingScreen
import com.liberty.discovoadorntk.core.MainSurfaceNColumn
import com.liberty.discovoadorntk.core.VerticalSpacer

//Tela de histórico de mensagens de um grupo.
//paddingValues Espaçamento vindo do Scaffold (status bar, nav bar).
//groupId       ID do grupo cujas mensagens serão carregadas.
//onBack        Callback para voltar à tela anterior.
@Composable
fun ScreenHistory1(
    paddingValues: PaddingValues,
    groupId: String?,
    navigateToGroupScreen: () -> Unit,
) {
    //Instancia o ViewModel e coleta o estado da UI
    val vm: ViewModelHistoryS1 = hiltViewModel()
    val uiStateHS1 by vm.uiState.collectAsState()
    //Trata a ação de voltar do sistema
    BackHandler {
        navigateToGroupScreen()
    }
    //Layout principal: surface + coluna
    MainSurfaceNColumn(paddingValues = paddingValues) {
        if (uiStateHS1.isLoading) {
            //Mostra loading enquanto carrega os dados
            LoadingScreen()
        } else {
            VerticalSpacer(15)
            //Título da seção
            BaseText(text = "message history".uppercase(), fontSize = 18)
            VerticalSpacer(18)
            //Lista de mensagens em ordem cronológica inversa
            BaseBox(textTag = null, paddingHorizontal = 0, paddingTop = 6, paddingBottom = 6) {
                BaseLazyColumn(
                    listItens = uiStateHS1.messages,
                    reverseLayout = true,
                    weight = 1f,
                    content = { messageInfo ->
                        HistoryListItem(alarmMessages = messageInfo)
                    })
            }
        }
    }
}