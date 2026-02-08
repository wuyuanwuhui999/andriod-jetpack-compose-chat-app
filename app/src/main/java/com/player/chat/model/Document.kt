// Document.kt
package com.player.chat.model

import com.google.gson.annotations.SerializedName

data class Document(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("ext")
    val ext: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("createTime")
    val createTime: String,

    @SerializedName("updateTime")
    val updateTime: String,

    @SerializedName("directoryId")
    val directoryId: String,

    @SerializedName("directoryName")
    val directoryName: String,

    // 本地状态，非后端字段
    var checked: Boolean = false
)