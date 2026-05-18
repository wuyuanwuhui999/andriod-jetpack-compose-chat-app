package com.player.chat.model

import com.google.gson.annotations.SerializedName
import java.util.Date

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
    val createdBy: String? = null,  // 修改为可空类型

    @SerializedName("updatedBy")
    val updatedBy: String? = null
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
        createdBy = "",  // 使用空字符串而非 null，避免空指针
        updatedBy = null
    )
}