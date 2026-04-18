package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 提示词数据模型
 */
data class Prompt(
    @SerializedName("id")
    val id: String,

    @SerializedName("tenantId")
    val tenantId: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("prompt")
    val prompt: String,

    @SerializedName("createTime")
    val createTime: String? = null,

    @SerializedName("updateTime")
    val updateTime: String? = null
)

/**
 * 更新提示词请求
 */
data class UpdatePromptRequest(
    @SerializedName("id")
    val id: String,

    @SerializedName("tenantId")
    val tenantId: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("prompt")
    val prompt: String
)