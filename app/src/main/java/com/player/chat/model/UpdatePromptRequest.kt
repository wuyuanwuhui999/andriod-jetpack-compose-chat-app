package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 更新提示词请求
 */
data class UpdatePromptRequest(
    @SerializedName("id")
    val id: String,

    @SerializedName("tenantId")
    val tenantId: String,

    @SerializedName("userId")
    val userId: String? = null, // 改为可选

    @SerializedName("prompt")
    val prompt: String
)