// UpdatePasswordRequest.kt
package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 修改密码请求
 */
data class UpdatePasswordRequest(
    @SerializedName("oldPassword")
    val oldPassword: String,

    @SerializedName("newPassword")
    val newPassword: String
)