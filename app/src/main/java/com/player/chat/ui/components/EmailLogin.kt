package com.player.chat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send  // 改为 AutoMirrored 版本
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.player.chat.ui.theme.Dimens
import com.player.chat.ui.theme.Color
import com.player.chat.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailLogin(viewModel: LoginViewModel) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.pagePadding)
    ) {
        // 邮箱输入框
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "邮箱"
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        // 发送验证码逻辑
                        viewModel.sendEmailCode(email)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,  // 修改这里
                        contentDescription = "发送验证码",
                        tint = Color.PrimaryColor
                    )
                }
            },
            placeholder = {
                Text(text = "请输入邮箱")
            },
            shape = RoundedCornerShape(Dimens.btnBorderRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        // 验证码输入框
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "请输入验证码")
            },
            shape = RoundedCornerShape(Dimens.btnBorderRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}