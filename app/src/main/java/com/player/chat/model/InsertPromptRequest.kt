package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 新增提示词请求
 */
data class InsertPromptRequest(
    @SerializedName("prompt")
    val prompt: String,

    @SerializedName("tenantId")
    val tenantId: String
)