// ForgetPasswordPage.kt
package com.player.chat.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.player.chat.navigation.Screens
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.ForgetPasswordViewModel
import kotlinx.coroutines.launch

/**
 * 忘记密码页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgetPasswordPage(
    navController: NavHostController,
    viewModel: ForgetPasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // 邮箱输入
    var email by remember { mutableStateOf("") }

    // 焦点状态
    var isFocused by remember { mutableStateOf(false) }

    // 加载状态
    var isLoading by remember { mutableStateOf(false) }

    // 错误信息
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 自动获取焦点
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // 验证邮箱是否有效
    val isEmailValid = remember(email) {
        email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "忘记密码",
                            color = Color.Black,
                            fontSize = Dimens.normalFontSize,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.Black,
                            modifier = Modifier.size(Dimens.middleIconSize)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.PageBackground)
                .padding(Dimens.middleGap)
                .clickable { focusManager.clearFocus() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 内容卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.middleGap),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // 邮箱输入框 - 无边框，文字左对齐
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.middleGap),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 左侧标签
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "邮箱",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                fontSize = Dimens.normalFontSize
                            )
                            Text(
                                text = " *",
                                color = Color.Red,
                                fontSize = Dimens.normalFontSize
                            )
                        }
                        Spacer(modifier = Modifier.width(Dimens.middleGap))
                        // 右侧邮箱输入框 - 无边框，文字左对齐
                        BasicTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = null
                            },
                            modifier = Modifier
                                .weight(1f)  // 占满剩余空间
                                .focusRequester(focusRequester)
                                .onFocusChanged { isFocused = it.isFocused }
                                .background(Color.Transparent),
                            textStyle = LocalTextStyle.current.copy(
                                color = Color.Black,
                                fontSize = Dimens.normalFontSize,
                                textAlign = TextAlign.Left  // 左对齐
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart  // 左对齐
                                ) {
                                    if (email.isEmpty()) {
                                        Text(
                                            text = "请输入邮箱",
                                            color = Color.Gray,
                                            fontSize = Dimens.normalFontSize
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.middleGap))

            // 错误提示
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color.Red,
                    fontSize = Dimens.normalFontSize,
                    modifier = Modifier.padding(vertical = Dimens.smallGap)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.middleGap))

            // 确定按钮
            Button(
                onClick = {
                    focusManager.clearFocus()
                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        val result = viewModel.sendVerificationCode(email)

                        isLoading = false

                        if (result.isSuccess) {
                            val msg = result.getOrNull() ?: "验证码已发送"
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                            // 跳转到重置密码页面，传递邮箱
                            navController.navigate("${Screens.ResetPassword.route}?email=${email}") {
                                popUpTo(Screens.ForgetPassword.route) { inclusive = true }
                            }
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "发送失败，请稍后重试"
                        }
                    }
                },
                enabled = isEmailValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.btnHeight),
                shape = RoundedCornerShape(Dimens.btnHeight / 2),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEmailValid && !isLoading) Color.Primary else Color.Gray,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "确定",
                        fontSize = Dimens.normalFontSize
                    )
                }
            }
        }
    }
}