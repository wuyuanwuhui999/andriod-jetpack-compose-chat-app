// ChatHistoryDialog.kt
package com.player.chat.ui.components

import com.player.chat.ui.theme.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.player.chat.model.ChatHistory
import com.player.chat.model.ChatMessage
import com.player.chat.model.PositionEnum
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.ChatViewModel
import com.player.chat.utils.CommonUtils.formatRelativeTime
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle

@Composable
fun ChatHistoryDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    val chatHistoryList by viewModel.chatHistoryList.collectAsState()
    val groupedHistory by viewModel.groupedChatHistory.collectAsState()
    val isLoading by viewModel.isChatHistoryLoading.collectAsState()
    val hasMoreData by viewModel.hasMoreChatHistory.collectAsState()
    val currentPage by viewModel.currentChatHistoryPage.collectAsState()

    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 监听滚动到底部
    LaunchedEffect(lazyListState.layoutInfo) {
        if (lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ==
            lazyListState.layoutInfo.totalItemsCount - 1 &&
            !isLoading && hasMoreData) {
            viewModel.loadMoreChatHistory()
        }
    }

    CustomBottomDialog(
        title = "会话记录",
        onDismiss = onDismiss,
        leftIconRes = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.pagePadding)
        ) {
            // 内容区
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(Dimens.moduleBorderRadius))
                    .background(Color.White)
                    .padding(Dimens.pagePadding)
            ) {
                if (isLoading && chatHistoryList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (chatHistoryList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无会话记录",
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(Dimens.pagePadding)
                    ) {
                        // 分组显示会话记录
                        groupedHistory.forEach { (timeGroup, historyList) ->
                            // 时间分组标题
                            item {
                                Column {
                                    Text(
                                        text = timeGroup,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // 该分组的会话记录
                            itemsIndexed(historyList) { index,history ->
                                Column(){
                                    // 显示提示词（最多2行）
                                    Spacer(modifier = Modifier.height(Dimens.pagePadding))
                                    Text(
                                        text = history.prompt,
                                        color = Color.Black,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.clickable{
                                            // 将会话记录加载到聊天列表
                                            viewModel.loadChatHistoryToChat(history)
                                            onDismiss()
                                        }
                                    )
                                    if(index != historyList.size - 1){
                                        Spacer(modifier = Modifier.height(Dimens.pagePadding))
                                        // 分隔线
                                        Divider(color = Color.Gray.copy(alpha = 0.2f))
                                    }

                                }

                            }

                            // 分组分隔线
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    thickness = 0.5.dp
                                )
                            }
                        }

                        // 加载更多指示器
                        item {
                            if (isLoading && chatHistoryList.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            } else if (!hasMoreData && chatHistoryList.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "已加载全部会话记录",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.labelSmall
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

@Composable
fun ChatHistoryItem(
    history: ChatHistory,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.btnBorderRadius))
            .background(Color.pageBackgroundColor)
            .clickable { onClick() }
            .padding(Dimens.pagePadding)
    ) {
        // 显示提示词（最多2行）
        Text(
            text = history.prompt,
            color = Color.Black,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}