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
    val createTime: String? = null,  // 已经是 String，保持

    @SerializedName("updateTime")
    val updateTime: String? = null  // 已经是 String，保持
)