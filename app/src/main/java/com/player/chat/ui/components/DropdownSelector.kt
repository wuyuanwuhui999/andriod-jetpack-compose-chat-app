package com.player.chat.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.player.chat.R
import com.player.chat.model.DropdownOption
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens

/**
 * 通用下拉选择组件（可复用）
 * 左侧为标题，右侧为当前选中项的展示文案 + 展开箭头，点击后弹出下拉菜单
 *
 * @param label 左侧标题，如"文档权限"
 * @param options 下拉选项列表（value 提交后端，label 界面展示）
 * @param selectedValue 当前选中的 value
 * @param onValueChange 选中回调，回传选项的 value
 * @param isRequired 是否展示必填星号
 * @param placeholder 未选中时的占位文案
 * @param enabled 是否可点击（上传中时禁用）
 */
@Composable
fun DropdownSelector(
    label: String,
    options: List<DropdownOption>,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    isRequired: Boolean = false,
    placeholder: String = "请选择",
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    // 当前选中项对应的展示文案
    val selectedLabel = options.firstOrNull { it.value == selectedValue }?.label ?: placeholder

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.middleGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 标题
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                fontSize = Dimens.normalFontSize
            )
            if (isRequired) {
                Text(
                    text = " *",
                    color = Color.Red,
                    fontSize = Dimens.normalFontSize
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = Dimens.middleGap)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) { expanded = !expanded },
                shape = RoundedCornerShape(Dimens.moduleBorderRadius),
                color = Color.DisableColor.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.middleGap, horizontal = Dimens.middleGap),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedLabel,
                        color = if (selectedValue.isNotBlank()) Color.Black else Color.Gray,
                        fontSize = Dimens.normalFontSize
                    )
                    Icon(
                        painter = painterResource(
                            if (expanded) R.drawable.icon_down else R.drawable.icon_arrow
                        ),
                        contentDescription = if (expanded) "收起" else "展开",
                        tint = Color.DisableColor,
                        modifier = Modifier.size(Dimens.smallIconSize)
                    )
                }
            }

            // 下拉菜单
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                color = Color.Black,
                                fontSize = Dimens.normalFontSize
                            )
                        },
                        onClick = {
                            onValueChange(option.value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
