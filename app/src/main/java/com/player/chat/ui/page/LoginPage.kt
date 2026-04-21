package com.player.chat.ui.page

import com.player.chat.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.player.chat.navigation.Screens
import com.player.chat.ui.theme.Color
import com.player.chat.ui.components.AccountLogin
import com.player.chat.ui.components.EmailLogin
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.LoginViewModel
import com.player.chat.viewmodel.LoginState // 直接导入LoginState
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(
    navController: NavHostController,  // 添加 NavController 参数
    viewModel: LoginViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val loginState = viewModel.loginState.collectAsState()
    var account by remember { mutableStateOf("吴时吴刻") }
    var password by remember { mutableStateOf("123456") }
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    // 监听登录成功状态
    LaunchedEffect(key1 = true) {
        viewModel.loginState.collectLatest { state ->
            when (state) {
                is LoginState.Success -> {
                    // 登录成功，跳转到 ChatPage
                    navController.navigate(Screens.Chat.route) {
                        // 清空返回栈，防止返回登录页
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.PageBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.middleGap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo (占位)
            Spacer(modifier = Modifier.height(Dimens.bigGap))
            Icon(
                painter = painterResource(R.drawable.icon_ai),
                contentDescription = "icon",
                modifier = Modifier.width(Dimens.bigIconSize).height(Dimens.bigIconSize)
            )
            Spacer(modifier = Modifier.height(Dimens.bigGap))
            // 选项卡
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                contentColor = Color.Primary,
                indicator = @Composable { tabPositions ->
                    TabRowDefaults.Indicator(  // 修改这里：使用 SecondaryIndicator
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = Dimens.borderSize,
                        color = Color.Primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "账号密码登录",
                            color = if (selectedTab == 0) Color.Primary else Color.Gray
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "邮箱验证码登录",
                            color = if (selectedTab == 1) Color.Primary else Color.subTitleColor
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(Dimens.middleGap))

            // 登录面板
            when (selectedTab) {
                0 -> AccountLogin(
                    onAccountChange = { account = it },
                    onPasswordChange = { password = it }
                )
                1 -> EmailLogin(
                    viewModel = viewModel,
                    onEmailChange = { email = it },
                    onCodeChange = { code = it }
                )
            }

            Spacer(modifier = Modifier.height(Dimens.middleGap))

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
                } && loginState.value !is LoginState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.btnHeight),
                shape = RoundedCornerShape(Dimens.bigBorderRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Primary
                )
            ) {
                when (loginState.value) {
                    is LoginState.Loading -> {
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
                            fontSize = Dimens.normalFontSize,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.middleGap))

            // 注册按钮
            OutlinedButton(
                onClick = {
                    navController.navigate(Screens.Register.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.btnHeight),
                shape = RoundedCornerShape(Dimens.bigBorderRadius),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                border = BorderStroke(Dimens.borderSize, Color.disableTextColor)
            ) {
                Text(
                    text = "注册",
                    fontSize = Dimens.normalFontSize,
                )
            }

            Spacer(modifier = Modifier.height(Dimens.middleGap))

            // 忘记密码按钮
            TextButton(
                onClick = {
                    navController.navigate(Screens.ForgetPassword.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.btnHeight),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.subTitleColor
                )
            ) {
                Text(
                    text = "忘记密码？",
                    fontSize = Dimens.normalFontSize,
                    color = Color.subTitleColor,
                    textDecoration = TextDecoration.Underline                 )
            }
        }
    }
}