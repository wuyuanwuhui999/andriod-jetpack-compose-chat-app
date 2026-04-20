// ResetPasswordPage.kt
package com.player.chat.ui.page

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.player.chat.navigation.Screens
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.ResetPasswordViewModel
import kotlinx.coroutines.launch

/**
 * 重置密码页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordPage(
    navController: NavHostController,
    email: String,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val codeFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmFocusRequester = remember { FocusRequester() }

    // 表单状态
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // 加载状态
    var isLoading by remember { mutableStateOf(false) }

    // 验证表单是否有效
    val isFormValid = remember(code, newPassword, confirmPassword) {
        code.isNotBlank() &&
                newPassword.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                newPassword.length in 6..18 &&
                confirmPassword.length in 6..18 &&
                newPassword == confirmPassword
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "重置密码",
                        color = Color.Black,
                        fontSize = Dimens.normalFontSize,
                        fontWeight = FontWeight.Medium
                    )
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                    // 验证码输入框
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.middleGap),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "验证码",
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

                        BasicTextField(
                            value = code,
                            onValueChange = { code = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = Dimens.middleGap)
                                .focusRequester(codeFocusRequester)
                                .background(Color.Transparent),
                            textStyle = LocalTextStyle.current.copy(
                                color = Color.Black,
                                fontSize = Dimens.normalFontSize,
                                textAlign = TextAlign.Left
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (code.isEmpty()) {
                                        Text(
                                            text = "请输入验证码",
                                            color = Color.Gray,
                                            fontSize = Dimens.normalFontSize
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // 新密码输入框
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.middleGap),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "新密码",
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

                        BasicTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = Dimens.middleGap)
                                .focusRequester(passwordFocusRequester)
                                .background(Color.Transparent),
                            textStyle = LocalTextStyle.current.copy(
                                color = Color.Black,
                                fontSize = Dimens.normalFontSize,
                                textAlign = TextAlign.Left
                            ),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (newPassword.isEmpty()) {
                                        Text(
                                            text = "请输入新密码",
                                            color = Color.Gray,
                                            fontSize = Dimens.normalFontSize
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // 确认密码输入框
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.middleGap),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "确认密码",
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

                        BasicTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = Dimens.middleGap)
                                .focusRequester(confirmFocusRequester)
                                .background(Color.Transparent),
                            textStyle = LocalTextStyle.current.copy(
                                color = Color.Black,
                                fontSize = Dimens.normalFontSize,
                                textAlign = TextAlign.Left
                            ),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (confirmPassword.isEmpty()) {
                                        Text(
                                            text = "请再次输入密码",
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

            // 确定按钮
            Button(
                onClick = {
                    focusManager.clearFocus()

                    // 表单验证 - 使用 Toast 提示
                    when {
                        code.isBlank() -> {
                            Toast.makeText(context, "请输入验证码", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        newPassword.isBlank() -> {
                            Toast.makeText(context, "请输入新密码", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        confirmPassword.isBlank() -> {
                            Toast.makeText(context, "请确认密码", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        newPassword.length < 6 || newPassword.length > 18 -> {
                            Toast.makeText(context, "密码长度需为6-18位", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        newPassword != confirmPassword -> {
                            Toast.makeText(context, "两次输入的密码不一致", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                    }

                    scope.launch {
                        isLoading = true

                        val result = viewModel.resetPassword(email, code, newPassword)

                        isLoading = false

                        if (result.isSuccess) {
                            Toast.makeText(
                                context,
                                "密码重置成功",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate(Screens.Chat.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            val errorMsg = result.exceptionOrNull()?.message ?: "重置密码失败，请稍后重试"
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.btnHeight),
                shape = RoundedCornerShape(Dimens.btnHeight / 2),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid && !isLoading) Color.Primary else Color.Gray,
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