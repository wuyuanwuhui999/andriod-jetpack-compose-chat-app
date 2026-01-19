package com.player.chat.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.player.chat.ui.page.ChatPage
import com.player.chat.ui.page.LaunchPage
import com.player.chat.ui.page.LoginPage

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
            LoginPage()
        }
        composable(Screens.Chat.route) {
            ChatPage(navController = navController)
        }
    }
}