package com.liberty.discovoadorntk.ui.navigation

import android.net.Uri

// Define as rotas tipadas da aplicação para uso com o NavHost.
// Cada objeto representa uma tela, e encapsula a rota e eventuais parâmetros
sealed class SCNavigationRoutes(val route: String) {

    companion object {
        // Chaves usadas nos argumentos de navegação
        const val ARG_GROUP_ID = "groupId"
        const val ARG_GROUP_NAME = "groupName"
    }

    //Rota da tela de entrada, sem parâmetros
    object Entry : SCNavigationRoutes(route = "entry_screen")

    //Rota da tela de grupo
    object Group : SCNavigationRoutes(route = "group_screen/{$ARG_GROUP_ID}/{$ARG_GROUP_NAME}") {
        fun createGroupRoute(groupId: String, groupName: String) =
        //Monta a rota completa, codificando nomes com espaços ou caracteres especiais
            //Uri.encode para garantir que nomes com espaços, “ç” ou “/” não quebrem a URL
            "group_screen/$groupId/${Uri.encode(groupName)}"
    }

    //Rota da tela de histórico de mensagens de um grupo.
    object History : SCNavigationRoutes(route = "history_screen/{$ARG_GROUP_ID}") {
        //Monta a rota completa para o histórico
        fun createHistoryRoute(groupId: String): String = "history_screen/$groupId"
    }
}
