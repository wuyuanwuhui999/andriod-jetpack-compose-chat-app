package com.player.chat.model

import com.google.gson.annotations.SerializedName

/**
 * 更新模型请求
 */
data class UpdateModelRequest(
    @SerializedName("id") val id: String,
    @SerializedName("modelName") val modelName: String,
    @SerializedName("type") val type: String,
    @SerializedName("companyId") val companyId: String,
    @SerializedName("apiKey") val apiKey: String? = null,
    @SerializedName("baseUrl") val baseUrl: String
)