package com.liberty.discovoadorntk.ui.screen_main

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.liberty.discovoadorntk.ui.components.snackbar.SnackbarEvent
import com.liberty.discovoadorntk.ui.components.snackbar.SnackbarManager
import com.liberty.discovoadorntk.ui.navigation.MyNavGraph

@Composable
fun ScreenMain() {

    val snackbarHostState = remember { SnackbarHostState() }
    var lastEvent by remember { mutableStateOf<SnackbarEvent?>(null) }

    // Coleta e dispara o Snackbar
    LaunchedEffect(Unit) {
        SnackbarManager.events.collect { event ->
            lastEvent = event
            snackbarHostState.showSnackbar(message = event.message, withDismissAction = true)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    // Puxa cor direto do último evento
                    val contentColor = lastEvent?.contentColor() ?: MaterialTheme.colorScheme.onSurface

                    Snackbar(
                        snackbarData = snackbarData,
                        contentColor = contentColor
                    )
                }
            )
        }

    ) { paddingValues ->
        MyNavGraph(
            paddingValues = paddingValues,
        )

    }
}

