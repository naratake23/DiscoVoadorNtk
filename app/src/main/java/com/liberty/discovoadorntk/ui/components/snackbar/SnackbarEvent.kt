package com.liberty.discovoadorntk.ui.components.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.liberty.discovoadorntk.ui.theme.amareloMostardaClose
import com.liberty.discovoadorntk.ui.theme.errorContainerDark
import com.liberty.discovoadorntk.ui.theme.secondaryLight


//Eventos tipados para exibir Snackbars com diferentes níveis de severidade.
sealed class SnackbarEvent() {
    abstract val message: String

    //Cor do texto da Snackbar
    @Composable
    fun contentColor(): Color = when (this) {
        is Success -> secondaryLight
        is Error -> errorContainerDark
        is Info -> amareloMostardaClose
    }

    //Mensagem de sucesso (verde, ou secundário claro)
    data class Success(override val message: String) : SnackbarEvent()

    //Mensagem de erro (vermelho, ou container de erro)
    data class Error(override val message: String) : SnackbarEvent()

    //Mensagem informativa (amarelo, ou cor de destaque)
    data class Info(override val message: String) : SnackbarEvent()
}