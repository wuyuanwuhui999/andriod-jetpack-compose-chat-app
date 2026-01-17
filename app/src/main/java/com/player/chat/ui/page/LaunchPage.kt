package com.player.chat.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.player.chat.navigation.Screens
import com.player.chat.viewmodel.MainViewModel

// LaunchPage.kt
@Composable
fun LaunchPage(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "欢迎使用",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }

    LaunchedEffect(Unit) {
        // 添加延迟，确保界面先显示
        kotlinx.coroutines.delay(1000)

        // 1. 从 ViewModel 获取当前缓存的 token
        val cachedToken = mainViewModel.token.value

        // 2. 本地校验 token 是否有效
        if (!mainViewModel.isTokenValid(cachedToken)) {
            // Token 无效或为空，跳转登录页
            navController.navigate(Screens.Login.route) {
                popUpTo(Screens.Launch.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        // 3. Token 有效，尝试调用 getUserData 接口刷新用户信息
        try {
            val result = mainViewModel.getUserData()
            if (result.isSuccess) {
                // 导航到主界面
                navController.navigate(Screens.Chat.route) {
                    popUpTo(Screens.Launch.route) { inclusive = true }
                }
            } else {
                // 接口调用失败（如 401 Token 过期），视为无效
                navController.navigate(Screens.Login.route) {
                    popUpTo(Screens.Launch.route) { inclusive = true }
                }
            }
        } catch (e: Exception) {
            // 网络异常等
            navController.navigate(Screens.Login.route) {
                popUpTo(Screens.Launch.route) { inclusive = true }
            }
        }
    }
}