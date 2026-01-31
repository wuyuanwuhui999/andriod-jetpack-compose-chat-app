package com.player.chat.model

import com.google.gson.annotations.SerializedName

data class TenantUser(
    @SerializedName("id") val id: String,
    @SerializedName("tenantId") val tenantId: String,
    @SerializedName("tenantName") val tenantName: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("roleType") val roleType: Int,
    @SerializedName("joinDate") val joinDate: String,
    @SerializedName("createBy") val createBy: String,
    @SerializedName("username") val username: String,
    @SerializedName("avater") val avatar: String?,
    @SerializedName("disabled") val disabled: Int,
    @SerializedName("email") val email: String
)