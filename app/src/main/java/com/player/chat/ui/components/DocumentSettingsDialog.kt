package com.player.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.player.chat.model.DocumentUploadConfig
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens

/**
 * 文档设置对话框
 * 功能：选择文档后弹出，用于设置文档权限、分割模式（fixed 时额外设置分割大小）
 * 点击"确定"后通过 onConfirm 回调把 splitMethod、chunkSize、permission 交给上层提交 uploadDoc 接口
 *
 * @param fileName 已选中的文件名（仅用于展示）
 * @param isUploading 是否正在上传，上传中禁用按钮并展示 loading
 * @param onDismiss 取消/关闭回调
 * @param onConfirm 确定回调，回传 (splitMethod, chunkSize, permission)
 */
@Composable
fun DocumentSettingsDialog(
    fileName: String,
    isUploading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (splitMethod: String, chunkSize: Int, permission: String) -> Unit
) {
    // 文档权限，默认私密
    var permission by remember { mutableStateOf(DocumentUploadConfig.DEFAULT_PERMISSION) }
    // 分割模式，默认递归字符分割
    var splitMethod by remember { mutableStateOf(DocumentUploadConfig.DEFAULT_SPLIT_METHOD) }
    // 分割大小，默认 1000，仅"固定长度分割"时可见
    var chunkSizeText by remember { mutableStateOf(DocumentUploadConfig.DEFAULT_CHUNK_SIZE.toString()) }

    // 仅固定长度分割时展示分割大小输入框
    val showChunkSize = splitMethod == DocumentUploadConfig.SPLIT_METHOD_FIXED
    // 分割大小必须是大于 0 的整数
    val chunkSizeValid = !showChunkSize || (chunkSizeText.toIntOrNull() ?: 0) > 0
    val confirmEnabled = !isUploading && chunkSizeValid

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = !isUploading) { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.85f)
                .align(Alignment.Center)
                .clickable(enabled = false) {}
                .clip(RoundedCornerShape(Dimens.moduleBorderRadius)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(Dimens.middleGap)
                    .verticalScroll(rememberScrollState())
            ) {
                // 标题
                Text(
                    text = "文档设置",
                    color = Color.Black,
                    fontSize = Dimens.bigFontSize,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(Dimens.middleGap))

                // 已选中的文件名
                Text(
                    text = if (fileName.isBlank()) "未选择文件" else fileName,
                    color = Color.Gray,
                    fontSize = Dimens.normalFontSize,
                    maxLines = 2
                )

                Divider(
                    color = Color.Gray.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = Dimens.middleGap)
                )

                // 文档权限下拉框：private-私密 / tenant-租户内公开 / company-公司内公开
                DropdownSelector(
                    label = "文档权限",
                    options = DocumentUploadConfig.PERMISSION_OPTIONS,
                    selectedValue = permission,
                    onValueChange = { permission = it },
                    isRequired = true,
                    enabled = !isUploading
                )

                // 分割模式下拉框：recursive / paragraph / sentence / fixed
                DropdownSelector(
                    label = "分割模式",
                    options = DocumentUploadConfig.SPLIT_METHOD_OPTIONS,
                    selectedValue = splitMethod,
                    onValueChange = { splitMethod = it },
                    isRequired = true,
                    enabled = !isUploading
                )

                // 分割大小输入框：仅"固定长度分割"时显示
                if (showChunkSize) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "分割大小",
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontSize = Dimens.normalFontSize,
                            modifier = Modifier.padding(vertical = Dimens.middleGap)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.inputHeight)
                                .clip(RoundedCornerShape(Dimens.inputHeight / 2))
                                .background(Color.PageBackground)
                                .padding(horizontal = Dimens.middleGap),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            BasicTextField(
                                value = chunkSizeText,
                                // 只允许输入数字
                                onValueChange = { input ->
                                    chunkSizeText = input.filter { it.isDigit() }.take(6)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle.Default.copy(
                                    color = Color.Black,
                                    fontSize = Dimens.normalFontSize
                                ),
                                cursorBrush = SolidColor(Color.Primary),
                                singleLine = true,
                                enabled = !isUploading,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (chunkSizeText.isEmpty()) {
                                            Text(
                                                text = "请输入分割大小",
                                                color = Color.DisableColor,
                                                fontSize = Dimens.normalFontSize
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        // 分割大小非法提示
                        if (!chunkSizeValid) {
                            Text(
                                text = "请输入大于 0 的整数",
                                color = Color.Red,
                                fontSize = Dimens.normalFontSize,
                                modifier = Modifier.padding(top = Dimens.smallGap)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.middleGap))

                // 底部按钮：取消 / 确定
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.middleGap)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isUploading,
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.btnHeight / 2),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("取消", fontSize = Dimens.normalFontSize)
                    }

                    Button(
                        onClick = {
                            // 非固定长度分割时，分割大小不参与提交，传默认值
                            val chunkSize = if (showChunkSize) {
                                chunkSizeText.toIntOrNull() ?: DocumentUploadConfig.DEFAULT_CHUNK_SIZE
                            } else {
                                DocumentUploadConfig.DEFAULT_CHUNK_SIZE
                            }
                            onConfirm(splitMethod, chunkSize, permission)
                        },
                        enabled = confirmEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.btnHeight / 2),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (confirmEnabled) Color.Primary else Color.Gray,
                            contentColor = Color.White
                        )
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimens.middleIconSize),
                                color = Color.White,
                                strokeWidth = Dimens.strokeWidth
                            )
                        } else {
                            Text("确定", fontSize = Dimens.normalFontSize)
                        }
                    }
                }
            }
        }
    }
}
