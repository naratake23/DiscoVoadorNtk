package com.liberty.discovoadorntk.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.liberty.discovoadorntk.ui.screen_entry.ScreenEntry
import com.liberty.discovoadorntk.ui.screen_group.ScreenGroup1
import com.liberty.discovoadorntk.ui.screen_history.ScreenHistory1

//Configura o grafo de navegação da aplicação usando o NavHost do Compose.
//Recebe paddingValues do Scaffold para respeitar barras de status e navegação.
@Composable
fun MyNavGraph(
    paddingValues: PaddingValues
) {
    // Contrói o NavController que irá coordenar as transições de tela
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = SCNavigationRoutes.Entry.route
    ) {
        // 1) Tela de entrada (Entry Screen)
        composable(SCNavigationRoutes.Entry.route) {
            ScreenEntry(
                paddingValues = paddingValues,
                navigateToGroupScreen = { groupId, groupName ->
                    // Navega para a rota de Grupo, garantindo único top e estado restaurado
                    navController.navigate(
                        SCNavigationRoutes.Group.createGroupRoute(groupId, groupName)
                    ) {
                        popUpTo(SCNavigationRoutes.Entry.route) { inclusive = false }
                        launchSingleTop = true
                        restoreState    = true
                    }
                }
            )
        }

        // 2) Tela de Grupo (Group Screen) com argumentos groupId e groupName
        composable(
            route = SCNavigationRoutes.Group.route,
            arguments = listOf(
                navArgument(SCNavigationRoutes.ARG_GROUP_ID)   { type = NavType.StringType },
                navArgument(SCNavigationRoutes.ARG_GROUP_NAME) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId   = backStackEntry.arguments!!.getString(SCNavigationRoutes.ARG_GROUP_ID)!!
            val groupName = backStackEntry.arguments!!.getString(SCNavigationRoutes.ARG_GROUP_NAME)!!
            ScreenGroup1(
                groupId             = groupId,
                groupName           = groupName,
                paddingValues       = paddingValues,
                navigateToEntryScreen   = { navController.popBackStack() },
                navigateToHistoryScreen = { id ->
                    // Navega para histórico do grupo
                    navController.navigate(
                        SCNavigationRoutes.History.createHistoryRoute(id)
                    )
                }
            )
        }

        // 3) Tela de Histórico (History Screen) com argumento groupId
        composable(
            route = SCNavigationRoutes.History.route,
            arguments = listOf(
                navArgument(SCNavigationRoutes.ARG_GROUP_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments!!.getString(SCNavigationRoutes.ARG_GROUP_ID)!!
            ScreenHistory1(
                paddingValues      = paddingValues,
                groupId            = groupId,
                navigateToGroupScreen = { navController.popBackStack() }
            )
        }
    }
}
