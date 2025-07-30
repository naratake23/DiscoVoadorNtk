package com.liberty.discovoadorntk.ui.components.snackbar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

//Emite eventos de Snackbar para toda a aplicação.
object SnackbarManager {

    private val _events = MutableSharedFlow<SnackbarEvent>()
    val events = _events.asSharedFlow()

    //Exibe um snackbar de sucesso
    fun showSuccess(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            _events.emit(SnackbarEvent.Success(message))
        }
    }

    //Exibe um snackbar de erro
    fun showError(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            _events.emit(SnackbarEvent.Error(message))
        }
    }

    //Exibe um snackbar informativo
    fun showInfo(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            _events.emit(SnackbarEvent.Info(message))
        }
    }
}