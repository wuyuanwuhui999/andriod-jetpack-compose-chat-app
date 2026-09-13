package com.player.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.player.chat.R
import com.player.chat.model.Directory
import com.player.chat.model.Document
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.ChatViewModel

/**
 * 查询文档对话框
 * 功能：显示目录列表，支持展开查看文档、刷新目录、创建目录、上传文档
 */
@Composable
fun QueryDocumentDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    val directories by viewModel.directoryList.collectAsState()
    val expandedDirectories by viewModel.expandedDirectories.collectAsState()
    val directoryDocuments by viewModel.directoryDocuments.collectAsState()
    val isLoading by viewModel.isDirectoryLoading.collectAsState()
    val selectedDocIds by viewModel.selectedDocIds.collectAsState()
    val showCreateDirDialog by viewModel.showCreateDirDialog.collectAsState()

    // 创建目录对话框
    if (showCreateDirDialog) {
        CreateDirectoryDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.hideCreateDirDialog() }
        )
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
                .fillMaxHeight(fraction = 0.85f)
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
                    onClick = { viewModel.loadDirectoriesForQuery() },
                    modifier = Modifier.size(Dimens.middleIconSize)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_refresh),
                        contentDescription = "刷新",
                        modifier = Modifier.size(Dimens.middleIconSize)
                    )
                }

                // 标题
                Text(
                    text = "选择文档",
                    color = Color.Black,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // 右侧创建目录图标
                IconButton(
                    onClick = { viewModel.showCreateDirDialog() },
                    modifier = Modifier.size(Dimens.middleIconSize)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_create_directory),
                        contentDescription = "创建目录",
                        modifier = Modifier.size(Dimens.middleIconSize)
                    )
                }

                Spacer(
                    modifier = Modifier
                        .width(Dimens.middleGap)
                )

                // 右侧上传图标
                IconButton(
                    onClick = {
                        viewModel.hideQueryDocumentDialog()
                        viewModel.showUploadDocumentDialog()
                    },
                    modifier = Modifier.size(Dimens.middleIconSize)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_upload),
                        contentDescription = "上传文档",
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
                    .background(Color.Gray.copy(alpha = 0.3f))
            )

            // 内容区
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.PageBackground)
                    .padding(Dimens.middleGap)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    if (isLoading && directories.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.middleGap),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimens.bigIconSize),
                                color = Color.Primary,
                                strokeWidth = Dimens.strokeWidth
                            )
                        }
                    } else if (directories.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.middleGap),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无目录",
                                color = Color.Gray,
                                fontSize = Dimens.normalFontSize
                            )
                        }
                    } else {
                        LazyColumn {
                            items(directories) { directory ->
                                DirectoryWithDocumentsItem(
                                    directory = directory,
                                    isExpanded = expandedDirectories.contains(directory.id),
                                    documents = directoryDocuments[directory.id] ?: emptyList(),
                                    selectedDocIds = selectedDocIds,
                                    onDirectoryClick = { viewModel.toggleDirectoryExpanded(directory) },
                                    onDocumentToggle = { docId -> viewModel.toggleDocSelection(docId) }
                                )
                            }
                        }
                    }
                }
            }

            // 底部确定按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(Dimens.middleGap),
                horizontalArrangement = Arrangement.spacedBy(Dimens.middleGap)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
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

                Button(
                    onClick = {
                        // 确定后关闭对话框
                        onDismiss()
                    },
                    enabled = selectedDocIds.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.btnHeight),
                    shape = RoundedCornerShape(Dimens.btnHeight / 2),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedDocIds.isNotEmpty()) Color.Primary else Color.Gray,
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
 * 目录及文档列表项
 */
@Composable
fun DirectoryWithDocumentsItem(
    directory: Directory,
    isExpanded: Boolean,
    documents: List<Document>,
    selectedDocIds: Set<String>,
    onDirectoryClick: () -> Unit,
    onDocumentToggle: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 目录项
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDirectoryClick() }
                .padding(horizontal = Dimens.middleGap, vertical = Dimens.middleGap),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = directory.directory,
                color = Color.Black,
                fontSize = Dimens.normalFontSize,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            // 箭头图标（展开时旋转90度向下）
            Icon(
                painter = painterResource(id = R.drawable.icon_arrow),
                contentDescription = if (isExpanded) "收起" else "展开",
                modifier = Modifier
                    .size(Dimens.smallIconSize)
                    .rotate(if (isExpanded) -90f else 0f)
            )
        }

        // 分隔线
        Divider(color = Color.Gray.copy(alpha = 0.2f))

        // 文档列表（展开时显示）
        if (isExpanded) {
            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.middleGap),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "该目录下暂无文档",
                        color = Color.DisableColor,
                        fontSize = Dimens.normalFontSize
                    )
                }
            } else {
                documents.forEach { document ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDocumentToggle(document.id) }
                            .padding(horizontal = Dimens.middleGap, vertical = Dimens.middleGap),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = document.name,
                            color = Color.Black,
                            fontSize = Dimens.normalFontSize,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // 复选框
                        Checkbox(
                            checked = selectedDocIds.contains(document.id),
                            onCheckedChange = { onDocumentToggle(document.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color.Primary
                            )
                        )
                    }

                    if (documents.indexOf(document) < documents.size - 1) {
                        Divider(
                            color = Color.Gray.copy(alpha = 0.1f),
                            modifier = Modifier.padding(start = Dimens.middleGap)
                        )
                    }
                }
            }
        }
    }
}