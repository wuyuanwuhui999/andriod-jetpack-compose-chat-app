package com.player.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens

data class OptionItem(
    val name: String,
    val value: String
)

@Composable
fun CustomBottomOption(
    options: List<OptionItem>,
    selectedValue: String? = null,
    onOptionSelected: (value: String, index: Int) -> Unit,
    onDismiss: () -> Unit,
) {
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
                .align(Alignment.BottomCenter)
                .padding(horizontal = Dimens.pagePadding)
                .padding(bottom = Dimens.pagePadding)
        ) {
            // 选项卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    options.forEachIndexed { index, option ->
                        // 选项行
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.btnHeight)
                                .clickable {
                                    onOptionSelected(option.value, index)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option.name,
                                fontSize = Dimens.fontSizeNormal,
                                color = if (option.value == selectedValue) {
                                    Color.PrimaryColor
                                } else {
                                    Color.Black
                                },
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // 选项之间的分隔线（最后一个选项后不显示）
                        if (index < options.size - 1) {
                            Divider(
                                color = Color.Gray.copy(alpha = 0.2f),
                                modifier = Modifier.padding(horizontal = 0.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.pagePadding))

            // 取消按钮
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.btnHeight),
                shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "取消",
                    fontSize = Dimens.fontSizeNormal,
                )
            }
        }
    }
}