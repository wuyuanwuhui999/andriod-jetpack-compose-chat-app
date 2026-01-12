package com.player.chat.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("data") val data: T?,
    @SerializedName("token") val token: String?,
    @SerializedName("status") val status: String,
    @SerializedName("msg") val message: String?,
    @SerializedName("total") val total: Int?
)