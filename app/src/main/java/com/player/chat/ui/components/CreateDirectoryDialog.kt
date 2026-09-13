package com.player.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import com.player.chat.viewmodel.ChatViewModel

/**
 * 创建目录对话框
 * 输入目录名称，确定按钮在输入非空时激活
 */
@Composable
fun CreateDirectoryDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    var directoryName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.85f)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(Dimens.moduleBorderRadius)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(Dimens.middleGap)
            ) {
                // 标题
                Text(
                    text = "创建目录",
                    color = Color.Black,
                    fontSize = Dimens.bigFontSize,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = Dimens.middleGap)
                )

                // 输入框
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
                        value = directoryName,
                        onValueChange = { directoryName = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle.Default.copy(
                            color = Color.Black,
                            fontSize = Dimens.normalFontSize
                        ),
                        cursorBrush = SolidColor(Color.Primary),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (directoryName.isEmpty()) {
                                    Text(
                                        text = "请输入目录名称",
                                        color = Color.DisableColor,
                                        fontSize = Dimens.normalFontSize
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.middleGap))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            viewModel.createDirectoryWithCallback(directoryName) {
                                onDismiss()
                            }
                        },
                        enabled = directoryName.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.btnHeight / 2),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (directoryName.isNotBlank()) Color.Primary else Color.Gray,
                            contentColor = Color.White
                        )
                    ) {
                        Text("确定", fontSize = Dimens.normalFontSize)
                    }
                }
            }
        }
    }
}