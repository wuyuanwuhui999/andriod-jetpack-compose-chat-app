package com.player.chat.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.player.chat.ui.page.AddCompanyUserPage
import com.player.chat.ui.page.AddModelPage
import com.player.chat.ui.page.AddPromptPage
import com.player.chat.ui.page.AddTenantUserPage
import com.player.chat.ui.page.ChatPage
import com.player.chat.ui.page.CompanyPage
import com.player.chat.ui.page.ForgetPasswordPage
import com.player.chat.ui.page.LaunchPage
import com.player.chat.ui.page.LoginPage
import com.player.chat.ui.page.ModelManagePage
import com.player.chat.ui.page.PromptManagePage
import com.player.chat.ui.page.RegisterPage
import com.player.chat.ui.page.ResetPasswordPage
import com.player.chat.ui.page.TenantManagePage
import com.player.chat.ui.page.UpdateModelPage
import com.player.chat.ui.page.UpdatePasswordPage
import com.player.chat.ui.page.UserManagePage
import com.player.chat.ui.page.UserPage  // 确保这行存在且只导入一次

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
        composable(Screens.UpdatePassword.route) {
            UpdatePasswordPage(navController = navController)
        }
        // 在 AppNavGraph 函数中添加
        composable(Screens.ForgetPassword.route) {
            ForgetPasswordPage(navController = navController)
        }
        composable(Screens.ResetPassword.route) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPasswordPage(navController = navController, email = email)
        }

        composable(Screens.Register.route) {
            RegisterPage(navController = navController)
        }

        composable(Screens.Company.route) {
            CompanyPage(navController = navController)
        }

        composable(Screens.Company.route) {
            CompanyPage(navController = navController)
        }

        composable(Screens.UserManage.route) {
            UserManagePage(navController = navController)
        }

        composable(Screens.AddUser.route) {
            AddCompanyUserPage(navController = navController)
        }

        composable(Screens.AddTenantUser.route) {
            AddTenantUserPage(navController = navController)
        }

        composable(Screens.PromptManage.route) {
            PromptManagePage(navController = navController)
        }

        composable(Screens.AddPrompt.route) {
            AddPromptPage(navController = navController)
        }

        composable(Screens.ModelManage.route) {
            ModelManagePage(navController = navController)
        }

        composable(Screens.AddModel.route) {
            AddModelPage(navController = navController)
        }

        composable(
            route = "${Screens.UpdateModel.route}?modelId={modelId}",
            arguments = listOf(
                navArgument("modelId") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val modelId = backStackEntry.arguments?.getString("modelId") ?: ""
            UpdateModelPage(
                navController = navController,
                modelId = modelId
            )
        }
    }
}