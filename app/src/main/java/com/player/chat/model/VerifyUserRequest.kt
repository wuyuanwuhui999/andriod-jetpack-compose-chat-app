// VerifyUserRequest.kt
package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 校验用户是否存在请求
 */
data class VerifyUserRequest(
    @SerializedName("userAccount")
    val userAccount: String? = null,

    @SerializedName("email")
    val email: String? = null
)