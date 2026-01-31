package com.player.chat.model

import com.google.gson.annotations.SerializedName
import java.util.Date


/**
 * 租户状态枚举
 */
enum class TenantStatus(val value: Int) {
    /** 禁用 */
    @SerializedName("0")
    DISABLED(0),

    /** 启用 */
    @SerializedName("1")
    ENABLED(1)
}

/**
 * 租户数据类型
 * @property id 租户ID（主键）
 * @property name 租户名称
 * @property code 租户编码（唯一）
 * @property description 租户描述（可选）
 * @property status 租户状态
 * @property createDate 创建时间
 * @property updateDate 更新时间（可选）
 * @property createdBy 创建人ID
 * @property updatedBy 更新人ID（可选）
 */
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

    // 如果 API 返回的是 create_date，这里需要保持 @SerializedName 一致
    @SerializedName("create_date")
    val createDate: Date? = null,

    @SerializedName("update_date")
    val updateDate: Date? = null,

    // 与 TenantUser 保持一致，但注意 API 实际返回的字段名
    @SerializedName("created_by")
    val createBy: String,

    @SerializedName("updated_by")
    val updateBy: String? = null
)

object DefaultTenant {
    /**
     * 默认租户 - 私人空间
     * 用于当前用户没有加入任何租户时的默认展示
     */
    val PERSONAL_SPACE = Tenant(
        id = "personal_space",
        name = "私人空间",
        code = "personal",
        description = "个人聊天空间，不归属于任何租户",
        status = TenantStatus.ENABLED,
        createDate = null,
        updateDate = null,
        createBy = "",
        updateBy = null
    )
}