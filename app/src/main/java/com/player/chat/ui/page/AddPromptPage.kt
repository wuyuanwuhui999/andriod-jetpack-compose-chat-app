// ui/page/AddPromptPage.kt
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.AddPromptViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 添加提示词页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPromptPage(
    navController: NavHostController,
    viewModel: AddPromptViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var promptText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }

    // 自动获取焦点
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "添加提示词",
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
            // 提示词编辑卡片
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
                ) {
                    // 提示词输入框
                    BasicTextField(
                        value = promptText,
                        onValueChange = {
                            promptText = it
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.textareaHeight)
                            .focusRequester(focusRequester),
                        textStyle = TextStyle.Default.copy(
                            color = Color.Black,
                            fontSize = Dimens.normalFontSize
                        ),
                        cursorBrush = SolidColor(Color.Primary),
                        decorationBox = { innerTextField ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(Dimens.smallGap)
                            ) {
                                if (promptText.isEmpty()) {
                                    Text(
                                        text = "请输入提示词内容...",
                                        color = Color.Secondary,
                                        fontSize = Dimens.normalFontSize,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.Start)
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    innerTextField()
                                }
                            }
                        }
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

                        if (promptText.isBlank()) {
                            errorMessage = "提示词不能为空"
                            return@Button
                        }

                        scope.launch {
                            isLoading = true
                            errorMessage = null

                            val result = viewModel.addPrompt(promptText)

                            isLoading = false

                            if (result.isSuccess) {
                                android.widget.Toast.makeText(
                                    context,
                                    "添加提示词成功",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                navController.navigateUp()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "添加失败，请重试"
                            }
                        }
                    },
                    // 只要不在加载中就可以点击，点击时在内部校验内容
                    enabled = !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.btnHeight),
                    shape = RoundedCornerShape(Dimens.btnHeight / 2),
                    colors = ButtonDefaults.buttonColors(
                        // 容器颜色：有内容时使用 Primary，空时使用 DisableColor
                        containerColor = if (promptText.isNotBlank()) Color.Primary else Color.DisableColor,
                        // 文字颜色：始终为白色
                        contentColor = Color.White,
                        // 禁用状态的容器颜色
                        disabledContainerColor = Color.DisableColor,
                        // 禁用状态的文字颜色
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