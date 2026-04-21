// RegisterPage.kt
package com.player.chat.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.player.chat.ui.components.DatePickerDialog
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.RegisterViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 注册页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPage(
    navController: NavHostController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // 表单状态
    var userAccount by remember { mutableStateOf("") }      // 账号
    var username by remember { mutableStateOf("") }         // 用户名（必填）
    var password by remember { mutableStateOf("") }         // 密码
    var confirmPassword by remember { mutableStateOf("") }  // 确认密码
    var telephone by remember { mutableStateOf("") }        // 电话
    var email by remember { mutableStateOf("") }            // 邮箱
    var sex by remember { mutableStateOf(0) }               // 性别 0-男 1-女
    var birthday by remember { mutableStateOf("") }         // 出生日期
    var region by remember { mutableStateOf("") }           // 地区
    var sign by remember { mutableStateOf("") }             // 个性签名

    // 校验状态
    var isAccountValid by remember { mutableStateOf(true) }
    var isEmailValid by remember { mutableStateOf(true) }
    var isAccountChecking by remember { mutableStateOf(false) }
    var isEmailChecking by remember { mutableStateOf(false) }

    // 延迟校验的协程
    var accountCheckJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var emailCheckJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // 焦点请求器
    val focusRequesters = listOf(
        remember { FocusRequester() },
        remember { FocusRequester() },
        remember { FocusRequester() },
        remember { FocusRequester() },
        remember { FocusRequester() },
        remember { FocusRequester() },
        remember { FocusRequester() },
        remember { FocusRequester() },
        remember { FocusRequester() }
    )

    // 加载状态
    var isLoading by remember { mutableStateOf(false) }

    // 日期选择器状态
    var showDatePicker by remember { mutableStateOf(false) }

    // 账号校验（延迟1秒）
    LaunchedEffect(userAccount) {
        if (userAccount.isNotBlank()) {
            accountCheckJob?.cancel()
            accountCheckJob = scope.launch {
                delay(1000)
                isAccountChecking = true
                val result = viewModel.checkUserExists(userAccount = userAccount, email = null)
                isAccountChecking = false
                isAccountValid = !result
                if (!isAccountValid) {
                    android.widget.Toast.makeText(context, "账号已存在", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            isAccountValid = true
        }
    }

    // 邮箱校验（延迟1秒，且邮箱格式正确时）
    LaunchedEffect(email) {
        val isEmailFormatValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        if (email.isNotBlank() && isEmailFormatValid) {
            emailCheckJob?.cancel()
            emailCheckJob = scope.launch {
                delay(1000)
                isEmailChecking = true
                val result = viewModel.checkUserExists(userAccount = null, email = email)
                isEmailChecking = false
                isEmailValid = !result
                if (!isEmailValid) {
                    android.widget.Toast.makeText(context, "邮箱已被注册", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            isEmailValid = true
        }
    }

    // 验证表单是否有效（必填项：账号、用户名、密码、确认密码、邮箱）
    val isFormValid = remember(
        userAccount, username, password, confirmPassword, email,
        isAccountValid, isEmailValid
    ) {
        userAccount.isNotBlank() &&
                username.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                email.isNotBlank() &&
                isAccountValid &&
                isEmailValid &&
                password.length in 6..18 &&
                confirmPassword.length in 6..18 &&
                password == confirmPassword &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // 性别选项
    val genderOptions = listOf("男", "女")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "注册",
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
            // 内容卡片 - 可滚动，去掉 weight
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.middleGap)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // 账号（必填）
                    RegisterInputRow(
                        label = "账号",
                        value = userAccount,
                        onValueChange = { userAccount = it },
                        isRequired = true,
                        placeholder = "请输入账号",
                        focusRequester = focusRequesters[0],
                        isLoading = isAccountChecking,
                        isValid = isAccountValid
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // 用户名（必填）
                    RegisterInputRow(
                        label = "用户名",
                        value = username,
                        onValueChange = { username = it },
                        isRequired = true,
                        placeholder = "请输入用户名",
                        focusRequester = focusRequesters[1]
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // 密码（必填）
                    RegisterPasswordRow(
                        label = "密码",
                        value = password,
                        onValueChange = { password = it },
                        isRequired = true,
                        placeholder = "请输入密码（6-18位）",
                        focusRequester = focusRequesters[2]
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // 确认密码（必填）
                    RegisterPasswordRow(
                        label = "确认密码",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        isRequired = true,
                        placeholder = "请再次输入密码",
                        focusRequester = focusRequesters[3],
                        isError = confirmPassword.isNotBlank() && password != confirmPassword
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // 电话
                    RegisterInputRow(
                        label = "电话",
                        value = telephone,
                        onValueChange = { telephone = it },
                        isRequired = false,
                        placeholder = "请输入电话号码",
                        focusRequester = focusRequesters[4],
                        keyboardType = KeyboardType.Phone
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // 邮箱（必填）
                    RegisterInputRow(
                        label = "邮箱",
                        value = email,
                        onValueChange = { email = it },
                        isRequired = true,
                        placeholder = "请输入邮箱",
                        focusRequester = focusRequesters[5],
                        keyboardType = KeyboardType.Email,
                        isLoading = isEmailChecking,
                        isValid = isEmailValid,
                        isError = email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // 性别
                    RegisterGenderRow(
                        label = "性别",
                        selectedValue = sex,
                        options = genderOptions,
                        onValueChange = { sex = if (it == "男") 0 else 1 }
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // 出生日期
                    RegisterDateRow(
                        label = "出生日期",
                        value = birthday,
                        placeholder = "请选择出生日期",
                        onClick = { showDatePicker = true }
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // 地区
                    RegisterInputRow(
                        label = "地区",
                        value = region,
                        onValueChange = { region = it },
                        isRequired = false,
                        placeholder = "请输入地区",
                        focusRequester = focusRequesters[6]
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // 个性签名
                    RegisterInputRow(
                        label = "个性签名",
                        value = sign,
                        onValueChange = { sign = it },
                        isRequired = false,
                        placeholder = "请输入个性签名",
                        focusRequester = focusRequesters[7]
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.middleGap))

            // 确定按钮
            Button(
                onClick = {
                    focusManager.clearFocus()

                    // 表单验证
                    when {
                        userAccount.isBlank() -> {
                            android.widget.Toast.makeText(context, "请输入账号", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        username.isBlank() -> {
                            android.widget.Toast.makeText(context, "请输入用户名", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        password.isBlank() -> {
                            android.widget.Toast.makeText(context, "请输入密码", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        confirmPassword.isBlank() -> {
                            android.widget.Toast.makeText(context, "请确认密码", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        email.isBlank() -> {
                            android.widget.Toast.makeText(context, "请输入邮箱", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        password.length < 6 || password.length > 18 -> {
                            android.widget.Toast.makeText(context, "密码长度需为6-18位", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        password != confirmPassword -> {
                            android.widget.Toast.makeText(context, "两次输入的密码不一致", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            android.widget.Toast.makeText(context, "请输入正确的邮箱格式", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        !isAccountValid -> {
                            android.widget.Toast.makeText(context, "账号已存在", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        !isEmailValid -> {
                            android.widget.Toast.makeText(context, "邮箱已被注册", android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                    }

                    scope.launch {
                        isLoading = true

                        val result = viewModel.register(
                            userAccount = userAccount,
                            username = username,
                            password = password,
                            telephone = telephone,
                            email = email,
                            sex = sex,
                            birthday = birthday,
                            region = region,
                            sign = sign
                        )

                        isLoading = false

                        if (result.isSuccess) {
                            android.widget.Toast.makeText(
                                context,
                                "注册成功",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate(Screens.Chat.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            val errorMsg = result.exceptionOrNull()?.message ?: "注册失败，请稍后重试"
                            android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_SHORT).show()
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
                        text = "确定",
                        fontSize = Dimens.normalFontSize
                    )
                }
            }
        }
    }

    // 日期选择器
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                birthday = formattedDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            initialDate = birthday
        )
    }
}

/**
 * 普通输入框组件
 * 修改说明：修复 placeholder 显示问题，输入框占据剩余宽度，左侧标签固定宽度
 */
@Composable
fun RegisterInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isRequired: Boolean,
    placeholder: String,
    focusRequester: FocusRequester,
    keyboardType: KeyboardType = KeyboardType.Text,
    isLoading: Boolean = false,
    isValid: Boolean = true,
    isError: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.middleGap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧标签 - 固定宽度，保证对齐
        Row(
            modifier = Modifier.width(70.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

        Spacer(modifier = Modifier.width(Dimens.smallGap))

        // 输入框区域 - 占据剩余宽度
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 输入框容器 - 占据剩余空间，可点击获取焦点
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable { focusRequester.requestFocus() }
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .background(Color.Transparent),
                    textStyle = LocalTextStyle.current.copy(
                        color = when {
                            isError -> Color.Red
                            !isValid -> Color.Red
                            else -> Color.Black
                        },
                        fontSize = Dimens.normalFontSize
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        // 关键修复：使用 Box 并正确处理内容对齐
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            // 当输入框为空时显示 placeholder
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = Color.Gray,
                                    fontSize = Dimens.normalFontSize
                                )
                            }
                            // 始终显示输入框内容（透明背景）
                            innerTextField()
                        }
                    }
                )
            }

            // 加载指示器（仅在需要时显示）
            if (isLoading) {
                Spacer(modifier = Modifier.width(Dimens.smallGap))
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 1.5.dp,
                    color = Color.Primary
                )
            }
        }
    }
}

/**
 * 密码输入框组件
 * 修改说明：修复 placeholder 显示问题，输入框占据剩余宽度
 */
@Composable
fun RegisterPasswordRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isRequired: Boolean,
    placeholder: String,
    focusRequester: FocusRequester,
    isError: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.middleGap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧标签 - 固定宽度
        Row(
            modifier = Modifier.width(70.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

        Spacer(modifier = Modifier.width(Dimens.smallGap))

        // 输入框区域 - 占据剩余宽度
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable { focusRequester.requestFocus() }
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .background(Color.Transparent),
                textStyle = LocalTextStyle.current.copy(
                    color = if (isError) Color.Red else Color.Black,
                    fontSize = Dimens.normalFontSize
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // 当输入框为空时显示 placeholder
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = Color.Gray,
                                fontSize = Dimens.normalFontSize
                            )
                        }
                        // 始终显示输入框内容
                        innerTextField()
                    }
                }
            )
        }
    }
}

/**
 * 性别选择组件
 * 修改说明：调整布局，标签固定宽度
 */
@Composable
fun RegisterGenderRow(
    label: String,
    selectedValue: Int,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.middleGap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧标签 - 固定宽度
        Text(
            modifier = Modifier.width(70.dp),
            text = label,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            fontSize = Dimens.normalFontSize
        )

        Spacer(modifier = Modifier.width(Dimens.smallGap))

        // 右侧选项区域
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Dimens.middleGap)
        ) {
            options.forEach { gender ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        onValueChange(gender)
                    }
                ) {
                    RadioButton(
                        selected = when (selectedValue) {
                            0 -> gender == "男"
                            1 -> gender == "女"
                            else -> false
                        },
                        onClick = { onValueChange(gender) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.Primary
                        )
                    )
                    Text(
                        text = gender,
                        color = Color.Black,
                        fontSize = Dimens.normalFontSize,
                        modifier = Modifier.padding(start = Dimens.smallGap)
                    )
                }
            }
        }
    }
}

/**
 * 日期选择组件
 * 修改说明：调整布局，标签固定宽度
 */
@Composable
fun RegisterDateRow(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = Dimens.middleGap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧标签 - 固定宽度
        Text(
            modifier = Modifier.width(70.dp),
            text = label,
            color = Color.Black,
            fontWeight = FontWeight.Medium,
            fontSize = Dimens.normalFontSize
        )

        Spacer(modifier = Modifier.width(Dimens.smallGap))

        // 右侧值区域 - 占据剩余宽度
        Text(
            modifier = Modifier.weight(1f),
            text = if (value.isBlank()) placeholder else value,
            color = if (value.isBlank()) Color.Gray else Color.Black,
            fontSize = Dimens.normalFontSize
        )
    }
}