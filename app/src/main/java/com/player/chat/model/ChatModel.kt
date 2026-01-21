// ChatModel.kt
package com.player.chat.model

import com.google.gson.annotations.SerializedName

data class ChatModel(
    @SerializedName("id") val id: String,
    @SerializedName("modelName") val modelName: String,
    @SerializedName("updateTime") val updateTime: String,
    @SerializedName("createTime") val createTime: String
)