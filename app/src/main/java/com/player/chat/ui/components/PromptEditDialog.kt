package com.player.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.ChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 修改提示词对话框
 * @param viewModel ChatViewModel实例
 * @param onDismiss 关闭对话框回调
 * @param onSuccess 保存成功回调
 */
@Composable
fun PromptEditDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    val promptText by viewModel.promptText.collectAsState()
    val isUpdating by viewModel.isUpdatingPrompt.collectAsState()

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {} // 阻止点击透传
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

                // 标题文字
                Text(
                    text = "修改提示词",
                    color = Color.Black,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // 右侧关闭按钮
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(Dimens.smallIconSize)
                ) {
                    Icon(
                        painter = painterResource(id = com.player.chat.R.drawable.icon_close),
                        contentDescription = "关闭",
                        tint = Color.Gray
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

            // 内容区
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.PageBackground)
                    .padding(Dimens.middleGap)
            ) {
                // 白色背景圆角矩形
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.moduleBorderRadius))
                        .background(Color.White)
                        .padding(Dimens.middleGap)
                ) {
                    // 文本框
                    BasicTextField(
                        value = promptText,
                        onValueChange = { viewModel.updatePromptText(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Dimens.moduleBorderRadius))
                            .background(Color.PageBackground)
                            .padding(Dimens.smallGap),
                        textStyle = TextStyle.Default.copy(color = Color.Black),
                        cursorBrush = SolidColor(Color.Primary),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (promptText.isEmpty()) {
                                    Text(
                                        text = "请输入提示词...",
                                        color = Color.Gray
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(Dimens.middleGap))

                    // 确定按钮
                    Button(
                        onClick = {
                            scope.launch {
                                val result = viewModel.savePrompt()
                                if (result.isSuccess) {
                                    // 接口调用成功后，弹出msg提示
                                    onSuccess()
                                    onDismiss()
                                } else {
                                    // 显示错误信息
                                    val errorMsg = result.exceptionOrNull()?.message ?: "保存失败"
                                    // 可以通过Toast或Snackbar显示错误信息
                                }
                            }
                        },
                        enabled = promptText.isNotBlank() && !isUpdating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.btnHeight / 2),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (promptText.isNotBlank()) Color.Primary else Color.Gray,
                            contentColor = Color.White
                        )
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "确定",
                                fontSize = Dimens.normalFontSize
                            )
                        }
                    }
                }
            }
        }
    }
}