package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 部门数据模型
 */
data class Department(
    @SerializedName("id") val id: String,
    @SerializedName("companyId") val companyId: String,
    @SerializedName("departmentName") val departmentName: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("createTime") val createTime: String
)