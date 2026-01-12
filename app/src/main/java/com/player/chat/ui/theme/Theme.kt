// Theme.kt
package com.player.chat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Color.PrimaryColor,
    secondary = Color.SecondaryColor,
    background = Color.BackgroundColor,
    surface = Color.BackgroundColor,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onBackground = Color.TextColor,
    onSurface = Color.TextColor
)

private val DarkColorScheme = darkColorScheme(
    primary = Color.PrimaryColor,
    secondary = Color.SecondaryColor,
    background = Color.DarkBackgroundColor, // 使用 DarkBackgroundColor
    surface = Color.DarkBackgroundColor, // 使用 DarkBackgroundColor
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onBackground = Color.DarkTextColor, // 使用 DarkTextColor
    onSurface = Color.DarkTextColor // 使用 DarkTextColor
)

@Composable
fun ChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}