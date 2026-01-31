package com.player.chat.ui.page

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.player.chat.R
import com.player.chat.model.PositionEnum
import com.player.chat.model.TenantUser
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

    // 添加租户列表状态
    val tenantList by chatViewModel.tenantList.collectAsState()
    val showTenantDialog by chatViewModel.showTenantDialog.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize().background(Color.pageBackgroundColor)
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
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 租户名称 - 可点击
                            Text(
                                text = currentTenant?.name ?: "私人空间",
                                modifier = Modifier
                                    .clickable {
                                        // 点击租户名称，弹出租户选择
                                        chatViewModel.toggleTenantDialog()
                                    }
                                    .padding(end = 4.dp),
                            )

                            // 分隔符
                            Text(
                                text = " | ",
                                modifier = Modifier.padding(horizontal = 4.dp),
                                color = Color.Gray
                            )

                            // 模型名称 - 可点击
                            Text(
                                text = selectedModel?.modelName ?: "无模型",
                                modifier = Modifier
                                    .clickable {
                                        // 点击模型名称，弹出模型选择
                                        if (modelList.isNotEmpty()) {
                                            chatViewModel.toggleModelDialog()
                                        }
                                    },
                            )
                        }
                    }
                }
            },
            navigationIcon = {
                Box(
                    modifier = Modifier.padding(horizontal = Dimens.pagePadding)
                ) {
                    Avatar(
                        avatarUrl = user?.avatar,
                        size = AvatarSize.SMALL
                    )
                }

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
                    contentPadding = PaddingValues(Dimens.pagePadding),
                ) {
                    items(chatList) { message ->
                        ChatBubble(
                            message = message,
                            userAvatar = user?.avatar,
                            thinkMode = thinkMode && message.position == PositionEnum.LEFT
                        )
                    }
                }
            }
        }

        // 3. 控制按钮区
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.pagePadding, vertical = Dimens.pagePadding)
                .background(Color.pageBackgroundColor),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 思考模式按钮
            OutlinedButton(
                onClick = { chatViewModel.toggleThinkMode() },
                shape = RoundedCornerShape(Dimens.bigBorderRadius),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (thinkMode) Color.PrimaryColor else Color.Gray,
                    containerColor = Color.Transparent
                ),
                border = BorderStroke(
                    width = Dimens.borderSize,
                    color = if (thinkMode) Color.PrimaryColor else Color.Gray
                )
            ) {
                Text(
                    text = "思考模式",
                    color = if (thinkMode) Color.PrimaryColor else Color.Gray
                )
            }

            // 语言切换按钮
            Button(
                onClick = { chatViewModel.toggleLanguage() },
                shape = RoundedCornerShape(Dimens.bigBorderRadius),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Gray,
                    containerColor = Color.Transparent
                ),
                border = BorderStroke(
                    width = Dimens.borderSize,
                    color = Color.Gray
                )
            ) {
                Text(
                    text = if (language == "zh") "中文" else "English",
                    color = Color.Gray
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
                    modifier = Modifier.size(Dimens.middleIconSize)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_add),
                        modifier = Modifier.width(Dimens.smallIconSize).height(Dimens.smallIconSize),
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
                            tint = if (inputText.isNotBlank()) Color.PrimaryColor else Color.Gray,
                            modifier = Modifier.rotate(-30f)
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

    // 租户选择对话框
    if (showTenantDialog) {
        Dialog(onDismissRequest = { chatViewModel.toggleTenantDialog() }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.moduleBorderRadius)
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.pagePadding)
                ) {
                    Text(
                        text = "选择租户",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = Dimens.pagePadding)
                    )

                    LazyColumn {
                        items(tenantList.size) { index ->
                            val tenant = tenantList[index]
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = tenant.name, // 添加空值检查
                                        color = if (tenant.id == currentTenant?.id)
                                            Color.PrimaryColor
                                        else
                                            Color.Black
                                    )
                                },
                                modifier = Modifier.clickable {
                                    chatViewModel.selectTenant(tenant)
                                }
                            )
                            if (index < tenantList.size - 1) {
                                Divider()
                            }
                        }
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
                                headlineContent = {
                                    Text(
                                        model.modelName,
                                        color = if (model.id == selectedModel?.id)
                                            Color.PrimaryColor
                                        else
                                            Color.Black
                                    )
                                },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.pagePadding),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start
            ) {
                // AI头像容器 - 相对定位
                Box(
                    modifier = Modifier.align(Alignment.Top)
                ) {
                    // AI头像
                    Icon(
                        painter = painterResource(R.drawable.icon_ai),
                        contentDescription = "AI",
                        modifier = Modifier
                            .size(Dimens.smallAvater)
                    )

                    // 三角形箭头 - 固定在头像中间
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)  // 对齐到头像容器的中间右侧
                            .offset(x = (Dimens.middleIconSize), y = 2.dp)  // 偏移到头像右侧
                            .size(Dimens.smallIconSize)
                            .rotate(-45f)
                            .background(Color.White)
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.pagePadding))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 思考内容（如果开启思考模式且存在）
                    if (thinkMode && message.thinkContent != null) {
                        // 思考内容容器 - 使用自适应宽度
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color.Gray.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(Dimens.btnBorderRadius)
                                    )
                                    .padding(Dimens.pagePadding),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "思考：${message.thinkContent}",
                                    color = Color.Gray,
                                    fontSize = Dimens.fontSizeNormal
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(Dimens.pagePadding))
                    }

                    // AI回复内容 - 使用自适应宽度
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()  // 内容自适应
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(Dimens.btnBorderRadius)
                                )
                                .padding(Dimens.pagePadding),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = message.responseContent,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        PositionEnum.RIGHT -> {
            // 用户消息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimens.pagePadding),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.End
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    // 用户消息容器 - 使用自适应宽度
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .wrapContentWidth()  // 内容自适应
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(Dimens.btnBorderRadius)
                                )
                                .padding(Dimens.pagePadding),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = message.responseContent,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(Dimens.pagePadding))
                // 用户头像容器 - 相对定位
                Box(
                    modifier = Modifier.align(Alignment.Top)
                ) {
                    // 三角形箭头 - 固定在头像中间左侧
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)  // 对齐到头像容器的中间左侧
                            .offset(x = (-Dimens.middleIconSize), y = 2.dp)  // 偏移到头像左侧
                            .size(Dimens.smallIconSize)
                            .rotate(45f)
                            .background(Color.White)
                    )
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
}