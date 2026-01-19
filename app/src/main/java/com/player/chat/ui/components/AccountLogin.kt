package com.player.chat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import com.player.chat.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountLogin(
    viewModel: LoginViewModel,
    onAccountChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {}
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 监听输入变化
    LaunchedEffect(account) {
        onAccountChange(account)
    }

    LaunchedEffect(password) {
        onPasswordChange(password)
    }


    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.pagePadding)
    ) {
        // 账号输入框
        OutlinedTextField(
            value = account,
            onValueChange = { account = it },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.icon_user),
                    contentDescription = "账号",
                    modifier = Modifier.width(Dimens.smallIconSize).height(Dimens.smallIconSize)
                )
            },
            placeholder = {
                Text(text = "请输入账号")
            },
            shape = RoundedCornerShape(Dimens.bigBorderRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.disableTextColor,
                unfocusedBorderColor = Color.disableTextColor
            ),
            singleLine = true
        )

        // 密码输入框
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.icon_password),
                    contentDescription = "密码",
                    modifier = Modifier.width(Dimens.smallIconSize).height(Dimens.smallIconSize)
                )
            },
            placeholder = {
                Text(text = "请输入密码")
            },
            shape = RoundedCornerShape(Dimens.bigBorderRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.disableTextColor,
                unfocusedBorderColor = Color.disableTextColor
            ),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
    }
}