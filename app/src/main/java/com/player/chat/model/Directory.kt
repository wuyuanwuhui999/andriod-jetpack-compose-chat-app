package com.player.chat.model

import com.google.gson.annotations.SerializedName

data class Directory(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("userId")
    val userId: String? = null,

    @SerializedName("directory")
    val directory: String,

    @SerializedName("tenantId")
    val tenantId: String,

    @SerializedName("createTime")
    val createTime: String? = null,  // 已经是 String，保持

    @SerializedName("updateTime")
    val updateTime: String? = null  // 已经是 String，保持
)
