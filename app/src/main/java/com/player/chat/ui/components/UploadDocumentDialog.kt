// UploadDocumentDialog.kt
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.player.chat.model.Directory
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.ChatViewModel
import com.player.chat.R

@Composable
fun UploadDocumentDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
    onUploadSuccess: () -> Unit
) {
    val directories by viewModel.directoryList.collectAsState()
    val selectedDirectory by viewModel.selectedDirectory.collectAsState()
    val showCreateDirectoryDialog by viewModel.showCreateDirectoryDialog.collectAsState()
    val isLoading by viewModel.isDirectoryLoading.collectAsState()

    CustomBottomDialog(
        title = "选择文件夹",
        onDismiss = onDismiss,
        leftIconRes = R.drawable.icon_add, // 使用加号图标作为左侧图标
        onLeftIconClick = { viewModel.showCreateDirectoryDialog() }, // 点击左侧图标创建文件夹
    ) {
        // 内容区 - 目录列表
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.pagePadding)
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
                        text = "暂无目录，点击左上角+号创建",
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

        // 底部按钮区 - 使用Row包装
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.pagePadding, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 取消按钮
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

            // 确定按钮
            Button(
                onClick = {
                    selectedDirectory?.let { directory ->
                        // 这里触发文件选择，实际实现需要从外部传入文件选择器
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

    // 创建文件夹对话框（仍然保留）
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