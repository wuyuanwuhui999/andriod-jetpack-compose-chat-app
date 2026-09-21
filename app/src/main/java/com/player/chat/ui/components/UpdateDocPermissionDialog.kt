package com.player.chat.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.player.chat.model.DocumentUploadConfig
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens

/**
 * 修改文档权限对话框
 * 功能：下拉选择文档权限（DOC_PERMISSION_OPTIONS），默认值为文档当前的 permission，
 * 点击"确定"通过 onConfirm 回调把选中的权限值交给上层调用 updateDocPermission 接口
 *
 * @param documentName 文档名称（仅用于展示）
 * @param defaultPermission 文档当前权限，用于下拉框回显
 * @param isSubmitting 是否正在提交，提交中禁用按钮并展示 loading
 * @param onDismiss 取消/关闭回调
 * @param onConfirm 确定回调，回传选中的权限值
 */
@Composable
fun UpdateDocPermissionDialog(
    documentName: String,
    defaultPermission: String,
    isSubmitting: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (permission: String) -> Unit
) {
    // 权限默认值取文档当前的 permission，若为空则回退到默认值（私密）
    var permission by remember(defaultPermission) {
        mutableStateOf(defaultPermission.ifBlank { DocumentUploadConfig.DEFAULT_PERMISSION })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = !isSubmitting) { onDismiss() }
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
                    text = "修改权限",
                    color = Color.Black,
                    fontSize = Dimens.bigFontSize,
                    modifier = Modifier.fillMaxWidth(),                  // 占满宽度，让居中生效
                    textAlign = TextAlign.Center,                       // 文字水平居中
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(Dimens.middleGap))

                // 文档名称
                Text(
                    text = documentName,
                    color = Color.Secondary,
                    fontSize = Dimens.normalFontSize,
                    maxLines = 2
                )

                Divider(
                    color = Color.DisableColor,
                    modifier = Modifier.padding(vertical = Dimens.middleGap)
                )

                // 文档权限下拉框：选项为 DOC_PERMISSION_OPTIONS，默认回显文档当前权限
                DropdownSelector(
                    label = "文档权限",
                    options = DocumentUploadConfig.DOC_PERMISSION_OPTIONS,
                    selectedValue = permission,
                    onValueChange = { permission = it },
                    isRequired = true,
                    enabled = !isSubmitting
                )

                Spacer(modifier = Modifier.height(Dimens.middleGap))

                // 底部按钮：取消 / 确定
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.middleGap)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.btnHeight / 2),
                        border = BorderStroke(1.dp, Color.DisableColor),          // 灰色边框
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.DisableColor
                        )
                    ) {
                        Text("取消", fontSize = Dimens.normalFontSize)
                    }

                    Button(
                        onClick = { onConfirm(permission) },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.btnHeight),
                        shape = RoundedCornerShape(Dimens.btnHeight / 2),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSubmitting) Color.Gray else Color.Primary,
                            contentColor = Color.White
                        )
                    ) {
                        if (isSubmitting) {
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
