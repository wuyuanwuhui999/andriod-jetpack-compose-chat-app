package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 添加公司用户请求
 */
data class AddCompanyUserRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("companyId") val companyId: String,
    @SerializedName("role") val role: Int, // 0:普通用户, 1:管理员
    @SerializedName("positionId") val positionId: String? = null
)