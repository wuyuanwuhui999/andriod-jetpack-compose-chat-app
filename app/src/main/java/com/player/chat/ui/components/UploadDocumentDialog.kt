// UploadDocumentDialog.kt (新增文件)
package com.player.chat.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.player.chat.model.Directory
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadDocumentDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
    onUploadSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val directories by viewModel.directoryList.collectAsState()
    val selectedDirectory by viewModel.selectedDirectory.collectAsState()
    val showCreateDirectoryDialog by viewModel.showCreateDirectoryDialog.collectAsState()
    val isLoading by viewModel.isDirectoryLoading.collectAsState()

    // 灰色透明遮罩层
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        // 白色对话框 - 距离顶部30%
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .padding(top = (0.3f * LocalContext.current.resources.displayMetrics.heightPixels / LocalContext.current.resources.displayMetrics.density).dp)
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .clickable { }, // 阻止点击透传
            colors = CardDefaults.cardColors(
                containerColor = ComposeColor.White
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // + 号按钮
                    IconButton(
                        onClick = { viewModel.showCreateDirectoryDialog() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "创建文件夹",
                            tint = Color.Black
                        )
                    }

                    // 标题
                    Text(
                        text = "选择文件夹",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )

                    // 关闭按钮
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = Color.Black
                        )
                    }
                }

                Divider(color = Color.Gray.copy(alpha = 0.3f))

                // 内容区 - 目录列表
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    } else if (directories.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无目录，点击+号创建",
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn {
                            items(directories) { directory ->
                                DirectoryItem(
                                    directory = directory,
                                    isSelected = selectedDirectory?.id == directory.id,
                                    onSelect = { viewModel.selectDirectory(directory) }
                                )
                            }
                        }
                    }
                }

                // 底部按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 取消按钮 - 和注册按钮样式一样
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.bigBorderRadius),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(Dimens.borderSize, Color.disableTextColor)

                    ) {
                        Text("取消")
                    }

                    // 确定按钮 - 和登录按钮样式一样
                    Button(
                        onClick = {
                            selectedDirectory?.let { directory ->
                                viewModel.openFilePicker(context)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.btnHeight),
                        enabled = selectedDirectory != null,
                        shape = RoundedCornerShape(Dimens.bigBorderRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedDirectory != null)
                                Color.PrimaryColor
                            else
                                Color.disableTextColor,
                            contentColor = if (selectedDirectory != null)
                                Color.White
                            else
                                Color.Gray
                        )
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }

    // 创建文件夹对话框
    if (showCreateDirectoryDialog) {
        CreateDirectoryDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.hideCreateDirectoryDialog() }
        )
    }
}

@Composable
fun DirectoryItem(
    directory: Directory,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 目录名称
        Text(
            text = directory.directory,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 单选按钮
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) Color.PrimaryColor else Color.Gray.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }

    Divider(color = Color.Gray.copy(alpha = 0.2f))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDirectoryDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    var directoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "创建文件夹", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            OutlinedTextField(
                value = directoryName,
                onValueChange = { directoryName = it },
                label = { Text("文件夹名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (directoryName.isNotBlank()) {
                        viewModel.createDirectory(directoryName)
                        onDismiss()
                    }
                },
                enabled = directoryName.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}