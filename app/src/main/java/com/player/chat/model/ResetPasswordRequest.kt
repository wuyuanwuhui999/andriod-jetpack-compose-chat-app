package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 重置密码请求
 */
data class ResetPasswordRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("password")
    val password: String
)