package com.player.chat.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.player.chat.R
import com.player.chat.model.Prompt
import com.player.chat.navigation.Screens
import com.player.chat.ui.components.InputDialog
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.PromptManageViewModel
import kotlinx.coroutines.delay

/**
 * 提示词管理页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptManagePage(
    navController: NavHostController,
    viewModel: PromptManageViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val promptList by viewModel.promptList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val searchKeyword by viewModel.searchKeyword.collectAsStateWithLifecycle()
    val currentPromptId by viewModel.currentPromptId.collectAsStateWithLifecycle()
    val operationMessage by viewModel.operationMessage.collectAsStateWithLifecycle()

    // 编辑对话框状态
    var showEditDialog by remember { mutableStateOf(false) }
    var editingPrompt by remember { mutableStateOf<Prompt?>(null) }

    // 删除确认对话框状态
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingPrompt by remember { mutableStateOf<Prompt?>(null) }

    // 操作结果提示
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var isSnackbarSuccess by remember { mutableStateOf(true) }

    // 监听操作消息
    LaunchedEffect(operationMessage) {
        if (operationMessage != null) {
            snackbarMessage = operationMessage!!
            isSnackbarSuccess = operationMessage?.contains("成功") == true
            showSnackbar = true
            delay(2000)
            showSnackbar = false
            viewModel.resetOperationMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "提示词管理",
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
                actions = {
                    // 加号图标 - 跳转到添加提示词页面
                    IconButton(
                        onClick = {
                            navController.navigate(Screens.AddPrompt.route)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加提示词",
                            tint = Color.Black,
                            modifier = Modifier.size(Dimens.middleIconSize)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.PageBackground)
                .padding(paddingValues)
        ) {
            // 搜索框 - 胶囊型
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.middleGap)
                    .padding(top = Dimens.middleGap),
                shape = RoundedCornerShape(Dimens.inputHeight / 2),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.inputHeight)
                        .padding(horizontal = Dimens.middleGap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索",
                        tint = Color.Gray,
                        modifier = Modifier.size(Dimens.smallIconSize)
                    )

                    Spacer(modifier = Modifier.width(Dimens.smallGap))

                    BasicTextField(
                        value = searchKeyword,
                        onValueChange = { viewModel.updateSearchKeyword(it) },
                        modifier = Modifier.weight(1f),
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
                                if (searchKeyword.isEmpty()) {
                                    Text(
                                        text = "请输入关键字搜索提示词",
                                        color = Color.secondary,
                                        fontSize = Dimens.normalFontSize
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    if (searchKeyword.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.updateSearchKeyword("") },
                            modifier = Modifier.size(Dimens.smallIconSize)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "清除",
                                tint = Color.Gray,
                                modifier = Modifier.size(Dimens.smallIconSize)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.middleGap))

            // 提示词列表卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = Dimens.middleGap)
                    .padding(bottom = Dimens.middleGap),
                shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    when {
                        isLoading && promptList.isEmpty() -> {
                            // 加载中状态
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(Dimens.middleGap),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(40.dp),
                                        color = Color.Primary,
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.middleGap))
                                    Text(
                                        text = "加载中...",
                                        color = Color.Gray,
                                        fontSize = Dimens.normalFontSize
                                    )
                                }
                            }
                        }
                        promptList.isEmpty() -> {
                            // 空状态
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(Dimens.middleGap),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.icon_search),
                                        contentDescription = "暂无提示词",
                                        modifier = Modifier.size(60.dp),
                                        tint = Color.Gray.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.middleGap))
                                    Text(
                                        text = if (searchKeyword.isNotBlank()) "未找到相关提示词" else "暂无提示词",
                                        color = Color.Gray,
                                        fontSize = Dimens.normalFontSize
                                    )
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                items(
                                    items = promptList,
                                    key = { it.id }
                                ) { prompt ->
                                    SwipeToDeletePromptItem(
                                        prompt = prompt,
                                        isCurrentUsed = prompt.id == currentPromptId,
                                        onUse = {
                                            viewModel.usePrompt(prompt)
                                        },
                                        onEdit = {
                                            editingPrompt = prompt
                                            showEditDialog = true
                                        },
                                        onDelete = {
                                            deletingPrompt = prompt
                                            showDeleteDialog = true
                                        }
                                    )

                                    // 分隔线
                                    if (promptList.indexOf(prompt) < promptList.size - 1) {
                                        Divider(
                                            color = Color.Gray.copy(alpha = 0.2f),
                                            thickness = Dimens.borderSize,
                                            modifier = Modifier.padding(horizontal = Dimens.middleGap)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 操作结果提示 Snackbar
    if (showSnackbar) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .widthIn(max = 250.dp),
                shape = RoundedCornerShape(20.dp),
                containerColor = if (isSnackbarSuccess) Color.Primary else Color.Red,
                contentColor = Color.White,
                action = null
            ) {
                Text(
                    text = snackbarMessage,
                    color = Color.White,
                    fontSize = Dimens.normalFontSize
                )
            }
        }
    }

    // 编辑提示词对话框
    if (showEditDialog && editingPrompt != null) {
        InputDialog(
            title = "编辑提示词",
            initialValue = editingPrompt?.prompt ?: "",
            hint = "请输入提示词内容",
            isRequired = true,
            onConfirm = { newPrompt ->
                viewModel.updatePrompt(editingPrompt!!.id, newPrompt)
                showEditDialog = false
                editingPrompt = null
            },
            onDismiss = {
                showEditDialog = false
                editingPrompt = null
            }
        )
    }

    // 删除确认对话框
    if (showDeleteDialog && deletingPrompt != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                deletingPrompt = null
            },
            title = { Text("删除提示词") },
            text = { Text("确定要删除提示词「${deletingPrompt?.prompt?.take(20)}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePrompt(deletingPrompt!!.id)
                        showDeleteDialog = false
                        deletingPrompt = null
                    }
                ) {
                    Text("确定", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deletingPrompt = null
                }) {
                    Text("取消")
                }
            }
        )
    }

    // 页面加载时加载提示词列表
    LaunchedEffect(Unit) {
        viewModel.loadPromptList()
    }
}

/**
 * 可滑动删除的提示词条目组件
 * 支持向左滑动显示操作按钮（使用/编辑/删除）
 */
@Composable
fun SwipeToDeletePromptItem(
    prompt: Prompt,
    isCurrentUsed: Boolean,
    onUse: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var showActions by remember { mutableStateOf(false) }

    // 每个按钮宽度
    val buttonWidth = 70.dp
    val actionCount = 3 // 使用、编辑、删除
    val totalWidth = actionCount * buttonWidth.value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(Color.White)
    ) {
        // 操作按钮（右侧）
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(buttonWidth * actionCount),
            horizontalArrangement = Arrangement.End
        ) {
            // 使用按钮 - 如果已经是当前使用的提示词，不显示使用按钮
            if (!isCurrentUsed) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(buttonWidth)
                        .background(Color.Primary)
                        .clickable {
                            onUse()
                            offsetX = 0f
                            showActions = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "使用",
                        color = Color.White,
                        fontSize = Dimens.normalFontSize,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 编辑按钮
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(buttonWidth)
                    .background(Color.secondary)
                    .clickable {
                        onEdit()
                        offsetX = 0f
                        showActions = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "编辑",
                    color = Color.White,
                    fontSize = Dimens.normalFontSize,
                    fontWeight = FontWeight.Medium
                )
            }

            // 删除按钮
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(buttonWidth)
                    .background(Color.Red)
                    .clickable {
                        onDelete()
                        offsetX = 0f
                        showActions = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "删除",
                    color = Color.White,
                    fontSize = Dimens.normalFontSize,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 提示词内容（可滑动）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = offsetX.dp)
                .background(Color.White)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val threshold = buttonWidth.value * 0.3f
                            val actualActionCount = if (isCurrentUsed) actionCount - 1 else actionCount
                            val actualTotalWidth = actualActionCount * buttonWidth.value
                            showActions = offsetX <= -threshold
                            offsetX = if (showActions) -actualTotalWidth else 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val actualActionCount = if (isCurrentUsed) actionCount - 1 else actionCount
                            val actualTotalWidth = actualActionCount * buttonWidth.value
                            val newOffset = offsetX + dragAmount
                            offsetX = newOffset.coerceIn(-actualTotalWidth, 0f)
                        }
                    )
                }
                .clickable {
                    if (showActions) {
                        showActions = false
                        offsetX = 0f
                    }
                }
                .padding(horizontal = Dimens.middleGap, vertical = Dimens.middleGap),
            verticalArrangement = Arrangement.Center
        ) {
            // 提示词内容 - 最多两行
            Text(
                text = prompt.prompt,
                color = Color.Black,
                fontSize = Dimens.normalFontSize,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            // 当前使用中标签
            if (isCurrentUsed) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "当前使用中",
                        color = Color.Primary,
                        fontSize = Dimens.normalFontSize,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}