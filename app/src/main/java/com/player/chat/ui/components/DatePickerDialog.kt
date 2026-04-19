package com.player.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.player.chat.ui.theme.Color
import com.player.chat.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit,
    initialDate: String? = null
) {
    val calendar = Calendar.getInstance()

    // 解析初始日期
    initialDate?.let {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = format.parse(it)
            date?.let { calendar.time = it }
        } catch (e: Exception) {
            // 使用当前日期
        }
    }

    var year by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var day by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.9f)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(Dimens.moduleBorderRadius)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(Dimens.middleGap)
            ) {
                Text(
                    text = "选择出生日期",
                    color = Color.Black,
                    fontSize = Dimens.bigFontSize,
                    modifier = Modifier.padding(bottom = Dimens.middleGap)
                )

                // 日期选择器
                DatePicker(
                    state = rememberDatePickerState(
                        yearRange = IntRange(1900, 2024),
                        initialSelectedDateMillis = calendar.timeInMillis
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black,
                        headlineContentColor = Color.Black,
                        weekdayContentColor = Color.Black,
                        subheadContentColor = Color.Gray,
                        yearContentColor = Color.Black,
                        todayDateBorderColor = Color.Primary,
                        todayContentColor = Color.Primary
                    )
                )

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
                            val selectedCalendar = Calendar.getInstance()
                            selectedCalendar.set(year, month, day)
                            onDateSelected(selectedCalendar.time)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    state: DatePickerState,
    modifier: Modifier = Modifier,
    colors: DatePickerColors = DatePickerDefaults.colors()
) {
    androidx.compose.material3.DatePicker(
        state = state,
        modifier = modifier,
        colors = colors,
        showModeToggle = false
    )
}