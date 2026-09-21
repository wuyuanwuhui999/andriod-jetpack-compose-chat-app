package com.player.chat.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.player.chat.R
import com.player.chat.model.Directory
import com.player.chat.model.Document
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.utils.DocumentListUtils
import com.player.chat.viewmodel.ChatViewModel

/**
 * 我的文档对话框
 * 样式与"选择文档"（QueryDocumentDialog）一致，区别：
 * 1. 文档条目没有复选框，也没有底部"确定/取消"按钮
 * 2. 文档条目右侧为"三个点"操作图标，点击可修改权限或删除文档
 * 交互：打开时先加载目录列表，点击目录右侧箭头才加载该目录下的文档，箭头旋转 90 度指向下方
 */
@Composable
fun MyDocumentsDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val directories by viewModel.directoryList.collectAsState()
    val expandedDirectories by viewModel.expandedDirectories.collectAsState()
    val directoryDocuments by viewModel.directoryDocuments.collectAsState()
    val isLoading by viewModel.isDirectoryLoading.collectAsState()
    val showCreateDirDialog by viewModel.showCreateDirDialog.collectAsState()

    // 需要修改权限的文档ID与点击时的快照（只存 id，渲染时优先从最新列表里取文档，避免持有旧 permission）
    var permissionDocId by remember { mutableStateOf<String?>(null) }
    var permissionDocSnapshot by remember { mutableStateOf<Document?>(null) }
    // 需要删除的文档（非空时展示删除确认对话框）
    var deleteDoc by remember { mutableStateOf<Document?>(null) }
    // 是否正在提交（修改权限）
    var isSubmitting by remember { mutableStateOf(false) }

    // 修改权限对话框：优先用当前列表里最新的文档对象回显（权限改动后立即生效），
    // 列表里找不到时退回点击时的快照，保证对话框仍能打开
    val permissionDoc = DocumentListUtils.findDocument(directoryDocuments, permissionDocId)
        ?: permissionDocSnapshot

    // 注意：修改权限对话框必须放在下方根 Box 内、基础弹窗之后渲染，
    // 否则会被基础弹窗的半透明遮罩与内容覆盖，导致"点了没反应"（看不见）

    // 删除确认对话框
    deleteDoc?.let { doc ->
        AlertDialog(
            onDismissRequest = { deleteDoc = null },
            title = { Text("删除文档") },
            text = { Text("确定要删除文档「${doc.name}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteDoc = null
                        viewModel.deleteDocumentWithCallback(
                            docId = doc.id
                        ) { _, msg ->
                            // 成功与失败都提示后端返回的 msg
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("确定", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDoc = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 使用 Box 作为根容器，确保创建目录对话框显示在最上层
    Box(modifier = Modifier.fillMaxSize()) {
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
                // 标题栏（与"选择文档"一致：左侧刷新、中间标题、右侧创建目录与上传）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.barHeight)
                        .background(Color.White),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(Dimens.middleGap))

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

                    Text(
                        text = "我的文档",
                        color = Color.Black,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

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

                    Spacer(modifier = Modifier.width(Dimens.middleGap))

                    IconButton(
                        onClick = {
                            viewModel.toggleMyDocumentsDialog()
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

                // 内容区：目录 + 文档列表
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
                                    MyDocumentDirectoryItem(
                                        directory = directory,
                                        isExpanded = expandedDirectories.contains(directory.id),
                                        documents = directoryDocuments[directory.id] ?: emptyList(),
                                        onDirectoryClick = { viewModel.toggleDirectoryExpanded(directory) },
                                        onEditPermission = { doc ->
                                            permissionDocId = doc.id
                                            permissionDocSnapshot = doc
                                        },
                                        onDelete = { doc -> deleteDoc = doc }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 创建目录对话框放在最外层 Box 内部，确保显示在"我的文档"之上
        if (showCreateDirDialog) {
            CreateDirectoryDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.hideCreateDirDialog() }
            )
        }

        // 修改权限对话框：同样放在最外层 Box 内部、基础弹窗之后，保证绘制在最上层
        permissionDoc?.let { doc ->
            UpdateDocPermissionDialog(
                documentName = doc.name,
                defaultPermission = doc.permission.orEmpty(),
                isSubmitting = isSubmitting,
                onDismiss = { if (!isSubmitting) permissionDocId = null },
                onConfirm = { permission ->
                    isSubmitting = true
                    viewModel.updateDocPermissionWithCallback(
                        docId = doc.id,
                        permission = permission
                    ) { _, msg ->
                        isSubmitting = false
                        permissionDocId = null
                        // 成功与失败都提示后端返回的 msg
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

/**
 * 目录及其文档列表项（我的文档）
 * 目录行可点击展开，箭头图标展开时顺时针旋转 90 度指向下方；
 * 每个文档条目右侧是"三个点"操作图标，点击弹出"修改权限/删除"菜单
 *
 * @param directory 目录
 * @param isExpanded 是否已展开
 * @param documents 该目录下的文档列表
 * @param onDirectoryClick 点击目录（或箭头）回调，用于展开/收起并加载文档
 * @param onEditPermission 点击"修改权限"回调
 * @param onDelete 点击"删除"回调
 */
@Composable
fun MyDocumentDirectoryItem(
    directory: Directory,
    isExpanded: Boolean,
    documents: List<Document>,
    onDirectoryClick: () -> Unit,
    onEditPermission: (Document) -> Unit,
    onDelete: (Document) -> Unit
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // 展开箭头：点击后才加载该目录下的文档，展开时顺时针旋转 90 度朝下
            Icon(
                painter = painterResource(id = R.drawable.icon_arrow),
                contentDescription = if (isExpanded) "收起" else "展开",
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(Dimens.smallIconSize)
                    .rotate(if (isExpanded) 90f else 0f)
                    .clickable { onDirectoryClick() }
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
                    MyDocumentItem(
                        document = document,
                        onEditPermission = { onEditPermission(document) },
                        onDelete = { onDelete(document) }
                    )

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

/**
 * 单个文档条目（我的文档）
 * 左侧文档名称，右侧"三个点"操作图标（不含时间，不含复选框）
 *
 * @param document 文档
 * @param onEditPermission 点击"修改权限"回调
 * @param onDelete 点击"删除"回调
 */
@Composable
fun MyDocumentItem(
    document: Document,
    onEditPermission: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
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

        // 三个点操作图标 + 操作菜单
        Box {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "操作",
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(Dimens.middleIconSize)
                    .clickable { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "修改权限",
                            color = Color.Black,
                            fontSize = Dimens.normalFontSize
                        )
                    },
                    onClick = {
                        expanded = false
                        onEditPermission()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "删除",
                            color = Color.Red,
                            fontSize = Dimens.normalFontSize
                        )
                    },
                    onClick = {
                        expanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}
