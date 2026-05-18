// models/TenantUserInfo.kt
package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 租户用户信息
 * 对应接口 /service/tenant/getTenantUser 返回的数据
 */
data class TenantUserInfo(
    @SerializedName("id") val id: String,
    @SerializedName("tenantId") val tenantId: String,
    @SerializedName("tenantName") val tenantName: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("roleType") val roleType: Int,
    @SerializedName("joinDate") val joinDate: String,  // 已经是 String，保持
    @SerializedName("createBy") val createBy: String,
    @SerializedName("username") val username: String,
    @SerializedName("avater") val avatar: String?,
    @SerializedName("disabled") val disabled: Int,
    @SerializedName("email") val email: String
)
