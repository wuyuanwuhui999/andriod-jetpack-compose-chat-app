package com.player.chat.ui.page

import com.player.chat.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.player.chat.navigation.Screens
import com.player.chat.ui.theme.Color
import com.player.chat.ui.components.AccountLogin
import com.player.chat.ui.components.EmailLogin
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val loginState = viewModel.loginState.collectAsState()
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.pageBackgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.pagePadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo (占位)
            Spacer(modifier = Modifier.height(Dimens.bigMargin))
            Icon(
                painter = painterResource(R.drawable.icon_ai),
                contentDescription = "icon",
                modifier = Modifier.width(Dimens.bigIconSize).height(Dimens.bigIconSize)
            )
            Spacer(modifier = Modifier.height(Dimens.bigMargin))
            // 选项卡
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                contentColor = Color.PrimaryColor,
                indicator = @Composable { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(  // 修改这里：使用 SecondaryIndicator
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = Dimens.borderSize,
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
                0 -> AccountLogin(
                    viewModel = viewModel,
                    onAccountChange = { account = it },
                    onPasswordChange = { password = it }
                )
                1 -> EmailLogin(
                    viewModel = viewModel,
                    onEmailChange = { email = it },
                    onCodeChange = { code = it }
                )
            }

            Spacer(modifier = Modifier.height(Dimens.pagePadding))

            // 登录按钮
            Button(
                onClick = {
                    when (selectedTab) {
                        0 -> {
                            if (account.isNotBlank() && password.isNotBlank()) {
                                viewModel.loginByUserAccount(account, password)
                            }
                        }
                        1 -> {
                            if (email.isNotBlank() && code.isNotBlank()) {
                                viewModel.loginByEmail(email, code)
                            }
                        }
                    }
                },
                enabled = when (selectedTab) {
                    0 -> account.isNotBlank() && password.isNotBlank()
                    1 -> email.isNotBlank() && code.isNotBlank()
                    else -> false
                } && loginState.value !is com.player.chat.viewmodel.LoginState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.btnHeight),
                shape = RoundedCornerShape(Dimens.bigBorderRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.PrimaryColor
                )
            ) {
                when (loginState.value) {
                    is com.player.chat.viewmodel.LoginState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                    else -> {
                        Text(
                            text = "登录",
                            color = Color.White,
                            fontSize = Dimens.fontSizeNormal,
                        )
                    }
                }
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
                shape = RoundedCornerShape(Dimens.bigBorderRadius),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.disableTextColor)

            ) {
                Text(
                    text = "注册",
                    fontSize = Dimens.fontSizeNormal,
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
                shape = RoundedCornerShape(Dimens.bigBorderRadius),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.disableTextColor) // ✅ 正确写法
            ) {
                Text(
                    text = "找回密码",
                    fontSize = Dimens.fontSizeNormal,
                )
            }
        }
    }
}