package com.player.chat.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.player.chat.R
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.utils.CommonUtils
import com.player.chat.viewmodel.ChatViewModel

/**
 * 上传文档对话框
 * 流程：选择目录 -> 选择文件 -> 弹出"文档设置对话框"（权限、分割模式、分割大小）
 * -> 点击确定后再把文件与 splitMethod、chunkSize、permission 一起提交 uploadDoc 接口
 */
@Composable
fun UploadDocumentDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
    onUploadSuccess: () -> Unit
) {
    val context = LocalContext.current
    val directories by viewModel.directoryList.collectAsState()
    val selectedDirId by viewModel.selectedDirIdForUpload.collectAsState()
    val isLoading by viewModel.isDirectoryLoading.collectAsState()
    val isUploading by viewModel.isUploadingDocument.collectAsState()

    var uploadSuccess by remember { mutableStateOf(false) }
    // 已选中的文件 Uri（文件选择完成后暂存，等待文档设置对话框确认）
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    // 已选中文件的文件名（仅用于文档设置对话框展示）
    var pendingFileName by remember { mutableStateOf("") }
    // 是否展示文档设置对话框
    var showSettingsDialog by remember { mutableStateOf(false) }

    // 文件选择器：选择完成后不直接上传，先弹出文档设置对话框
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            pendingUri = it
            pendingFileName = CommonUtils.getFileName(context, it)
            showSettingsDialog = true
        }
    }

    // 上传成功提示
    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            kotlinx.coroutines.delay(500)
            onDismiss()
        }
    }

    // 文档设置对话框：设置权限、分割模式、分割大小后提交上传
    if (showSettingsDialog && pendingUri != null) {
        DocumentSettingsDialog(
            fileName = pendingFileName,
            isUploading = isUploading,
            onDismiss = {
                // 取消设置：关闭设置对话框并释放已选文件
                if (!isUploading) {
                    showSettingsDialog = false
                    pendingUri = null
                    pendingFileName = ""
                }
            },
            onConfirm = { splitMethod, chunkSize, permission ->
                val uri = pendingUri
                if (uri != null) {
                    viewModel.uploadDocumentWithCallback(
                        context = context,
                        uri = uri,
                        splitMethod = splitMethod,
                        chunkSize = chunkSize,
                        permission = permission,
                        onSuccess = {
                            showSettingsDialog = false
                            pendingUri = null
                            pendingFileName = ""
                            uploadSuccess = true
                            onUploadSuccess()
                        },
                        onFailure = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
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

                Text(
                    text = "上传文档",
                    color = Color.Black,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = Dimens.normalFontSize,
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(Dimens.middleIconSize)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_close),
                        contentDescription = "关闭",
                        tint = Color.Gray,
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

            // 内容区（目录列表）
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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectDirForUpload(directory.id ?: "") }
                                        .padding(horizontal = Dimens.middleGap, vertical = Dimens.middleGap),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = directory.directory,
                                        color = Color.Black,
                                        fontSize = Dimens.normalFontSize,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // 单选按钮
                                    RadioButton(
                                        selected = selectedDirId == directory.id,
                                        onClick = { viewModel.selectDirForUpload(directory.id ?: "") },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color.Primary
                                        )
                                    )
                                }

                                if (directories.indexOf(directory) < directories.size - 1) {
                                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                                }
                            }
                        }
                    }
                }
            }

            // 底部按钮
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
                        // 触发文件选择器，选择完成后弹出文档设置对话框
                        filePickerLauncher.launch("*/*")
                    },
                    enabled = selectedDirId != null && !isUploading,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.btnHeight),
                    shape = RoundedCornerShape(Dimens.btnHeight / 2),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedDirId != null) Color.Primary else Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text("确定", fontSize = Dimens.normalFontSize)
                }
            }
        }
    }
}
