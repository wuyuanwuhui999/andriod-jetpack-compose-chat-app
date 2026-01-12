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
import androidx.navigation.NavHostController
import com.player.chat.navigation.Screens
import kotlinx.coroutines.delay

@Composable
fun LaunchPage(navController: NavHostController) {
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
        // 这里添加token验证逻辑
        // 模拟延迟
        delay(2000)

        // 假设验证成功，跳转到Chat页面
        // 实际应该根据token验证结果跳转
        navController.navigate(Screens.Chat.route) {
            popUpTo(Screens.Launch.route) { inclusive = true }
        }
    }
}