// Tanent.kt - 修改 Tenant 数据类
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

    // 将 Date 类型改为 String 类型
    @SerializedName("createDate")
    val createDate: String? = null,  // 改为 String

    @SerializedName("updateDate")
    val updateDate: String? = null,  // 改为 String

    @SerializedName("createdBy")
    val createdBy: String,

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
        createdBy = "",
        updatedBy = null
    )
}