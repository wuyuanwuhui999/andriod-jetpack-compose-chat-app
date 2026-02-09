package com.player.chat.model

import com.google.gson.annotations.SerializedName

data class ChatHistory(
    @SerializedName("id") val id: Long,
    @SerializedName("modelName") val modelName: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("files") val files: String? = null,
    @SerializedName("chatId") val chatId: String,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("SystemPrompt") val systemPrompt: String,
    @SerializedName("content") val content: String,
    @SerializedName("createTime") val createTime: String,
    @SerializedName("thinkContent") val thinkContent: String? = null,
    @SerializedName("responseContent") val responseContent: String? = null
)

data class ChatHistoryResponse(
    @SerializedName("data") val data: List<ChatHistory>? = null,
    @SerializedName("status") val status: String,
    @SerializedName("msg") val message: String?,
    @SerializedName("total") val total: Int?
)