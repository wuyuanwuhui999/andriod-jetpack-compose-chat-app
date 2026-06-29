package com.player.chat.model

import com.google.gson.annotations.SerializedName

enum class TenantStatus(val value: Int) {
    @SerializedName("0")
    DISABLED(0),

    @SerializedName("1")
    ENABLED(1)
}

data class Tenant(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("status")
    val status: TenantStatus,

    @SerializedName("createDate")
    val createDate: String? = null,

    @SerializedName("updateDate")
    val updateDate: String? = null,

    @SerializedName("createdBy")
    val createdBy: String? = null,

    @SerializedName("updatedBy")
    val updatedBy: String? = null,

    // 新增字段：当前用户在该租户下的角色
    // 0: 普通用户, 1: 租户管理员, 2: 超级管理员
    @SerializedName("role")
    val role: Int = 0
)

object DefaultTenant {
    val PERSONAL_SPACE = Tenant(
        id = "personal_space",
        name = "私人空间",
        code = "personal",
        description = "个人聊天空间，不归属于任何租户",
        status = TenantStatus.ENABLED,
        createDate = null,
        updateDate = null,
        createdBy = "",
        updatedBy = null,
        role = 2 // 私人空间默认为超级管理员
    )
}