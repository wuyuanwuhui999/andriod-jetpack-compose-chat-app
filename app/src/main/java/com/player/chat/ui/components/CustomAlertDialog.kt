package com.player.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens

@Composable
fun CustomAlertDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() } // 点击遮罩关闭
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.7f)
                .clickable(enabled = false) {} // 阻止点击透传
                .wrapContentHeight() // 根据内容自适应高度
                .align(Alignment.Center)
                .clip(RoundedCornerShape(Dimens.moduleBorderRadius)) // 为整个对话框添加圆角
                .background(Color.White) // 在clip之后设置背景
        ) {
            // 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.barHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.Black,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            // 内容区
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.pagePadding)
                    .wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {
                content()
            }

            // 分隔线
            Divider(
                color = Color.Gray.copy(alpha = 0.2f),
                thickness = Dimens.borderSize
            )

            // 底部按钮区
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.btnHeight)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape( // 为底部按钮区添加圆角
                            bottomStart = Dimens.moduleBorderRadius,
                            bottomEnd = Dimens.moduleBorderRadius
                        )
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 取消按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape( // 取消按钮左下角圆角
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = Dimens.moduleBorderRadius,
                        bottomEnd = 0.dp
                    )
                ) {
                    Text(
                        text = "取消",
                        fontSize = Dimens.fontSizeNormal,
                    )
                }

                // 垂直分隔线
                Divider(
                    color = Color.Gray.copy(alpha = 0.2f),
                    modifier = Modifier
                        .width(Dimens.borderSize)
                        .fillMaxHeight()
                )

                // 确定按钮
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.PrimaryColor
                    ),
                    shape = RoundedCornerShape( // 确定按钮右下角圆角
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 0.dp,
                        bottomEnd = Dimens.moduleBorderRadius
                    )
                ) {
                    Text(
                        text = "确定",
                        fontSize = Dimens.fontSizeNormal,
                    )
                }
            }
        }
    }
}