package com.player.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens

/**
 * 输入对话框
 * @param title 对话框标题
 * @param initialValue 初始值
 * @param hint 输入提示
 * @param isRequired 是否必填
 * @param keyboardType 键盘类型
 * @param validator 自定义验证器（可选）
 * @param onConfirm 确认回调，返回输入的值
 * @param onDismiss 取消回调
 */
@Composable
fun InputDialog(
    title: String,
    initialValue: String = "",
    hint: String = "",
    isRequired: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    validator: ((String) -> Boolean)? = null,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf(initialValue) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 验证输入
    fun validateInput(): Boolean {
        return when {
            isRequired && inputText.isBlank() -> {
                errorMessage = "此项为必填项"
                false
            }
            validator != null && !validator(inputText) -> {
                errorMessage = "输入格式不正确"
                false
            }
            else -> {
                errorMessage = null
                true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(Dimens.moduleBorderRadius)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(Dimens.middleGap)
            ) {
                // 标题
                Text(
                    text = title,
                    color = Color.Black,
                    fontSize = Dimens.bigFontSize,
                    modifier = Modifier.padding(bottom = Dimens.middleGap)
                )

                // 输入框
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        errorMessage = null
                    },
                    label = { Text(hint) },
                    isError = errorMessage != null,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(Dimens.inputHeight / 2)
                )

                // 错误提示
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = Dimens.normalFontSize,
                        modifier = Modifier.padding(top = Dimens.smallGap)
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
                        shape = RoundedCornerShape(Dimens.btnHeight / 2)
                    ) {
                        Text("取消")
                    }

                    Button(
                        onClick = {
                            if (validateInput()) {
                                onConfirm(inputText)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.btnHeight / 2),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Primary
                        )
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}