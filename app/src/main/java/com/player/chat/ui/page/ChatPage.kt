package com.player.chat.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.player.chat.model.PositionEnum
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
    val chatList by chatViewModel.chatList.collectAsState()
    val currentTenant by chatViewModel.currentTenant.collectAsState()
    val thinkMode by chatViewModel.thinkMode.collectAsState()
    val language by chatViewModel.language.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()
    val isSending by chatViewModel.isSending.collectAsState()
    val isConnecting by chatViewModel.isConnecting.collectAsState()
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
                            modifier = Modifier.size(Dimens.smallIconSize),
                            strokeWidth = Dimens.strokeWidth
                        )
                    } else {
                        Text(
                            text = "${currentTenant?.tenantName ?: "私人空间"} | ${selectedModel?.modelName ?: "无模型"}",
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
                        modifier = Modifier.size(Dimens.middleIconSize)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color.Black,
                actionIconContentColor = Color.Black,
                navigationIconContentColor = Color.Black
            ),
        )

        // 2. 中间聊天内容区
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color.pageBackgroundColor)
                .fillMaxWidth()
        ) {
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
                    reverseLayout = true
                ) {
                    items(chatList.reversed()) { message ->
                        ChatBubble(
                            message = message,
                            userAvatar = user?.avatar,
                            thinkMode = thinkMode && message.position == PositionEnum.LEFT
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // 3. 控制按钮区
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 思考模式按钮
            Button(
                onClick = { chatViewModel.toggleThinkMode() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (thinkMode) Color.PrimaryColor else Color.Gray
                ),
                shape = RoundedCornerShape(Dimens.bigBorderRadius)
            ) {
                Text(
                    text = if (thinkMode) "思考模式 ON" else "思考模式",
                    color = Color.White
                )
            }

            // 语言切换按钮
            Button(
                onClick = { chatViewModel.toggleLanguage() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.PrimaryColor
                ),
                shape = RoundedCornerShape(Dimens.bigBorderRadius)
            ) {
                Text(
                    text = if (language == "zh") "中文" else "English",
                    color = Color.White
                )
            }
        }

        // 4. 底部输入区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = Dimens.pagePadding, vertical = Dimens.pagePadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        chatViewModel.startNewChat()
                    },
                    enabled = !isSending,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "开启新对话",
                    )
                }

                // 输入框
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.pageBackgroundColor, RoundedCornerShape(Dimens.bigBorderRadius))
                        .padding(horizontal = Dimens.pagePadding, vertical = Dimens.pagePadding)
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

    // 连接状态指示
    if (isConnecting) {
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(text = "连接中...")
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

@Composable
fun ChatBubble(
    message: com.player.chat.model.ChatMessage,
    userAvatar: String?,
    thinkMode: Boolean
) {
    when (message.position) {
        PositionEnum.LEFT -> {
            // AI消息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                // AI头像
                Icon(
                    painter = androidx.compose.ui.res.painterResource(R.drawable.icon_ai),
                    contentDescription = "AI",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 8.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 思考内容（如果开启思考模式且存在）
                    if (thinkMode && message.thinkContent != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Gray.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = "思考：${message.thinkContent}",
                                modifier = Modifier.padding(8.dp),
                                color = Color.Gray,
                                fontSize = androidx.compose.ui.unit.sp(12)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // 回复内容
                    Card(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Text(
                            text = message.responseContent,
                            modifier = Modifier.padding(12.dp),
                            color = Color.Black
                        )
                    }
                }
            }
        }
        PositionEnum.RIGHT -> {
            // 用户消息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.PrimaryColor.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(
                            text = message.responseContent,
                            modifier = Modifier.padding(12.dp),
                            color = Color.White
                        )
                    }
                }

                // 用户头像
                Avatar(
                    avatarUrl = userAvatar,
                    size = AvatarSize.SMALL,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}