package com.player.chat.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.player.chat.R
import com.player.chat.model.ChatModel
import com.player.chat.ui.components.Avatar
import com.player.chat.ui.components.AvatarSize
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.ChatViewModel
import com.player.chat.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPage(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
) {
    val user by mainViewModel.currentUser.collectAsState()
    val modelList by chatViewModel.modelList.collectAsState()
    val selectedModel by chatViewModel.selectedModel.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()
    val isSending by chatViewModel.isSending.collectAsState()
    val showModelDialog by chatViewModel.showModelDialog.collectAsState()
    val showMenuDialog by chatViewModel.showMenuDialog.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 1. 顶部标题栏
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "当前接入模型：${selectedModel?.modelName ?: "无"}",
                            modifier = Modifier.clickable {
                                if (modelList.isNotEmpty()) {
                                    chatViewModel.toggleModelDialog()
                                }
                            }
                        )
                    }
                }
            },
            navigationIcon = {
                Avatar(
                    avatarUrl = user?.avatar,
                    size = AvatarSize.SMALL
                )
            },
            actions = {
                IconButton(
                    onClick = { chatViewModel.toggleMenuDialog() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "菜单",
                        modifier = Modifier.size(Dimens.smallIconSize)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // 2. 中间内容区
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // 这里暂时显示空白，后续开发内容
            if (isLoading && modelList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // 这里暂时为空，后续添加聊天消息
                    item {
                        // 占位文本
                        if (modelList.isEmpty() && !isLoading) {
                            Text(
                                text = "暂无模型可用",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // 3. 底部输入区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.disableTextColor)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 输入框
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .focusRequester(focusRequester),
                    textStyle = TextStyle.Default.copy(color = Color.Black),
                    cursorBrush = SolidColor(Color.PrimaryColor),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "输入消息...",
                                    color = Color.Gray
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // 发送按钮
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isSending) {
                            chatViewModel.sendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isSending,
                    modifier = Modifier.size(40.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = if (inputText.isNotBlank()) Color.PrimaryColor else Color.Gray
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "发送",
                            tint = if (inputText.isNotBlank()) Color.PrimaryColor else Color.Gray
                        )
                    }
                }
            }
        }
    }

    // 模型选择对话框
    if (showModelDialog && modelList.isNotEmpty()) {
        Dialog(onDismissRequest = { chatViewModel.toggleModelDialog() }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "选择模型",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn {
                        items(modelList.size) { index ->
                            val model = modelList[index]
                            ListItem(
                                headlineContent = { Text(model.modelName) },
                                modifier = Modifier.clickable {
                                    chatViewModel.selectModel(model)
                                }
                            )
                            if (index < modelList.size - 1) {
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }

    // 菜单对话框
    if (showMenuDialog) {
        Dialog(onDismissRequest = { chatViewModel.toggleMenuDialog() }) {
            Card(
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column {
                    val menuItems = listOf(
                        "上传文档",
                        "我的文档",
                        "会话记录",
                        "设置提示词",
                        "我的提示词"
                    )

                    menuItems.forEachIndexed { index, item ->
                        ListItem(
                            headlineContent = { Text(item) },
                            modifier = Modifier.clickable {
                                // TODO: 处理菜单点击
                                chatViewModel.toggleMenuDialog()
                            }
                        )
                        if (index < menuItems.size - 1) {
                            Divider()
                        }
                    }
                }
            }
        }
    }

    // 自动获取焦点
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}