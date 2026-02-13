package com.player.chat.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.player.chat.ui.page.ChatPage
import com.player.chat.ui.page.LaunchPage
import com.player.chat.ui.page.LoginPage
import com.player.chat.ui.page.TenantManagePage
import com.player.chat.ui.page.UserPage

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screens.Launch.route
    ) {
        composable(Screens.Launch.route) {
            LaunchPage(navController = navController)
        }
        composable(Screens.Login.route) {
            LoginPage(navController = navController)
        }
        composable(Screens.Chat.route) {
            ChatPage(navController = navController)
        }

        composable(Screens.User.route) {
            UserPage(navController = navController)
        }
        composable(Screens.TenantManage.route) {
            TenantManagePage(navController = navController)
        }
    }
}