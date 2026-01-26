package com.player.chat.model

enum class PositionEnum {
    LEFT,
    RIGHT
}

data class ChatMessage(
    val position: PositionEnum,
    val thinkContent: String? = null,
    val responseContent: String,
    val timestamp: Long = System.currentTimeMillis()
)