// UpdatePasswordPage.kt
package com.player.chat.ui.page

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.UpdatePasswordViewModel
import kotlinx.coroutines.launch

/**
 * 修改密码页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePasswordPage(
    navController: NavHostController,
    viewModel: UpdatePasswordViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 表单状态
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // 加载状态
    var isLoading by remember { mutableStateOf(false) }

    // 错误信息
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 验证表单是否有效
    val isFormValid = remember(oldPassword, newPassword, confirmPassword) {
        oldPassword.isNotBlank() &&
                newPassword.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                newPassword.length in 6..18 &&
                confirmPassword.length in 6..18 &&
                newPassword == confirmPassword
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
                            text = "修改密码",
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
                .padding(Dimens.middleGap),
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
                    // 旧密码输入框
                    PasswordInputField(
                        label = "旧密码",
                        value = oldPassword,
                        onValueChange = {
                            oldPassword = it
                            errorMessage = null
                        },
                        isRequired = true
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // 新密码输入框
                    PasswordInputField(
                        label = "新密码",
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            errorMessage = null
                        },
                        isRequired = true,
                        hint = "密码长度6-18位"
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // 确认密码输入框
                    PasswordInputField(
                        label = "确认密码",
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        isRequired = true,
                        isError = confirmPassword.isNotBlank() && newPassword != confirmPassword,
                        errorText = "两次输入的密码不一致"
                    )
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

            // 确认按钮
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        val result = viewModel.updatePassword(
                            oldPassword = oldPassword,
                            newPassword = newPassword
                        )

                        isLoading = false

                        if (result) {
                            // 修改成功，返回上一页
                            android.widget.Toast.makeText(
                                context,
                                "密码修改成功",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            navController.navigateUp()
                        } else {
                            errorMessage = viewModel.errorMessage.value ?: "密码修改失败，请检查旧密码是否正确"
                        }
                    }
                },
                enabled = isFormValid && !isLoading,
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
                        text = "确认",
                        fontSize = Dimens.normalFontSize
                    )
                }
            }
        }
    }
}

/**
 * 密码输入框组件（无显示/隐藏切换按钮）
 */
@Composable
fun PasswordInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isRequired: Boolean = true,
    hint: String? = null,
    isError: Boolean = false,
    errorText: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
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
                    text = label,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                    fontSize = Dimens.normalFontSize
                )
                if (isRequired) {
                    Text(
                        text = " *",
                        color = Color.Red,
                        fontSize = Dimens.normalFontSize
                    )
                }
            }

            // 右侧密码输入框（无切换按钮）
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .width(180.dp)
                    .background(Color.Transparent),
                textStyle = LocalTextStyle.current.copy(
                    color = Color.Black,
                    fontSize = Dimens.normalFontSize,
                    textAlign = TextAlign.End
                ),
                visualTransformation = PasswordVisualTransformation(),  // 始终使用密码掩码
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = hint ?: "请输入${label}",
                                color = Color.Gray,
                                fontSize = Dimens.normalFontSize
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        // 错误提示
        if (isError && errorText != null) {
            Text(
                text = errorText,
                color = Color.Red,
                fontSize = Dimens.normalFontSize,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.smallGap),
                textAlign = TextAlign.End
            )
        }
    }
}