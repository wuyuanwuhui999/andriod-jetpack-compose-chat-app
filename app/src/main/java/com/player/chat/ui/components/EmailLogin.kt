package com.player.chat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import com.player.chat.R
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
                    painter = painterResource(R.drawable.icon_password),
                    contentDescription = "邮箱",
                    modifier = Modifier.width(Dimens.smallIconSize).height(Dimens.smallIconSize)
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
                        painter = painterResource(R.drawable.icon_send),
                        contentDescription = "发送验证码",
                        modifier = Modifier.width(Dimens.smallIconSize).height(Dimens.smallIconSize)
                    )
                }
            },
            placeholder = {
                Text(text = "请输入邮箱")
            },
            shape = RoundedCornerShape(Dimens.bigBorderRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.disableTextColor,
                unfocusedBorderColor = Color.disableTextColor
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
            shape = RoundedCornerShape(Dimens.bigBorderRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.disableTextColor,
                unfocusedBorderColor = Color.disableTextColor
            ),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.icon_code),
                    contentDescription = "邮箱",
                    modifier = Modifier.width(Dimens.smallIconSize).height(Dimens.smallIconSize)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}