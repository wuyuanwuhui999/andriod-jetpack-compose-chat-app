package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 添加模型请求
 */
data class AddModelRequest(
    @SerializedName("modelName") val modelName: String,
    @SerializedName("type") val type: String,
    @SerializedName("companyId") val companyId: String,
    @SerializedName("apiKey") val apiKey: String? = null,
    @SerializedName("baseUrl") val baseUrl: String
)