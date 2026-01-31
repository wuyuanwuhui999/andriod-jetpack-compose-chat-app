package com.player.chat.ui.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.player.chat.R
import com.player.chat.navigation.Screens
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.MainViewModel

@Composable
fun LaunchPage(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    // 监听 token 验证状态
    val tokenValidState by mainViewModel.isTokenValid.collectAsState()
    val tokenValue by mainViewModel.token.collectAsState()

    LaunchedEffect(key1 = tokenValidState) {
        tokenValidState?.let { isValid ->
            if (isValid) {
                navController.navigate(Screens.Chat.route) {
                    popUpTo(Screens.Launch.route) { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.icon_ai),
                contentDescription = "默认头像",
                modifier = Modifier
                    .size(Dimens.bigIconSize),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(Dimens.bigMargin))
            Text(
                text = "欢迎使用",
                fontSize = Dimens.fontSizeBig,
            )
        }
    }

    LaunchedEffect(Unit) {
        // 添加延迟，确保界面先显示
        kotlinx.coroutines.delay(1500)

        // 1. 先检查本地是否有 token
        val hasToken = mainViewModel.hasTokenLocally()

        if (!hasToken) {
            navController.navigate(Screens.Login.route) {
                popUpTo(Screens.Launch.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        // 2. 调用接口验证 token 有效性
        try {
            val isValid = mainViewModel.validateToken()

            if (isValid) {
                // 状态流会在监听到变化后自动跳转
            } else {
                navController.navigate(Screens.Login.route) {
                    popUpTo(Screens.Launch.route) { inclusive = true }
                }
            }
        } catch (e: Exception) {
            // 网络异常等，跳转到登录页
            navController.navigate(Screens.Login.route) {
                popUpTo(Screens.Launch.route) { inclusive = true }
            }
        }
    }
}