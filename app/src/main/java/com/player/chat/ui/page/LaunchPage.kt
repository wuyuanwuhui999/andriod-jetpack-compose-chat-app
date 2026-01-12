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
        // 1. 从 ViewModel 获取当前缓存的 token（StateFlow 的最新值）
        val cachedToken = mainViewModel.token.value

        // 2. 本地校验 token 是否有效（未过期）
        if (!mainViewModel.isTokenValid(cachedToken)) {
            // Token 无效或为空，跳转登录页
            navController.navigate(Screens.Login.route) {
                popUpTo(Screens.Launch.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        // 3. Token 有效，尝试调用 getUserData 接口刷新用户信息
        try {
            val result = mainViewModel.userRepository.getUserData()
            if (result.isSuccess) {
                // getUserData 成功后，UserRepository 已将新 token 和 userData 存入 DataStore
                // MainViewModel 会自动通过 collect 从 DataStore 加载最新数据到 StateFlow
                // 所以无需手动 set，但为了确保立即可用，可主动触发一次加载（可选）
                // 这里我们信任 DataStore -> StateFlow 的自动同步

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