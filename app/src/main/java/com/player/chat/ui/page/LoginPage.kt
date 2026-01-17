package com.player.chat.ui.page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.player.chat.navigation.Screens
import com.player.chat.ui.theme.Dimens
import com.player.chat.ui.theme.Color
import com.player.chat.ui.components.AccountLogin
import com.player.chat.ui.components.EmailLogin
import com.player.chat.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.pagePadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo (占位)
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "Logo",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.PrimaryColor
            )
            Spacer(modifier = Modifier.height(40.dp))

            // 选项卡
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
                contentColor = Color.PrimaryColor,
                indicator = @Composable { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(  // 修改这里：使用 SecondaryIndicator
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 2.dp,
                        color = Color.PrimaryColor
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "账号密码登录",
                            color = if (selectedTab == 0) Color.PrimaryColor else Color.Gray
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "邮箱验证码登录",
                            color = if (selectedTab == 1) Color.PrimaryColor else Color.Gray
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(Dimens.pagePadding))

            // 登录面板
            when (selectedTab) {
                0 -> AccountLogin()
                1 -> EmailLogin(viewModel = viewModel)
            }

            Spacer(modifier = Modifier.height(Dimens.pagePadding))

            // 登录按钮
            Button(
                onClick = {
                    // 登录逻辑
                    navController.navigate(Screens.Chat.route) {
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.btnHeight),
                shape = RoundedCornerShape(Dimens.btnBorderRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text(
                    text = "登录",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(Dimens.pagePadding))

            // 注册按钮
            OutlinedButton(
                onClick = {
                    // TODO: 注册逻辑
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.btnHeight),
                shape = RoundedCornerShape(Dimens.btnBorderRadius),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.Gray)

            ) {
                Text(
                    text = "注册",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(Dimens.pagePadding))

            // 找回密码按钮
            OutlinedButton(
                onClick = {
                    // TODO: 找回密码逻辑
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.btnHeight),
                shape = RoundedCornerShape(Dimens.btnBorderRadius),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.Gray) // ✅ 正确写法
            ) {
                Text(
                    text = "找回密码",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}