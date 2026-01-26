package com.player.chat.network

import android.util.Log
import com.google.gson.Gson
import com.player.chat.config.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.*
import java.util.UUID
import java.util.concurrent.TimeUnit

class WebSocketManager {
    private var webSocket: WebSocket? = null
    private val gson = Gson()

    data class ChatRequest(
        val modelId: String,
        val token: String,
        val chatId: String,
        val tenantId: String,
        val docIds: List<String>? = null,
        val prompt: String,
        val systemPrompt: String = "你是一个智能助手",
        val showThink: Boolean = false,
        val language: String = "zh"
    )

    fun connectWebSocket(
        token: String,
        chatId: String,
        onMessageReceived: (String) -> Unit,
        onConnected: () -> Unit,
        onClosed: () -> Unit
    ) {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("ws://${Config.BASE_URL.removePrefix("http://")}/service/chat/ws/chat?token=$token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "连接已建立")
                onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "收到消息: $text")
                onMessageReceived(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "连接已关闭: $code $reason")
                onClosed()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "连接失败", t)
                onClosed()
            }
        })
    }

    fun sendMessage(request: ChatRequest) {
        val json = gson.toJson(request)
        webSocket?.send(json)
        Log.d("WebSocket", "发送消息: $json")
    }

    fun closeWebSocket() {
        webSocket?.close(1000, "正常关闭")
        webSocket = null
    }

    fun isConnected(): Boolean {
        return webSocket != null
    }
}

// 创建WebSocket流式响应处理器
object WebSocketMessageHandler {
    private const val THINK_START = "<think>"
    private const val THINK_END = "</think>"
    private const val COMPLETED = "[completed]"

    data class ParsedMessage(
        val thinkContent: String?,
        val responseContent: String,
        val isCompleted: Boolean
    )

    fun parseMessage(message: String): ParsedMessage {
        var thinkContent: String? = null
        var responseContent = message
        var isCompleted = false

        // 检查是否包含思考内容
        if (message.contains(THINK_START) && message.contains(THINK_END)) {
            val startIndex = message.indexOf(THINK_START) + THINK_START.length
            val endIndex = message.indexOf(THINK_END)
            thinkContent = message.substring(startIndex, endIndex)
            responseContent = message.substring(endIndex + THINK_END.length)
        }

        // 检查是否完成
        if (responseContent.contains(COMPLETED)) {
            isCompleted = true
            responseContent = responseContent.replace(COMPLETED, "")
        }

        return ParsedMessage(thinkContent, responseContent, isCompleted)
    }
}