package com.player.chat.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.player.chat.model.AddModelRequest
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.ModelManageViewModel
import kotlinx.coroutines.launch

/**
 * 添加模型页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddModelPage(
    navController: NavHostController,
    viewModel: ModelManageViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // 表单状态
    var modelName by remember { mutableStateOf("") }
    var modelType by remember { mutableStateOf("ollama") } // ollama 或 online
    var baseUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }

    // 自动获取焦点
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // 验证表单是否有效（必填项：模型名称、模型类型、模型地址）
    val isFormValid = remember(modelName, modelType, baseUrl) {
        modelName.isNotBlank() &&
                modelType.isNotBlank() &&
                baseUrl.isNotBlank()
    }

    // 模型类型选项
    val typeOptions = listOf("ollama", "online")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "添加模型",
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
            // 表单卡片
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
                        .padding(Dimens.middleGap),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // 模型名称（必填）
                    ModelFormRow(
                        label = "模型名称",
                        value = modelName,
                        onValueChange = { modelName = it },
                        isRequired = true,
                        placeholder = "请输入模型名称",
                        focusRequester = focusRequester
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // 模型类型（必填）- 使用下拉选择
                    ModelTypeRow(
                        label = "模型类型",
                        value = modelType,
                        options = typeOptions,
                        onValueChange = { modelType = it },
                        isRequired = true
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // 模型地址（必填）
                    ModelFormRow(
                        label = "模型地址",
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        isRequired = true,
                        placeholder = "请输入模型地址",
                        focusRequester = FocusRequester()
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.2f))

                    // API Key（选填）
                    ModelFormRow(
                        label = "API Key",
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        isRequired = false,
                        placeholder = "请输入API Key（选填）",
                        focusRequester = FocusRequester()
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

            // 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.middleGap)
            ) {
                // 取消按钮
                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        navController.navigateUp()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.btnHeight),
                    shape = RoundedCornerShape(Dimens.btnHeight / 2)
                ) {
                    Text("取消", color = Color.Secondary)
                }

                // 确定按钮
                Button(
                    onClick = {
                        focusManager.clearFocus()

                        val companyId = viewModel.getCompanyId()
                        if (companyId.isNullOrBlank()) {
                            errorMessage = "未找到公司信息"
                            return@Button
                        }

                        scope.launch {
                            isLoading = true
                            errorMessage = null

                            val companyId = viewModel.getCompanyId()
                            if (companyId.isNullOrBlank()) {
                                errorMessage = "未找到公司信息"
                                isLoading = false
                                return@launch
                            }

                            val request = AddModelRequest(
                                modelName = modelName,
                                type = modelType,
                                companyId = companyId,
                                apiKey = apiKey.takeIf { it.isNotBlank() },
                                baseUrl = baseUrl
                            )

                            val result = viewModel.addModel(request)
                            isLoading = false

                            if (result.isSuccess && (result.getOrNull() ?: 0) > 0) {
                                android.widget.Toast.makeText(
                                    context,
                                    "添加模型成功",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                navController.navigateUp()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "添加模型失败"
                            }
                        }
                    },
                    enabled = isFormValid && !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.btnHeight),
                    shape = RoundedCornerShape(Dimens.btnHeight / 2),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormValid && !isLoading) Color.Primary else Color.DisableColor,
                        contentColor = Color.White,
                        disabledContainerColor = Color.DisableColor,
                        disabledContentColor = Color.White
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
}

/**
 * 模型表单行组件
 */
@Composable
fun ModelFormRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isRequired: Boolean,
    placeholder: String,
    focusRequester: FocusRequester
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.middleGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
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

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .padding(start = Dimens.middleGap)
                .focusRequester(focusRequester)
                .background(Color.Transparent),
            textStyle = LocalTextStyle.current.copy(
                color = Color.Black,
                fontSize = Dimens.normalFontSize
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
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

/**
 * 模型类型选择行
 */
@Composable
fun ModelTypeRow(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    isRequired: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.middleGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
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

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = Dimens.middleGap)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                shape = RoundedCornerShape(4.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (value.isNotBlank()) value else "请选择类型",
                        color = if (value.isNotBlank()) Color.Black else Color.Gray,
                        fontSize = Dimens.normalFontSize
                    )
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(
                            if (expanded) com.player.chat.R.drawable.icon_down else com.player.chat.R.drawable.icon_arrow
                        ),
                        contentDescription = if (expanded) "收起" else "展开",
                        tint = Color.Gray,
                        modifier = Modifier.size(Dimens.smallIconSize)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                color = Color.Black,
                                fontSize = Dimens.normalFontSize
                            )
                        },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}