package com.player.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.player.chat.model.Directory
import com.player.chat.model.Document
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.ChatViewModel
import com.player.chat.R
import com.player.chat.utils.CommonUtils.formatRelativeTime

@Composable
fun MyDocumentsDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    val directories by viewModel.directoryList.collectAsState()
    val expandedDirectories by viewModel.expandedDirectories.collectAsState()
    val directoryDocuments by viewModel.directoryDocuments.collectAsState()
    val isLoading by viewModel.isDocumentsLoading.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedDocument by remember { mutableStateOf<Document?>(null) }

    // 删除确认对话框
    if (showDeleteDialog && selectedDocument != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除文档") },
            text = { Text("确定要删除文档 ${selectedDocument?.name} 吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDocument?.let { doc ->
                            viewModel.deleteDocument(doc.id, doc.directoryId)
                        }
                        showDeleteDialog = false
                        selectedDocument = null
                    }
                ) {
                    Text("确定", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    selectedDocument = null
                }) {
                    Text("取消")
                }
            }
        )
    }

    CustomBottomDialog(
        title = "我的文档",
        onDismiss = onDismiss,
        leftIconRes = null // 左侧不需要图标
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.pagePadding)
        ) {
            // 内容区 - 白色背景+圆角，高度自适应
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(Dimens.moduleBorderRadius))
                    .background(Color.White)
                    .padding(Dimens.pagePadding)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimens.middleAvater),
                            strokeWidth = Dimens.borderSize
                        )
                    }
                } else if (directories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无目录",
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn {
                        items(directories) { directory ->
                            DocumentsItem(
                                directory = directory,
                                isExpanded = expandedDirectories.contains(directory.id),
                                documents = directoryDocuments[directory.id] ?: emptyList(),
                                onDirectoryClick = { viewModel.toggleDirectoryExpanded(directory) },
                                onDeleteDocument = { doc ->
                                    selectedDocument = doc
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.pagePadding))

            // 底部关闭按钮
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.btnHeight),
                shape = RoundedCornerShape(Dimens.bigBorderRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.player.chat.ui.theme.Color.PrimaryColor
                )
            ) {
                Text("关闭")
            }
        }
    }
}

@Composable
fun DocumentsItem(
    directory: Directory,
    isExpanded: Boolean,
    documents: List<Document>,
    onDirectoryClick: () -> Unit,
    onDeleteDocument: (Document) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 目录项
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDirectoryClick() }
                .padding(vertical = Dimens.pagePadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 目录名称
            Text(
                text = directory.directory,
                color = com.player.chat.ui.theme.Color.Black,
                fontSize = Dimens.fontSizeNormal,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(Dimens.pagePadding))

            // 箭头图标
            Icon(
                painter = painterResource(if(isExpanded)R.drawable.icon_down else R.drawable.icon_arrow),
                contentDescription = if (isExpanded) "收起" else "展开",
                tint = Color.Gray,
                modifier = Modifier.size(Dimens.smallIconSize)
            )
        }

        // 分隔线
        Divider(color = Color.Gray.copy(alpha = 0.2f))

        // 文档列表（如果展开）
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Dimens.pagePadding)
            ) {
                documents.forEach { document ->
                    SwipeToDeleteDocumentItem(
                        document = document,
                        onDelete = { onDeleteDocument(document) }
                    )
                }

                if (documents.isEmpty()) {
                    Text(
                        text = "该目录下暂无文档",
                        color = com.player.chat.ui.theme.Color.Gray,
                        modifier = Modifier
                            .padding(vertical = Dimens.pagePadding)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteDocumentItem(
    document: Document,
    onDelete: () -> Unit
) {
    var swipeState by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.btnHeight)
    ) {
        // 删除按钮（在右侧）
        if (swipeState) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Red)
                    .clickable {
                        onDelete()
                        swipeState = false
                    },
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "删除",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // 文档项
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.White)
                .clickable(enabled = !swipeState) {
                    // 点击文档的逻辑（可选）
                }
                .swipeToReveal(
                    revealDirection = RevealDirection.EndToStart,
                    onReveal = { swipeState = true },
                    onConceal = { swipeState = false },
                    threshold = 0.3f
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.pagePadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 文档名称和格式
                Text(
                    text = document.name,
                    color = Color.Black,
                    fontSize = Dimens.fontSizeNormal,
                    modifier = Modifier.weight(1f)
                )
                // 文档信息
                Text(
                    text = formatRelativeTime(document.createTime),
                    color = Color.Gray,
                    fontSize = Dimens.fontSizeNormal
                )
            }
        }
    }

    // 分隔线
    Divider(color = com.player.chat.ui.theme.Color.Gray.copy(alpha = 0.1f))
}

// 简单的滑动显示隐藏组件（简化实现）
@Composable
fun Modifier.swipeToReveal(
    revealDirection: RevealDirection = RevealDirection.EndToStart,
    onReveal: () -> Unit,
    onConceal: () -> Unit,
    threshold: Float = 0.3f
): Modifier {
    // 这是一个简化的实现，实际项目中可能需要使用更复杂的滑动处理
    // 这里使用 clickable 来模拟点击切换状态
    return this.clickable {
        // 在实际应用中，这里应该处理滑动事件
        // 为了简化，我们使用点击来切换状态
        onReveal()
    }
}

enum class RevealDirection {
    StartToEnd,
    EndToStart
}