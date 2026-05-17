// models/TenantUserInfo.kt
package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 租户用户信息
 * 对应接口 /service/tenant/getTenantUser 返回的数据
 */
data class TenantUserInfo(
    @SerializedName("id")
    val id: String,  // 主键

    @SerializedName("tenantId")
    val tenantId: String,  // 租户id

    @SerializedName("tenantName")
    val tenantName: String,

    @SerializedName("userId")
    val userId: String,  // 用户id

    @SerializedName("roleType")
    val roleType: Int,  // 用户角色 (0-普通用户，1-租户管理员，2-超级管理员)

    @SerializedName("joinDate")
    val joinDate: String,  // 加入日期

    @SerializedName("createBy")
    val createBy: String,  // 创建时间

    @SerializedName("username")
    val username: String,  // 用户名

    @SerializedName("avater")
    val avatar: String?,  // 头像

    @SerializedName("disabled")
    val disabled: Int,  // 是否禁用

    @SerializedName("email")
    val email: String  // 邮箱
)

/**
 * 租户用户信息响应
 */
data class TenantUserInfoResponse(
    @SerializedName("data")
    val data: List<TenantUserInfo>? = null,

    @SerializedName("total")
    val total: Int? = null,

    @SerializedName("msg")
    val message: String? = null,

    @SerializedName("status")
    val status: String
)