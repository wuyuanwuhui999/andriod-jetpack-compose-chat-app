package com.player.chat.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: String,
    @SerializedName("userAccount") val userAccount: String,
    @SerializedName("createDate") val createDate: String,
    @SerializedName("updateDate") val updateDate: String,
    @SerializedName("username") val username: String,
    @SerializedName("telephone") val telephone: String,
    @SerializedName("email") val email: String,
    @SerializedName("avater") val avatar: String?,
    @SerializedName("birthday") val birthday: String?,
    @SerializedName("sex") val sex: Int,
    @SerializedName("role") val role: String,
    @SerializedName("password") val password: String?,
    @SerializedName("sign") val sign: String?,
    @SerializedName("region") val region: String?,
    @SerializedName("disabled") val disabled: Int,
    @SerializedName("permission") val permission: Int
)