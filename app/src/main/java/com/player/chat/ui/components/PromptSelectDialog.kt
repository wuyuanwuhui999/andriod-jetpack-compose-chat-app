package com.player.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.player.chat.R
import com.player.chat.model.Prompt
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.ChatViewModel
import kotlinx.coroutines.delay

/**
 * 提示词选择对话框
 * 支持搜索、分页加载、滑动操作（删除、编辑、使用）
 */
@Composable
fun PromptSelectDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onAddPrompt: () -> Unit,
    onEditPrompt: (Prompt) -> Unit
) {
    val promptList by viewModel.promptSelectList.collectAsState()
    val searchKeyword by viewModel.promptSearchKeyword.collectAsState()
    val isLoading by viewModel.isPromptListLoading.collectAsState()
    val isLoadingMore by viewModel.isPromptListLoadingMore.collectAsState()
    val hasMoreData by viewModel.hasMorePrompts.collectAsState()
    val tempSelectedPromptId by viewModel.tempSelectedPromptId.collectAsState()

    val listState = rememberLazyListState()

    // 监听滚动到底部，加载更多
    LaunchedEffect(listState.layoutInfo) {
        val layoutInfo = listState.layoutInfo
        if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.last().index
            val totalItemsCount = layoutInfo.totalItemsCount

            if (lastVisibleItemIndex >= totalItemsCount - 1 &&
                !isLoading && !isLoadingMore && hasMoreData && promptList.isNotEmpty()
            ) {
                viewModel.loadMorePrompts()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
                .fillMaxHeight(fraction = 0.8f)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = Dimens.moduleBorderRadius, topEnd = Dimens.moduleBorderRadius))
        ) {
            // 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.barHeight)
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(Dimens.middleGap))

                // 左侧刷新按钮
                IconButton(
                    onClick = { viewModel.refreshPromptList() },
                    modifier = Modifier.size(Dimens.smallIconSize)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_refresh),
                        contentDescription = "刷新",
                        modifier = Modifier.size(Dimens.smallIconSize)
                    )
                }

                // 标题
                Text(
                    text = "提示词",
                    color = Color.Black,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // 右侧添加按钮
                IconButton(
                    onClick = { onAddPrompt() },
                    modifier = Modifier.size(Dimens.middleIconSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加提示词",
                        tint = Color.Black,
                        modifier = Modifier.size(Dimens.middleIconSize)
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.middleGap))
            }

            // 分隔线
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.borderSize)
                    .background(Color.Gray)
            )

            // 内容区 - 灰色背景
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.PageBackground)
                    .padding(Dimens.middleGap)
            ) {
                // 搜索框
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.inputHeight),
                    shape = RoundedCornerShape(Dimens.inputHeight / 2),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Dimens.middleGap),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索",
                            tint = Color.DisableColor,
                            modifier = Modifier.size(Dimens.smallIconSize)
                        )

                        Spacer(modifier = Modifier.width(Dimens.smallGap))

                        BasicTextField(
                            value = searchKeyword,
                            onValueChange = { viewModel.updatePromptSearchKeyword(it) },
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
                                            color = Color.DisableColor,
                                            fontSize = Dimens.normalFontSize
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        if (searchKeyword.isNotBlank()) {
                            IconButton(
                                onClick = { viewModel.updatePromptSearchKeyword("") },
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

                // 提示词列表卡片 - 高度自适应
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),   // ← 改为高度自适应
                    shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()   // ← 高度自适应
                    ) {
                        when {
                            isLoading && promptList.isEmpty() -> {
                                // 加载中状态 - 高度自适应
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .padding(Dimens.middleGap),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(Dimens.bigIconSize),
                                            color = Color.Primary,
                                            strokeWidth = Dimens.strokeWidth
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
                                // 空状态 - 高度自适应
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .padding(Dimens.middleGap),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.icon_search),
                                            contentDescription = "暂无提示词",
                                            modifier = Modifier.size(Dimens.bigIconSize),
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
                                // 提示词列表 - 高度自适应
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),   // ← 高度自适应
                                    verticalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    items(
                                        items = promptList,
                                        key = { it.id }
                                    ) { prompt ->
                                        SwipeToDeletePromptItem(
                                            prompt = prompt,
                                            isCurrentUsed = prompt.id == tempSelectedPromptId,
                                            onUse = { viewModel.selectPrompt(prompt) },
                                            onCancelUse = { viewModel.deselectPrompt() },
                                            onEdit = { onEditPrompt(prompt) },
                                            onDelete = { viewModel.showDeletePromptDialog(prompt) }
                                        )

                                        // 分隔线
                                        if (promptList.indexOf(prompt) < promptList.size - 1) {
                                            Divider(
                                                color = Color.Gray.copy(alpha = 0.3f),
                                                thickness = Dimens.borderSize
                                            )
                                        }
                                    }

                                    // 加载更多指示器
                                    if (isLoadingMore) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(Dimens.middleGap),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(Dimens.middleIconSize),
                                                    color = Color.Primary,
                                                    strokeWidth = Dimens.strokeWidth
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

            // 底部按钮区
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(Dimens.middleGap),
                horizontalArrangement = Arrangement.spacedBy(Dimens.middleGap)
            ) {
                // 取消按钮
                OutlinedButton(
                    onClick = { onDismiss() },
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

                // 确定按钮 - 必须选择提示词后才能点击
                Button(
                    onClick = { onConfirm() },
                    enabled = tempSelectedPromptId != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.btnHeight),
                    shape = RoundedCornerShape(Dimens.btnHeight / 2),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tempSelectedPromptId != null) Color.Primary else Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text("确定", fontSize = Dimens.normalFontSize)
                }
            }
        }
    }
}

/**
 * 可滑动删除的提示词条目组件
 * 支持向左滑动显示操作按钮（删除、编辑、使用）
 */
@Composable
fun SwipeToDeletePromptItem(
    prompt: Prompt,
    isCurrentUsed: Boolean,
    onUse: () -> Unit,
    onCancelUse: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var showActions by remember { mutableStateOf(false) }

    val buttonWidth = 70.dp
    val actionCount = 3
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

            // 编辑按钮
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(buttonWidth)
                    .background(Color.Secondary)
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

            // 使用/取消使用按钮
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(buttonWidth)
                    .background(if (isCurrentUsed) Color.Gray else Color.Primary)
                    .clickable {
                        if (isCurrentUsed) {
                            onCancelUse()
                        } else {
                            onUse()
                        }
                        offsetX = 0f
                        showActions = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCurrentUsed) "取消使用" else "使用",
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
                            showActions = offsetX <= -threshold
                            offsetX = if (showActions) -totalWidth else 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = offsetX + dragAmount
                            offsetX = newOffset.coerceIn(-totalWidth, 0f)
                        }
                    )
                }
                .clickable {
                    if (showActions) {
                        showActions = false
                        offsetX = 0f
                    }
                }
                .padding(Dimens.middleGap),
            verticalArrangement = Arrangement.Center
        ) {
            // 提示词内容 - 最多三行
            Text(
                text = prompt.prompt,
                color = if (isCurrentUsed) Color.Primary else Color.Black,
                fontSize = Dimens.normalFontSize,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}