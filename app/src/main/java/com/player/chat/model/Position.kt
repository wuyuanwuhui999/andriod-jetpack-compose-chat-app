package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 职位数据模型
 */
data class Position(
    @SerializedName("id") val id: String,
    @SerializedName("positionName") val positionName: String,
    @SerializedName("departmentId") val departmentId: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("createTime") val createTime: String
)