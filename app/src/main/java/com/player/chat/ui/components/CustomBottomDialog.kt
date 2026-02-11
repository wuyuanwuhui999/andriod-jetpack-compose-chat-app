package com.player.chat.ui.components

import com.player.chat.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens

@Composable
fun CustomBottomDialog(
    title: String,
    onDismiss: () -> Unit,
    leftIconRes: Int? = null,
    onLeftIconClick: (() -> Unit)? = null, // 新增：左侧图标的点击回调
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
                    .fillMaxWidth()
                    .clickable{}// 阻止点击透传
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
                Spacer(modifier = Modifier.width(Dimens.pagePadding))
                // 左侧图标（可选 + 可点击）
                if (leftIconRes != null) {
                    IconButton(
                        modifier = Modifier.size(Dimens.smallIconSize),
                        onClick = {
                            onLeftIconClick?.invoke() ?: onDismiss() // 如果未提供点击事件，默认关闭？或什么都不做？
                        }) {
                        Icon(
                            painter = painterResource(id = leftIconRes),
                            contentDescription = "Left Icon",
                            tint = Color.Gray
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(Dimens.smallIconSize)) // 占位保持标题居中
                }

                // 标题文字
                Text(
                    text = title,
                    color = Color.Black,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                // 右侧关闭按钮
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(Dimens.smallIconSize)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_close),
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.pagePadding))
            }

            // 分隔线
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.borderSize)
                    .background(Color.Gray)
            )

            // 内容区
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.pageBackgroundColor) // 浅灰色背景
            ) {
                content()
            }
        }
    }
}