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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.UpdatePromptViewModel
import kotlinx.coroutines.launch

/**
 * 更新提示词页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePromptPage(
    navController: NavHostController,
    promptId: String,
    viewModel: UpdatePromptViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val promptText by viewModel.promptText.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }

    // 加载提示词数据
    LaunchedEffect(promptId) {
        if (promptId.isNotBlank()) {
            viewModel.loadPrompt(promptId)
        }
    }

    // 自动获取焦点
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "更新提示词",
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
            // 提示词编辑卡片 - 占满剩余高度
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.middleGap)
                ) {
                    BasicTextField(
                        value = promptText,
                        onValueChange = {
                            viewModel.updatePromptText(it)
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(focusRequester),
                        textStyle = TextStyle.Default.copy(
                            color = Color.Black,
                            fontSize = Dimens.normalFontSize
                        ),
                        cursorBrush = SolidColor(Color.Primary),
                        decorationBox = { innerTextField ->
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (promptText.isEmpty()) {
                                    Text(
                                        text = "请输入提示词内容...",
                                        color = Color.Gray,
                                        fontSize = Dimens.normalFontSize,
                                        modifier = Modifier.fillMaxWidth()
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

            // 按钮区
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
                    shape = RoundedCornerShape(Dimens.btnHeight / 2),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Secondary
                    )
                ) {
                    Text("取消", fontSize = Dimens.normalFontSize)
                }

                // 确定按钮 - 文本框为空时禁用
                Button(
                    onClick = {
                        focusManager.clearFocus()

                        if (promptText.isBlank()) {
                            errorMessage = "提示词不能为空"
                            return@Button
                        }

                        scope.launch {
                            isSaving = true
                            errorMessage = null

                            val result = viewModel.updatePrompt(promptId, promptText)

                            isSaving = false

                            if (result.isSuccess && (result.getOrNull() ?: 0) > 0) {
                                android.widget.Toast.makeText(
                                    context,
                                    "更新提示词成功",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                navController.navigateUp()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "更新失败，请重试"
                            }
                        }
                    },
                    enabled = promptText.isNotBlank() && !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.btnHeight),
                    shape = RoundedCornerShape(Dimens.btnHeight / 2),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (promptText.isNotBlank()) Color.Primary else Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimens.middleIconSize),
                            color = Color.White,
                            strokeWidth = Dimens.strokeWidth
                        )
                    } else {
                        Text("确定", fontSize = Dimens.normalFontSize)
                    }
                }
            }
        }
    }
}