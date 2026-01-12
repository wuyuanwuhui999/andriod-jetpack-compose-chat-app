package com.player.chat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.player.chat.ui.theme.Dimens
import com.player.chat.ui.theme.PrimaryColor
import com.player.chat.ui.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailLogin(viewModel: LoginViewModel) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingMedium)
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
                        imageVector = Icons.Default.Send,
                        contentDescription = "发送验证码",
                        tint = PrimaryColor
                    )
                }
            },
            placeholder = {
                Text(text = "请输入邮箱")
            },
            shape = RoundedCornerShape(Dimens.CornerRadiusMedium),
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
            shape = RoundedCornerShape(Dimens.CornerRadiusMedium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}