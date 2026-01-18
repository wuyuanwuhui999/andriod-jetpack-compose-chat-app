// Color.kt
package com.player.chat.ui.theme

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color as ComposeColor

object Color {
    @ColorInt const val PAGE_BACKGROUND_COLOR = 0xFFEFEFEF.toInt()
    @ColorInt const val MODULE_BACKGROUND_COLOR = 0xFFFFFFFF.toInt()
    @ColorInt const val DISABLE_TEXT_COLOR = 0xFFDDDDDD.toInt()
    @ColorInt const val TAB_COLOR_ACTIVE = 0xFFFFAE00.toInt()
    @ColorInt const val SEARCH_INPUT_COLOR = 0xFFEFEFEF.toInt()
    @ColorInt const val SEARCH_INPUT_PLACEHOLD = 0xFFDDDDDD.toInt()
    @ColorInt const val LINE_COLOR = 0xFF2196F3.toInt()
    @ColorInt const val BLACK_BACKGROUND_COLOR = 0xFF000000.toInt()
    @ColorInt const val WHITE_BACKGROUND_COLOR = 0xFFFFFFFF.toInt()
    @ColorInt const val SELECTED_COLOR = 0xFFFF9A00.toInt()
    @ColorInt const val SUB_TITLE_COLOR = 0xFF999999.toInt()
    @ColorInt const val WARN_COLOR = 0xFFF7453B.toInt()
    @ColorInt const val BLUE_COLOR = 0xFF3E7D9B.toInt()
    @ColorInt const val LINEAR_GRADIENT = 0xFF333333.toInt()
    @ColorInt const val POP_BACKGROUND_COLOR = 0xFF333333.toInt()
    @ColorInt const val POP_LINE_COLOR = 0xFF444444.toInt()

    // Compose Material3 颜色定义
    @ColorInt const val PRIMARY_COLOR = 0xFFFFAE00.toInt()
    @ColorInt const val SECONDARY_COLOR = 0xFF2196F3.toInt()
    @ColorInt const val BACKGROUND_COLOR = 0xFFEFEFEF.toInt()
    @ColorInt const val TEXT_COLOR = 0xFF000000.toInt()
    @ColorInt const val DARK_BACKGROUND_COLOR = 0xFF000000.toInt() // 添加暗色背景常量
    @ColorInt const val DARK_TEXT_COLOR = 0xFFFFFFFF.toInt() // 添加暗色文字颜色

    // 为了兼容 Compose Color 类型，同时提供 Color 类型常量
    val PrimaryColor = ComposeColor(PRIMARY_COLOR)
    val SecondaryColor = ComposeColor(SECONDARY_COLOR)
    val BackgroundColor = ComposeColor(BACKGROUND_COLOR)
    val TextColor = ComposeColor(TEXT_COLOR)
    val DarkBackgroundColor = ComposeColor(DARK_BACKGROUND_COLOR)
    val DarkTextColor = ComposeColor(DARK_TEXT_COLOR)
    val pageBackgroundColor = ComposeColor(PAGE_BACKGROUND_COLOR)
    val White = ComposeColor.White
    val Red = ComposeColor.Red
    val Black = ComposeColor.Black

    val Gray = ComposeColor.Gray

    val disableTextColor = ComposeColor(DISABLE_TEXT_COLOR)

    val subTitleColor = ComposeColor(SUB_TITLE_COLOR)
}